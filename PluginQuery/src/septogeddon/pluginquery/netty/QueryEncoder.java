package septogeddon.pluginquery.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import septogeddon.pluginquery.QueryMessage;
import septogeddon.pluginquery.utils.Debug;

public class QueryEncoder extends MessageToByteEncoder<QueryMessage> {

	@Override
	protected void encode(ChannelHandlerContext arg0, QueryMessage arg1, ByteBuf arg2) throws Exception {
		Debug.debug(()->"Encoder: BEGIN");
		arg2.writeByte(arg1.getChannel().length());
		arg2.writeBytes(arg1.getChannel().getBytes());
		Debug.debug(()->"Encoder: ENCODE: "+arg1.getChannel());
		arg2.writeInt(arg1.getMessage().length);
		arg2.writeBytes(arg1.getMessage());
		Debug.debug(()->"Encoder: END");
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	}
	
}
