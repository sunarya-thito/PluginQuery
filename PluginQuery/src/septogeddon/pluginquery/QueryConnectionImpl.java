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
import septogeddon.pluginquery.netty.QueryHandshaker;
import septogeddon.pluginquery.netty.QueryProtocol;
import septogeddon.pluginquery.netty.QueryReadTimeout;
import septogeddon.pluginquery.utils.QueryUtil;

public class QueryConnectionImpl implements QueryConnection {

	private SocketAddress address;
	private QueryMessenger messenger;
	private QueryMetadata metadata = new QueryMetadataImpl();
	private QueryEventBus eventBus = new QueryEventBusImpl();
	private Queue<QueueQuery> queues = new LinkedList<>();
	private Channel channel;
	private QueryProtocol protocol;
	private boolean connecting;
	public QueryConnectionImpl(QueryMessenger messenger, SocketAddress address, Channel channel) {
		this.messenger = messenger;
		this.address = address;
		this.channel = channel;
		this.protocol = new QueryProtocol(messenger, this) {
			@Override
			public void onHandshaken() {
				connectionConnected();
				super.onHandshaken();
			}
		};
		prepareChannel(null);
	}
	
	// from constructor
	protected void prepareChannel(QueryCompletableFuture<QueryConnection> conn) {
		if (channel != null) {
			connecting = true;
			getChannel().pipeline().addFirst(QueryContext.PIPELINE_TIMEOUT, new QueryReadTimeout(this, getMessenger().getMetadata().getData(QueryContext.METAKEY_READ_TIMEOUT, 1000L * 30), TimeUnit.MILLISECONDS));
			getChannel().pipeline().addFirst("query_handshaker",
					new QueryHandshaker(protocol,conn)
					);
		}
	}
	
	protected void connectionDisconnected() {
		connecting = false;
		getMessenger().getPipeline().dispatchInactive(this);
		getEventBus().dispatchConnectionState(this);
		queues.clear();
		protocol.clear();
	}
	
	protected void connectionConnected() {
		connecting = false;
		getMessenger().getPipeline().dispatchActive(this);
		getEventBus().dispatchConnectionState(this);
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
		
		// OUTBOUND
		// write order from down to top
		
		// record message length, useful for Appender to know how long
		// was the packet sent to the server
		pipeline.addLast(protocol.getSplitter());
		// handle QueryPipeline
		pipeline.addLast(protocol.getPipelineOutbound());
		// encode the message sent from
		pipeline.addLast(protocol.getEncoder());
		
		// manage incoming Query Message
		pipeline.addLast(protocol.getManager());
		protocol.onHandshaken();
	}
	
	@Override
	public boolean isConnecting() {
		return connecting;
	}
	
	@Override
	public QueryMessenger getMessenger() {
		return messenger;
	}
	
	public void handshake(QueryCompletableFuture<QueryConnection> future, int currentTime) {
		ByteBuf buf = getChannel().alloc().heapBuffer();
		// send "query"
		buf.writeByte((byte)QueryContext.PACKET_HANDSHAKE.length());
		buf.writeBytes(QueryContext.PACKET_HANDSHAKE.getBytes());
		// send UUID
		UUID randomized = UUID.randomUUID();
		buf.writeLong(randomized.getMostSignificantBits());
		buf.writeLong(randomized.getLeastSignificantBits());
		// encrypt UUID
		String uuid = randomized.toString();
		byte[] handshake = uuid.getBytes();
		handshake = getMessenger().getPipeline().dispatchSending(this, handshake);
		QueryUtil.nonNull(handshake, "unique handshake token");
		// send encrypted UUID
		QueryUtil.illegalArgument(handshake.length > Byte.MAX_VALUE, "unique handshake token too long > "+Byte.MAX_VALUE);
		buf.writeByte((byte)handshake.length);
		buf.writeBytes(handshake);
		// ask to response
		buf.writeBoolean(true);
		
		ChannelFuture fut = getChannel().writeAndFlush(buf);
		fut.addListener((ChannelFuture f)->{
			Throwable cause = f.cause();
			if (!f.channel().isOpen() || (cause == null && !f.isSuccess())) {
				cause = new IllegalStateException("connection closed");
			}
			if (cause != null) {
				connecting = false;
				getMessenger().getPipeline().dispatchUncaughtException(this, f.cause());
				long reconnectDelay = getMetadata().getData(QueryContext.METAKEY_RECONNECT_DELAY, -1L);
				if (reconnectDelay >= 0) {
					int maxTime = getMetadata().getData(QueryContext.METAKEY_MAX_RECONNECT_TRY, 0);
					if (maxTime >= 0 && currentTime + 1 > maxTime) {
						future.completeExceptionally(cause);
						return;
					}
					fut.channel().eventLoop().schedule(()->{
						connect(currentTime + 1);
					}, reconnectDelay, TimeUnit.MILLISECONDS);
				} else {
					future.completeExceptionally(cause);
				}
				future.completeExceptionally(cause);
				if (f.channel().isOpen()) {
					f.channel().close();
				}
			}
		});
	}
	
	@Override
	public QueryFuture<QueryConnection> connect() {
		return connect(0);
	}
	
	public QueryFuture<QueryConnection> connect(int currentTime) {
		if (isConnecting()) return new QueryChannelFuture<>(null, this);
		QueryUtil.illegalState(isConnected(), "already connected");
		connecting = true;
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
		future.addListener((ChannelFuture f)->{
			if (!f.isSuccess()) {
				connecting = false;
				getMessenger().getPipeline().dispatchUncaughtException(this, f.cause());
				long reconnectDelay = getMetadata().getData(QueryContext.METAKEY_RECONNECT_DELAY, -1L);
				if (reconnectDelay >= 0) {
					int maxTime = getMetadata().getData(QueryContext.METAKEY_MAX_RECONNECT_TRY, 0);
					if (maxTime >= 0 && currentTime + 1 > maxTime) {
						fut.completeExceptionally(f.cause());
						return;
					}
					future.channel().eventLoop().schedule(()->{
						connect(currentTime + 1);
					}, reconnectDelay, TimeUnit.MILLISECONDS);
				} else {
					fut.completeExceptionally(f.cause());
				}
			} else {
				channel = f.channel();
				prepareChannel(fut);
				handshake(fut, currentTime);
			}
		});
		return fut;
	}
	
	public void flushQueue() {
		if (channel != null && !channel.eventLoop().inEventLoop()) {
			channel.eventLoop().submit(()->{
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
		connecting = false;
		ChannelFuture future;
		if (channel != null && getMessenger().getActiveConnections().contains(this)) {
			if (channel.isOpen()) {
				future = channel.close();
			} else future = null;
			connectionDisconnected();
		} else future = null;
		return new QueryChannelFuture<>(future, this);
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
		return channel != null && channel.isOpen() && !isConnecting();
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
		if (channel == null || channel.eventLoop().inEventLoop()) {
			sendDirectly(query);
		} else {
			channel.eventLoop().submit(()->sendDirectly(query));
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
			channel.writeAndFlush(a.message).addListener(futureListener);
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
		return channel;
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
