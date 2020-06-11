package septogeddon.pluginquery.netty;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import septogeddon.pluginquery.QueryCompletableFuture;
import septogeddon.pluginquery.QueryConnectionImpl;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

public class QueryHandshaker extends ChannelDuplexHandler {

	protected QueryProtocol protocol;
	protected QueryCompletableFuture<QueryConnection> future;
	public QueryHandshaker(QueryProtocol protocol, QueryCompletableFuture<QueryConnection> future) {
		this.protocol = protocol;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof ByteBuf) {
			ChannelPipeline pipe = ctx.channel().pipeline();
			ByteBuf buf = (ByteBuf)msg;
			buf.markReaderIndex();
			try {
				int length = buf.readInt();
				// prevent overflow byte read
				if (length == QueryContext.PACKET_HANDSHAKE.length()) {
					byte[] bytes = new byte[length];
					buf.readBytes(bytes);
					if (new String(bytes).equals(QueryContext.PACKET_HANDSHAKE)) {
						// SERVER SIDE HANDSHAKE HANDLING
						long most = buf.readLong();
						long least = buf.readLong();
						String uuid = new UUID(most, least).toString();
						length = buf.readInt();
						bytes = new byte[length];
						buf.readBytes(bytes);
						boolean response = buf.readBoolean();
						try {
							bytes = protocol.getMessenger().getPipeline().dispatchReceiving(protocol.getConnection(), bytes);
							if (new String(bytes).equals(uuid)) {
								// remove minecraft packet handlers and this handler
								// in this process, read timeout also removed
								// we don't use read timeout, keep it open as long as possible
								pipe.forEach(entry->pipe.remove(entry.getKey()));
								// initialize query channel
								if (future != null) {
									future.complete(protocol.getConnection());
								}
								QueryConnectionImpl.handshakenConnection(protocol, pipe);
								if (response) {
									ByteBuf buffer = ctx.alloc().directBuffer();
									buffer.writeInt(QueryContext.PACKET_HANDSHAKE.length());
									buffer.writeBytes(QueryContext.PACKET_HANDSHAKE.getBytes());
									buffer.writeLong(most);
									buffer.writeLong(least);
									buffer.writeInt(bytes.length);
									buffer.writeBytes(bytes);
									buffer.writeBoolean(false);
									ctx.writeAndFlush(buffer).addListener((ChannelFuture f)->{
										if (!f.isSuccess()) {
											f.channel().close();
										}
									});
								}
								
							} else {
								throw new IllegalArgumentException("invalid encryption");
							}
						} catch (Throwable t) {
							ctx.channel().close();
						}
						return;
					}
				}
			} catch (Throwable t) {
			}
			buf.resetReaderIndex();
			pipe.remove(this);
		}
		super.channelRead(ctx, msg);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		protocol.getConnection().disconnect();
	}
	
}
