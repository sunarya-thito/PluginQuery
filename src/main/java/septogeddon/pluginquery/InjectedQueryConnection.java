package septogeddon.pluginquery;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import septogeddon.pluginquery.api.*;
import septogeddon.pluginquery.netty.QueryHandshaker;
import septogeddon.pluginquery.netty.QueryProtocol;
import septogeddon.pluginquery.netty.QueryReadTimeout;
import septogeddon.pluginquery.utils.Debug;
import septogeddon.pluginquery.utils.QueryUtil;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class InjectedQueryConnection implements QueryConnection {

    private final QueryMetadata data = new QueryMetadataImpl();
    private final QueryEventBus events = new QueryEventBusImpl();
    private final Queue<QueryQueue> queue = new LinkedList<>();
    private final QueryMessenger messenger;
    private final Channel channel;
    private boolean handshaken;
    private final QueryProtocol protocol;

    public InjectedQueryConnection(QueryMessenger messenger, Channel channel) {
        this.messenger = messenger;
        this.channel = channel;
        protocol = new QueryProtocol(messenger, this) {
            public void onHandshaken() {
                handshaken = true;
                connectionConnected();
            }
        };
        prepareChannel();
    }

    protected void connectionDisconnected() {
        Debug.debug(() -> "Connection: END");
        getMessenger().getPipeline().dispatchInactive(this);
        getEventBus().dispatchConnectionState(this);
        protocol.clear();
        handshaken = false;
    }

    protected void connectionConnected() {
        Debug.debug(() -> "Connection: DONE");
        getMessenger().getPipeline().dispatchActive(this);
        getEventBus().dispatchConnectionState(this);
        flushQueue();
    }

    protected void prepareChannel() {
        Debug.debug(() -> "Connection: PREPARE");
        getChannel().closeFuture().addListener((ChannelFuture f) -> {
            connectionDisconnected();
        });
        getChannel().pipeline().addFirst("query_handshaker", new QueryHandshaker(protocol));
        getChannel().pipeline().addFirst(QueryContext.PIPELINE_TIMEOUT, new QueryReadTimeout(this, getMessenger().getMetadata().getData(QueryContext.METAKEY_READ_TIMEOUT, 1000L * 30), TimeUnit.MILLISECONDS));
    }

    public void flushQueue() {
        if (getChannel() != null && !getChannel().eventLoop().inEventLoop()) {
            getChannel().eventLoop().submit(() -> {
                synchronized (this.queue) {
                    QueryQueue queue;
                    while ((queue = this.queue.poll()) != null) sendQueryMessage(queue.message, queue.future, true);
                }
            });
        } else {
            synchronized (queue) {
                QueryQueue queue;
                while ((queue = this.queue.poll()) != null) sendQueryMessage(queue.message, queue.future, true);
            }
        }
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public SocketAddress getAddress() {
        return channel.remoteAddress();
    }

    @Override
    public boolean isConnected() {
        return channel.isOpen();
    }

    @Override
    public boolean isHandshaken() {
        return handshaken;
    }

    @Override
    public QueryMessenger getMessenger() {
        return messenger;
    }

    @Override
    public QueryFuture<QueryConnection> connect() {
        throw new UnsupportedOperationException("injected connection");
    }

    @Override
    public QueryFuture<QueryConnection> disconnect() {
        if (getChannel() != null) {
            if (getChannel().isOpen()) {
                Debug.debug(() -> "Disconnect: ATTEMPT");
                getChannel().disconnect();
            }
        }
        return new QueryChannelFuture<>(channel.closeFuture(), this);
    }

    @Override
    public QueryMetadata getMetadata() {
        return data;
    }

    @Override
    public QueryEventBus getEventBus() {
        return events;
    }

    @Override
    public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message) {
        return sendQuery(channel, message, false);
    }

    public void sendQueryMessage(QueryMessage msg, QueryCompletableFuture<QueryConnection> future, boolean queue) {
        if (isHandshaken()) {
            if (channel.eventLoop().inEventLoop()) {
                ChannelFutureListener futureListener = (ChannelFuture f) -> {
                    // wrapping a listener, what a shame...
                    if (f.isSuccess()) {
                        future.complete(this);
                    } else {
                        future.completeExceptionally(f.cause());
                    }
                };
                getChannel().writeAndFlush(msg).addListener(futureListener);
            } else {
                channel.eventLoop().submit(() -> sendQueryMessage(msg, future, queue));
            }
        } else {
            if (queue) {
                synchronized (this.queue) {
                    QueryUtil.illegalState(!this.queue.offer(new QueryQueue(msg, future)), "failed to offer queue");
                }
            }
        }
    }

    @Override
    public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message, boolean queue) {
        QueryCompletableFuture<QueryConnection> future = new QueryCompletableFuture<QueryConnection>();
        sendQueryMessage(new QueryMessage(channel, message), future, queue);
        return future;
    }

    static class QueryQueue {
        QueryMessage message;
        QueryCompletableFuture<QueryConnection> future;

        QueryQueue(QueryMessage m, QueryCompletableFuture<QueryConnection> f) {
            message = m;
            future = f;
        }
    }
}
