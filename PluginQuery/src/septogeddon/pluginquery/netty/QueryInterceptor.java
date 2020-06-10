package septogeddon.pluginquery.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import septogeddon.pluginquery.api.QueryMessenger;

public class QueryInterceptor extends ChannelInboundHandlerAdapter {

	private QueryInitiator initiator;
	
	public QueryInterceptor(QueryMessenger messenger) {
		initiator = new QueryInitiator(messenger);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// always instance of Channel actually
		// but who knows the previous handler
		// has been hijacked
		if (msg instanceof Channel) {
			// represent the client channel connection
			Channel channel = (Channel)msg;
			// add a channel to handler pushback control
			channel.pipeline().addFirst(initiator);
		}
		super.channelRead(ctx, msg);
	}
	
}
