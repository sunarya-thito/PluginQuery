package septogeddon.pluginquery;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryEventBus;
import septogeddon.pluginquery.api.QueryFuture;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.api.QueryMetadata;
import septogeddon.pluginquery.netty.QueryProtocol;
import septogeddon.pluginquery.utils.QueryUtil;

public class QueryConnectionImpl implements QueryConnection {

	private SocketAddress address;
	private QueryMessenger messenger;
	private QueryMetadata metadata = new QueryMetadataImpl();
	private QueryEventBus eventBus = new QueryEventBusImpl();
	private Queue<QueryMessage> queues = new LinkedList<>();
	private Channel channel;
	private QueryProtocol protocol;
	private boolean connecting;
	public QueryConnectionImpl(QueryMessenger messenger, SocketAddress address, Channel channel) {
		this.messenger = messenger;
		this.address = address;
		this.channel = channel;
		this.protocol = new QueryProtocol(messenger, this);
		prepareChannel();
	}
	
	// from constructor
	protected void prepareChannel() {
		if (isConnected()) {
			getChannel().pipeline().addFirst(
					protocol.getHandshaker()
					);
			getMessenger().getPipeline().dispatchActive(this);
			getEventBus().dispatch(this);
			flushQueue();
		}
	}
	
	// from #connect
	protected void prepareConnection() {
		getChannel().pipeline().addLast(
				protocol.getAppender(),
				protocol.getSplitter(),
				protocol.getPipelineInbound(),
				protocol.getDecoder(),
				protocol.getManager(),
				protocol.getEncoder(),
				protocol.getPipelineOutbound()
				);
		getMessenger().getPipeline().dispatchActive(this);
		getEventBus().dispatch(this);
		flushQueue();
	}
	
	@Override
	public boolean isConnecting() {
		return connecting;
	}
	
	@Override
	public QueryMessenger getMessenger() {
		return messenger;
	}
	
	public void handshake(QueryCompletableFuture<QueryConnection> future) {
		ByteBuf buf = getChannel().alloc().heapBuffer();
		buf.writeInt(QueryContext.PACKET_HANDSHAKE.length());
		buf.writeBytes(QueryContext.PACKET_HANDSHAKE.getBytes());
		buf.writeInt(QueryContext.HANDSHAKE_UNIQUE.length());
		buf.writeBytes(QueryContext.HANDSHAKE_UNIQUE.getBytes());
		ChannelFuture fut = getChannel().writeAndFlush(buf);
		fut.addListener((ChannelFuture f)->{
			if (f.isSuccess()) {
				future.complete(this);
				prepareConnection();
			} else {
				future.completeExceptionally(f.cause());
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
		QueryUtil.illegalState(isConnecting(), "already connecting");
		QueryUtil.illegalState(isConnected(), "already connected");
		connecting = true;
		Bootstrap client = new Bootstrap();
		client.group(getMessenger().getEventLoopGroup());
		client.channel(getMessenger().getChannelClass());
		client.option(ChannelOption.SO_KEEPALIVE, true);
		client.option(ChannelOption.TCP_NODELAY, false);
		client.option(ChannelOption.AUTO_READ, true);
		client.handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel arg0) throws Exception {
//				Debug.debug("Channel initialized: "+arg0);
//				QueryConnectionImpl.this.channel = arg0;
//				handshake();
//				prepareChannel();
			}
			
		});
		client.remoteAddress(address);
		ChannelFuture future = client.connect();
		QueryCompletableFuture<QueryConnection> fut = new QueryCompletableFuture<>();
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
				handshake(fut);
			}
		});
		return fut;
	}
	
	public void flushQueue() {
		QueryMessage queue;
		while ((queue = queues.poll()) != null) sendQuery(queue, true);
	}

	@Override
	public void disconnect() {
		QueryUtil.illegalState(!isConnected(), "not connected");
		channel.close();
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
		return channel != null && channel.isOpen();
	}
	
	@Override
	public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message) {
		return sendQuery(channel, message, true);
	}
	
	public QueryFuture<QueryConnection> sendQuery(QueryMessage message, boolean queue) {
		QueryCompletableFuture<QueryConnection> future = new QueryCompletableFuture<>();
		if (isConnected()) {
			ChannelFutureListener futureListener = (ChannelFuture f)->{
				// wrapping a listener, what a shame...
				if (f.isSuccess()) {
					future.complete(this);
				} else {
					f.cause().printStackTrace();
					future.completeExceptionally(f.cause());
				}
			};
			if (channel.eventLoop().inEventLoop()) {
				channel.writeAndFlush(message).addListener(futureListener);
				channel.flush();
			} else {
				channel.eventLoop().submit(()->{
					channel.writeAndFlush(message).addListener(futureListener);
					channel.flush();
				});
			}
		} else if (queue) {
			if (!queues.offer(message)) {
				future.completeExceptionally(new IllegalStateException("failed to queue query"));
			}
		}
		return future;
	}
	@Override
	public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message, boolean queue) {
		return sendQuery(new QueryMessage(channel, message), queue);
	}

	@Override
	public Channel getChannel() {
		return channel;
	}

}
