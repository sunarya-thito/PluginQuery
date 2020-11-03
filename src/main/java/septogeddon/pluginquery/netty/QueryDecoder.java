package septogeddon.pluginquery.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import septogeddon.pluginquery.QueryMessage;
import septogeddon.pluginquery.utils.Debug;

import java.util.List;

public class QueryDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext arg0, ByteBuf arg1, List<Object> arg2) throws Exception {
        if (arg1.readableBytes() <= 0) return;
        Debug.debug(() -> "Decoder: BEGIN");
        int length = arg1.readByte();
        byte[] buf = new byte[length];
        arg1.readBytes(buf);
        // ignoring charset
        // channel name shouldn't be emoji wtf?
        String channel = new String(buf);
        Debug.debug(() -> "Decoder: IDENTIFIED: " + channel);
        length = arg1.readInt();
        byte[] message = new byte[length];
        Debug.debug(() -> "Decoder: LENGTH: " + message.length);
        arg1.readBytes(message);
        QueryMessage query = new QueryMessage(channel, message);
        arg2.add(query);
        Debug.debug(() -> "Decoder: END");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    }

}
