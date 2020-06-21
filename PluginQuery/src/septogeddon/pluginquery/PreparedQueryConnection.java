package septogeddon.pluginquery;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryEventBus;
import septogeddon.pluginquery.api.QueryFuture;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.api.QueryMetadata;
import septogeddon.pluginquery.netty.QueryProtocol;
import septogeddon.pluginquery.utils.QueryUtil;

public class PreparedQueryConnection implements QueryConnection {

	private class CloseListener implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture arg0) throws Exception {
			connectionDisconnected();
		}
		
	}
	private SocketAddress address;
	private QueryMessenger messenger;
	private QueryMetadata metadata = new QueryMetadataImpl();
	private QueryEventBus eventBus = new QueryEventBusImpl();
	private Queue<QueueQuery> queues = new LinkedList<>();
	private ChannelFuture channelFuture;
	private QueryProtocol protocol;
	private CloseListener closeFuture = new CloseListener();
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
	public boolean isHandshaken() {
		return handshaken;
	}
	
	protected void prepareChannel() {
		handshakenConnection(protocol, channelFuture.channel().pipeline());
	}
	
	protected void connectionDisconnected() {
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
	
	@Override
	public QueryMessenger getMessenger() {
		return messenger;
	}
	
	public static ByteBuf createHandshake(ByteBuf buf, QueryConnection conn) {
		// signature
		buf.writeByte((byte)QueryContext.PACKET_HANDSHAKE.length());
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
		buf.writeByte((byte)encrypted.length);
		buf.writeBytes(encrypted);
		return buf;
	}
	
	public void handshake(QueryCompletableFuture<QueryConnection> future, int currentTime) {
		ByteBuf buf = createHandshake(getChannel().alloc().heapBuffer(), this);
		ChannelFuture fut = getChannel().writeAndFlush(buf);
		fut.addListener((ChannelFuture f)->{
			if (f.isSuccess()) {
				prepareChannel();
				future.complete(this);
				return;
			}
			Throwable cause = f.cause();
			if (!f.channel().isOpen() || (cause == null && !f.isSuccess())) {
				cause = new IllegalStateException("connection closed");
			}
			if (cause != null) {
				long reconnectDelay = getMetadata().getData(QueryContext.METAKEY_RECONNECT_DELAY, -1L);
				if (reconnectDelay >= 0) {
					int maxTime = getMetadata().getData(QueryContext.METAKEY_MAX_RECONNECT_TRY, 0);
					if (maxTime >= 0 && currentTime + 1 > maxTime) {
						future.completeExceptionally(cause);
						disconnect();
						return;
					}
					fut.channel().eventLoop().schedule(()->{
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
		future.addListener((ChannelFuture f)->{
			if (!f.isSuccess()) {
				getMessenger().getPipeline().dispatchUncaughtException(this, f.cause());
				long reconnectDelay = getMetadata().getData(QueryContext.METAKEY_RECONNECT_DELAY, -1L);
				if (reconnectDelay >= 0) {
					int maxTime = getMetadata().getData(QueryContext.METAKEY_MAX_RECONNECT_TRY, 0);
					if (maxTime >= 0 && currentTime + 1 > maxTime) {
						fut.completeExceptionally(f.cause());
						return;
					}
					future.channel().eventLoop().schedule(()->{
						channelFuture = null;
						connect(currentTime + 1);
					}, reconnectDelay, TimeUnit.MILLISECONDS);
				} else {
					fut.completeExceptionally(f.cause());
				}
			} else {
				handshake(fut, currentTime);
			}
		});
		return fut;
	}
	
	public void flushQueue() {
		if (getChannel() != null && !getChannel().eventLoop().inEventLoop()) {
			getChannel().eventLoop().submit(()->{
				synchronized (queues) {
					QueueQuery queue;
					while ((queue = queues.poll()) != null) sendPrivately(queue);
				}
			});
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
			getChannel().eventLoop().submit(()->sendDirectly(query));
		}
	}
	
	private void sendDirectly(QueueQuery a) {
		if (isConnected()) {
			ChannelFutureListener futureListener = (ChannelFuture f)->{
				// wrapping a listener, what a shame...
				if (f.isSuccess()) {
					a.future.complete(this);
				} else {
					f.cause().printStackTrace();
					a.future.completeExceptionally(f.cause());
				}
			};
			getChannel().writeAndFlush(a.message).addListener(futureListener);
		} else if (a.queue) {
			synchronized(queues) {
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
		public QueueQuery(QueryMessage m, QueryCompletableFuture<QueryConnection> f,boolean q) {
			message = m;
			future = f;
			queue = q;
		}
	}

}
