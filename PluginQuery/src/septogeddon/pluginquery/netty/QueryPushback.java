package septogeddon.pluginquery.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import septogeddon.pluginquery.api.QueryMessenger;

public class QueryPushback extends ChannelInitializer<Channel> {

	private QueryMessenger messenger;
	public QueryPushback(QueryMessenger messenger) {
		this.messenger = messenger;
	}
	@Override
	protected void initChannel(Channel arg0) throws Exception {
		try {
			synchronized (this) {
				arg0.eventLoop().submit(()->messenger.injectConnection(arg0));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	}

}
