package septogeddon.pluginquery.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import septogeddon.pluginquery.api.QueryMessenger;

public class QueryPushback extends ChannelInitializer<Channel> {

    public static Object lock = new Object();
    private final QueryMessenger messenger;

    public QueryPushback(QueryMessenger messenger) {
        this.messenger = messenger;
    }

    @Override
    protected void initChannel(Channel arg0) {
        try {
            synchronized (this) {
                arg0.eventLoop().submit(() -> {
                    messenger.injectConnection(arg0);
                    arg0.pipeline().addFirst(new HttpQueryHandler());
                });
            }
        } catch (Throwable t) {
        }
    }

}
