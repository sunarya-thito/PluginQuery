package septogeddon.pluginquery.channel;

import septogeddon.pluginquery.QueryChannelHandler;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueryLimiter extends QueryChannelHandler {

    private final Map<SocketAddress, Integer> connected = new ConcurrentHashMap<>();
    private final int limit;

    public QueryLimiter(int limit) {
        super(QueryContext.HANDLER_LIMITER);
        this.limit = limit;
    }

    @Override
    public void onActive(QueryConnection connection) throws Exception {
        if (limit >= 0) {
            int score = connected.getOrDefault(connection.getAddress(), 0);
            if (score >= limit) {
                connection.disconnect();
                return;
            }
            connected.put(connection.getAddress(), score + 1);
        }
        super.onActive(connection);
    }

    @Override
    public void onInactive(QueryConnection connection) throws Exception {
        Integer score = connected.remove(connection.getAddress());
        if (score != null && score > 1) {
            connected.put(connection.getAddress(), score - 1);
        }
        super.onInactive(connection);
    }

}
