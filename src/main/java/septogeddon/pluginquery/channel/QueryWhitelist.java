package septogeddon.pluginquery.channel;

import septogeddon.pluginquery.QueryChannelHandler;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

import java.net.InetSocketAddress;
import java.util.List;

public class QueryWhitelist extends QueryChannelHandler {

    private final List<String> whitelist;

    public QueryWhitelist(List<String> whitelist) {
        super(QueryContext.HANDLER_WHITELIST);
        this.whitelist = whitelist;
    }

    @Override
    public void onActive(QueryConnection connection) throws Exception {
        if (connection.getAddress() instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress) connection.getAddress();
            if (!whitelist.contains(address.getHostName())) {
                connection.disconnect();
                return;
            }
        }
        super.onActive(connection);
    }

}
