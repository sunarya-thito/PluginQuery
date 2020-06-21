package septogeddon.pluginquery.netty;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import septogeddon.pluginquery.QueryMessage;

public class QueryDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext arg0, ByteBuf arg1, List<Object> arg2) throws Exception {
		if (arg1.readableBytes() <= 0) return;
		int length = arg1.readByte();
		byte[] buf = new byte[length];
		arg1.readBytes(buf);
		// ignoring charset
		// channel name shouldn't be emoji wtf?
		String channel = new String(buf);
		length = arg1.readInt();
		byte[] message = new byte[length];
		arg1.readBytes(message);
		QueryMessage query = new QueryMessage(channel, message);
		arg2.add(query);
		byte[] remains = new byte[arg1.readableBytes()];
		arg1.readBytes(remains);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	}
	
}
