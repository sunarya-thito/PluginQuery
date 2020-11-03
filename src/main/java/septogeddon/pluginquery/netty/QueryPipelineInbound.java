package septogeddon.pluginquery.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryPipeline;

import java.util.List;

public class QueryPipelineInbound extends ByteToMessageDecoder {

    private final QueryPipeline pipeline;
    private final QueryConnection connection;

    public QueryPipelineInbound(QueryPipeline pipeline, QueryConnection connection) {
        this.pipeline = pipeline;
        this.connection = connection;
    }

    @Override
    protected void decode(ChannelHandlerContext arg0, ByteBuf arg1, List<Object> arg2) throws Exception {
        if (arg1.readableBytes() <= 0) return;
        byte[] bytes = new byte[arg1.readableBytes()];
        arg1.readBytes(bytes, 0, bytes.length);
        bytes = pipeline.dispatchReceiving(connection, bytes);
        ByteBuf buf = arg0.alloc().heapBuffer(bytes.length);
        buf.writeBytes(bytes);
        arg2.add(buf);
    }

}
