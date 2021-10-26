package septogeddon.pluginquery.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import septogeddon.pluginquery.QueryMessage;
import septogeddon.pluginquery.utils.Debug;

public class QueryManager extends SimpleChannelInboundHandler<QueryMessage> {

    private final QueryProtocol protocol;

    public QueryManager(QueryProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext arg0, QueryMessage arg1) throws Exception {
        Debug.debug("Manager: RECEIVED: " + arg1.getChannel());
        protocol.getConnection().getEventBus().dispatchMessage(protocol.getConnection(), arg1.getChannel(), arg1.getMessage());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        protocol.getConnection().disconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    }

}
