package septogeddon.pluginquery.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import septogeddon.pluginquery.api.QueryMessenger;

public class QueryInitiator extends ChannelInitializer<Channel> {

    private final QueryPushback pushback;

    public QueryInitiator(QueryMessenger messenger) {
        pushback = new QueryPushback(messenger);
    }

    @Override
    protected void initChannel(Channel arg0) throws Exception {
        arg0.pipeline().addLast("query_pushback", pushback);
    }

}
