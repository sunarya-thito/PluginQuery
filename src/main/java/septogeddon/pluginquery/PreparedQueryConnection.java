package septogeddon.pluginquery;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import septogeddon.pluginquery.api.*;
import septogeddon.pluginquery.netty.QueryProtocol;
import septogeddon.pluginquery.utils.Debug;
import septogeddon.pluginquery.utils.QueryUtil;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PreparedQueryConnection implements QueryConnection {

    private final SocketAddress address;
    private final QueryMessenger messenger;
    private final QueryMetadata metadata = new QueryMetadataImpl();
    private final QueryEventBus eventBus = new QueryEventBusImpl();
    private final Queue<QueueQuery> queues = new LinkedList<>();
    private ChannelFuture channelFuture;
    private final QueryProtocol protocol;
    private final CloseListener closeFuture = new CloseListener();
    private boolean handshaken;

    public PreparedQueryConnection(QueryMessenger messenger, SocketAddress address) {
        this.messenger = messenger;
        this.address = address;
        this.protocol = new QueryProtocol(messenger, this) {
            @Override
            public void onHandshaken() {
                handshaken = true;
                connectionConnected();
                super.onHandshaken();
            }
        };
    }

    @Override
    public QueryFuture<Set<QueryConnection>> fetchActiveConnections() {
        QueryCompletableFuture<Set<QueryConnection>> listQueryCompletableFuture = new QueryCompletableFuture<>();
        listQueryCompletableFuture.complete(new HashSet<>(messenger.getActiveConnections()));
        return listQueryCompletableFuture;
    }

    public static void handshakenConnection(QueryProtocol protocol, ChannelPipeline pipeline) {
        // RESET ALL INSTANCES
        protocol.clear();

        // INBOUND
        // read order from top to down

        // another name of FramePrepender
        pipeline.addLast(protocol.getAppender());
        // handle QueryPipeline
        pipeline.addLast(protocol.getPipelineInbound());
        // decode Bytes to Query Message
        pipeline.addLast(protocol.getDecoder());
        // manage incoming Query Message
        pipeline.addLast(protocol.getManager());

        // OUTBOUND
        // write order from down to top

        // record message length, useful for Appender to know how long
        // was the packet sent to the server
        pipeline.addLast(protocol.getSplitter());
        // handle QueryPipeline
        pipeline.addLast(protocol.getPipelineOutbound());
        // encode the message sent from
        pipeline.addLast(protocol.getEncoder());
        protocol.onHandshaken();
    }

    public static ByteBuf createHandshake(ByteBuf buf, QueryConnection conn) {
        // signature
        buf.writeByte((byte) QueryContext.PACKET_HANDSHAKE.length());
        buf.writeBytes(QueryContext.PACKET_HANDSHAKE.getBytes());
        // UUID
        UUID randomized = UUID.randomUUID();
        buf.writeLong(randomized.getMostSignificantBits());
        buf.writeLong(randomized.getLeastSignificantBits());
        // encrypt UUID
        String uuid = randomized.toString();
        byte[] encrypted = conn.getMessenger().getPipeline().dispatchSending(conn, uuid.getBytes());
        QueryUtil.nonNull(encrypted, "encrypted token");
        QueryUtil.illegalArgument(encrypted.length > Byte.MAX_VALUE, "encrypted token too long");
        // send handshake
        buf.writeByte((byte) encrypted.length);
        buf.writeBytes(encrypted);
        return buf;
    }

    @Override
    public boolean isHandshaken() {
        return handshaken;
    }

    protected void prepareChannel() {
        handshakenConnection(protocol, channelFuture.channel().pipeline());
    }

    protected void connectionDisconnected() {
        Debug.debug("Connection: END");
        getMessenger().getPipeline().dispatchInactive(this);
        getEventBus().dispatchConnectionState(this);
        queues.clear();
        protocol.clear();
        handshaken = false;
    }

    protected void connectionConnected() {
        getMessenger().getPipeline().dispatchActive(this);
        getEventBus().dispatchConnectionState(this);
        getChannel().closeFuture().removeListener(closeFuture);
        getChannel().closeFuture().addListener(closeFuture);
        flushQueue();
    }

    @Override
    public QueryMessenger getMessenger() {
        return messenger;
    }

    public void handshake(QueryCompletableFuture<QueryConnection> future, int currentTime) {
        ByteBuf buf = createHandshake(getChannel().alloc().heapBuffer(), this);
        ChannelFuture fut = getChannel().writeAndFlush(buf);
        fut.addListener((ChannelFuture f) -> {
            if (f.isSuccess()) {
                Debug.debug("Connection: SUCCESS");
                prepareChannel();
                future.complete(this);
                return;
            }
            Throwable cause = f.cause();
            if (!f.channel().isOpen() || (cause == null && !f.isSuccess())) {
                cause = new IllegalStateException("connection closed");
            }
            if (cause != null) {
                Debug.debug("Connection: CLOSE: " + f.cause());
                long reconnectDelay = getMetadata().getData(QueryContext.METAKEY_RECONNECT_DELAY, -1L);
                if (reconnectDelay >= 0) {
                    int maxTime = getMetadata().getData(QueryContext.METAKEY_MAX_RECONNECT_TRY, 0);
                    if (maxTime >= 0 && currentTime + 1 > maxTime) {
                        future.completeExceptionally(cause);
                        disconnect();
                        return;
                    }
                    fut.channel().eventLoop().schedule(() -> {
                        channelFuture = null;
                        connect(currentTime + 1);
                    }, reconnectDelay, TimeUnit.MILLISECONDS);
                    return;
                }
                future.completeExceptionally(cause);
                disconnect();
            }
        });
    }

    @Override
    public QueryFuture<QueryConnection> connect() {
        return connect(0);
    }

    public QueryFuture<QueryConnection> connect(int currentTime) {
        disconnect();
        QueryCompletableFuture<QueryConnection> fut = new QueryCompletableFuture<>();
        Bootstrap client = new Bootstrap();
        client.group(getMessenger().getEventLoopGroup());
        client.channel(getMessenger().getChannelClass());
        client.option(ChannelOption.SO_KEEPALIVE, true);
        client.option(ChannelOption.TCP_NODELAY, false);
        client.option(ChannelOption.AUTO_READ, true);
        client.handler(new ChannelDuplexHandler());
        client.remoteAddress(address);
        ChannelFuture future = client.connect();
        this.channelFuture = future;
        future.addListener((ChannelFuture f) -> {
            if (!f.isSuccess()) {
                Debug.debug("Connection: FAILED: " + f.cause() + " (" + f.channel().remoteAddress() + ")");
                long reconnectDelay = getMetadata().getData(QueryContext.METAKEY_RECONNECT_DELAY, -1L);
                if (reconnectDelay >= 0) {
                    int maxTime = getMetadata().getData(QueryContext.METAKEY_MAX_RECONNECT_TRY, 0);
                    if (maxTime >= 0 && currentTime + 1 > maxTime) {
                        fut.completeExceptionally(f.cause());
                        return;
                    }
                    future.channel().eventLoop().schedule(() -> {
                        connect(currentTime + 1);
                    }, reconnectDelay, TimeUnit.MILLISECONDS);
                } else {
                    fut.completeExceptionally(f.cause());
                }
            } else {
                Debug.debug("Connection: DONE");
                handshake(fut, currentTime);
            }
        });
        return fut;
    }

    public void flushQueue() {
        if (getChannel() != null && !getChannel().eventLoop().inEventLoop()) {
            getChannel().eventLoop().schedule(() -> {
                synchronized (queues) {
                    QueueQuery queue;
                    while ((queue = queues.poll()) != null) sendPrivately(queue);
                }
            }, 1, TimeUnit.MILLISECONDS);
        } else {
            synchronized (queues) {
                QueueQuery queue;
                while ((queue = queues.poll()) != null) sendPrivately(queue);
            }
        }
    }

    @Override
    public QueryFuture<QueryConnection> disconnect() {
        Channel c = getChannel();
        if (c != null) {
            if (c.isOpen()) {
                Debug.debug("Disconnect: ATTEMPT");
                c.disconnect();
            }
            return new QueryChannelFuture<>(c.closeFuture(), this);
        }
        return new QueryChannelFuture<>(null, this);
    }

    public void finalize() {
        disconnect();
    }

    @Override
    public QueryMetadata getMetadata() {
        return metadata;
    }

    @Override
    public QueryEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public boolean isConnected() {
        return channelFuture != null && channelFuture.isSuccess() && channelFuture.channel() != null && channelFuture.channel().isOpen();
    }

    @Override
    public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message) {
        return sendQuery(channel, message, true);
    }

    public QueryFuture<QueryConnection> sendQuery(QueryMessage message, boolean queue) {
        QueryCompletableFuture<QueryConnection> future = new QueryCompletableFuture<>();
        sendPrivately(new QueueQuery(message, future, queue));
        return future;
    }

    private void sendPrivately(QueueQuery query) {
        if (getChannel() == null || getChannel().eventLoop().inEventLoop()) {
            sendDirectly(query);
        } else {
            getChannel().eventLoop().submit(() -> sendDirectly(query));
        }
    }

    private void sendDirectly(QueueQuery a) {
        if (isConnected()) {
            ChannelFutureListener futureListener = f -> {
                // wrapping a listener, what a shame...
                if (f.isSuccess()) {
                    a.future.complete(this);
                } else {
                    a.future.completeExceptionally(f.cause());
                }
            };
            getChannel().writeAndFlush(a.message).addListener(futureListener);
        } else if (a.queue) {
            synchronized (queues) {
                if (!queues.offer(a)) {
                    a.future.completeExceptionally(new IllegalStateException("failed to queue query"));
                }
            }
        }
    }

    @Override
    public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message, boolean queue) {
        return sendQuery(new QueryMessage(channel, message), queue);
    }

    @Override
    public Channel getChannel() {
        return channelFuture == null ? null : channelFuture.channel();
    }

    static class QueueQuery {
        QueryMessage message;
        QueryCompletableFuture<QueryConnection> future;
        boolean queue;

        public QueueQuery(QueryMessage m, QueryCompletableFuture<QueryConnection> f, boolean q) {
            message = m;
            future = f;
            queue = q;
        }
    }

    private class CloseListener implements ChannelFutureListener {

        @Override
        public void operationComplete(ChannelFuture arg0) throws Exception {
            connectionDisconnected();
        }

    }

}
