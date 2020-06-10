package septogeddon.pluginquery.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryPipeline;

public class QueryPipelineOutbound extends MessageToByteEncoder<ByteBuf> {

	private QueryPipeline pipe;
	private QueryConnection connection;
	public QueryPipelineOutbound(QueryPipeline pipeline, QueryConnection connection) {
		this.pipe = pipeline;
		this.connection = connection;
	}
	@Override
	protected void encode(ChannelHandlerContext arg0, ByteBuf input, ByteBuf output) throws Exception {
		byte[] bytes = new byte[input.readableBytes()];
		input.readBytes(bytes, 0, bytes.length);
		bytes = pipe.dispatchSending(connection, bytes);
		output.writeBytes(bytes, 0, bytes.length);
	}

}
