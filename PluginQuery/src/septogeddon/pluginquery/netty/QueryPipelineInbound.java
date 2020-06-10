package septogeddon.pluginquery.netty;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryPipeline;

public class QueryPipelineInbound extends ByteToMessageDecoder {

	private QueryPipeline pipeline;
	private QueryConnection connection;
	public QueryPipelineInbound(QueryPipeline pipeline, QueryConnection connection) {
		this.pipeline = pipeline;
		this.connection = connection;
	}
	@Override
	protected void decode(ChannelHandlerContext arg0, ByteBuf arg1, List<Object> arg2) throws Exception {
		byte[] bytes = new byte[arg1.readableBytes()];
		arg1.readBytes(bytes, 0, bytes.length);
		bytes = pipeline.dispatchReceiving(connection, bytes);
		ByteBuf buf = arg0.alloc().heapBuffer(bytes.length);
		buf.writeBytes(bytes);
		arg2.add(buf);
	}

}
