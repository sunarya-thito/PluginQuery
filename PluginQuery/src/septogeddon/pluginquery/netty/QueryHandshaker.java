package septogeddon.pluginquery.netty;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import septogeddon.pluginquery.QueryCompletableFuture;
import septogeddon.pluginquery.QueryConnectionImpl;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.utils.QueryUtil;

public class QueryHandshaker extends ChannelInboundHandlerAdapter {

	protected QueryProtocol protocol;
	protected QueryCompletableFuture<QueryConnection> future;
	public QueryHandshaker(QueryProtocol protocol, QueryCompletableFuture<QueryConnection> future) {
		this.protocol = protocol;
	}	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ChannelPipeline pipe = ctx.channel().pipeline();
		if (msg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf)msg;
			buf.markReaderIndex();
			try {
				byte length = buf.readByte();
				// prevent overflow byte read
				if (length == QueryContext.PACKET_HANDSHAKE.length()) {
					// read "query"
					byte[] bytes = new byte[length];
					buf.readBytes(bytes);
					if (new String(bytes).equals(QueryContext.PACKET_HANDSHAKE)) {
						// read UUID
						long most = buf.readLong();
						long least = buf.readLong();
						String uuid = new UUID(most, least).toString();
						// read encrypted UUID
						length = buf.readByte();
						bytes = new byte[length];
						buf.readBytes(bytes);
						boolean response = buf.readBoolean();
						try {
							byte[] encryptedUUID = bytes;
							// decrypt UUID
							bytes = protocol.getMessenger().getPipeline().dispatchReceiving(protocol.getConnection(), bytes);
							QueryUtil.nonNull(bytes, "unique handshake token");
							// match the decrypted UUID with the UUID
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
									
									// send "query"
									buffer.writeByte((byte)QueryContext.PACKET_HANDSHAKE.length());
									buffer.writeBytes(QueryContext.PACKET_HANDSHAKE.getBytes());
									// send UUID
									buffer.writeLong(most);
									buffer.writeLong(least);
									// send encrypted UUID
									buffer.writeByte((byte)encryptedUUID.length);
									buffer.writeBytes(encryptedUUID);
									// don't ask to response, otherwise, it will create infinite loop
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
						buf.release();
						return;
					} 
				}
			} catch (Throwable t) {
			}
			buf.resetReaderIndex();
		}
		try {
			pipe.remove(this);
			pipe.remove(QueryContext.PIPELINE_TIMEOUT);
		} catch (Throwable t) {
		}
		ctx.fireChannelRead(msg);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		protocol.getConnection().disconnect();
	}
	
}
