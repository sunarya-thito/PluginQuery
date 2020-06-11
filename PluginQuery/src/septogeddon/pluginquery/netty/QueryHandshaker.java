package septogeddon.pluginquery.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import septogeddon.pluginquery.QueryConnectionImpl;
import septogeddon.pluginquery.api.QueryContext;

public class QueryHandshaker extends ChannelDuplexHandler {

	protected QueryProtocol protocol;
	public QueryHandshaker(QueryProtocol protocol) {
		this.protocol = protocol;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		boolean flag = false;
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
						length = buf.readInt();
						bytes = new byte[length];
						buf.readBytes(bytes);
						try {
							bytes = protocol.getMessenger().getPipeline().dispatchReceiving(protocol.getConnection(), bytes);
							if (new String(bytes).equals(QueryContext.HANDSHAKE_UNIQUE)) {
								// remove minecraft packet handlers and this handler
								// in this process, read timeout also removed
								// we don't use read timeout, keep it open as long as possible
								pipe.forEach(entry->pipe.remove(entry.getKey()));
								// initialize query channel
								QueryConnectionImpl.handshakenConnection(protocol, pipe);
								return;
							}
						} catch (Throwable t) {
							flag = true;
						}
					} else {
						flag = true;
					}
				}
			} catch (Throwable t) {
				flag = true;
			}
			if (flag) {
				buf.resetReaderIndex();
				pipe.remove(this);
			}
		}
		super.channelRead(ctx, msg);
	}
	
}
