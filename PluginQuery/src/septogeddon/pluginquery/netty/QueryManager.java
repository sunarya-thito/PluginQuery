package septogeddon.pluginquery.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import septogeddon.pluginquery.QueryMessage;

public class QueryManager extends SimpleChannelInboundHandler<QueryMessage> {

	private QueryProtocol protocol;
	public QueryManager(QueryProtocol protocol) {
		this.protocol = protocol;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext arg0, QueryMessage arg1) throws Exception {
		protocol.getConnection().getEventBus().dispatch(protocol.getConnection(), arg1.getChannel(), arg1.getMessage());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		// channel disconnected
		protocol.getMessenger().getPipeline().dispatchInactive(protocol.getConnection());
		protocol.getConnection().getEventBus().dispatch(protocol.getConnection());
		// side note:
		// "channel connected" handling
		// is at QueryConnection#prepareChannel
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	}

}
