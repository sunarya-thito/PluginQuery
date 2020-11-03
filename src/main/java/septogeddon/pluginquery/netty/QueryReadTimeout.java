package septogeddon.pluginquery.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import septogeddon.pluginquery.api.QueryConnection;

import java.util.concurrent.TimeUnit;

public class QueryReadTimeout extends ReadTimeoutHandler {

    private final QueryConnection conn;

    public QueryReadTimeout(QueryConnection conn, long timeout, TimeUnit unit) {
        super(timeout, unit);
        this.conn = conn;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        conn.disconnect();
        super.channelInactive(ctx);
    }

}
