package septogeddon.pluginquery;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryEventBus;
import septogeddon.pluginquery.api.QueryListener;
import septogeddon.pluginquery.message.QueryGetActiveConnections;
import septogeddon.pluginquery.message.QueryObject;
import septogeddon.pluginquery.message.QuerySendActiveConnections;
import septogeddon.pluginquery.utils.QueryUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class QueryEventBusImpl implements QueryEventBus {

    private final Set<QueryListener> listeners = ConcurrentHashMap.newKeySet();
    private final Set<QueryEventBus> parents = ConcurrentHashMap.newKeySet();

    @Override
    public void registerListener(QueryListener listener) {
        QueryUtil.nonNull(listener, "listener");
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(QueryListener listener) {
        QueryUtil.nonNull(listener, "listener");
        listeners.remove(listener);
    }

    @Override
    public void addParent(QueryEventBus eventBus) {
        QueryUtil.nonNull(eventBus, "eventBus");
        parents.add(eventBus);
    }

    @Override
    public void removeParent(QueryEventBus eventBus) {
        QueryUtil.nonNull(eventBus, "eventBus");
        parents.remove(eventBus);
    }

    @Override
    public void dispatchMessage(QueryConnection connection, String channel, byte[] message) {
        for (QueryListener listener : listeners) {
            try {
                listener.onQueryReceived(connection, channel, message);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        for (QueryEventBus parent : parents) {
            parent.dispatchMessage(connection, channel, message);
        }
    }

    @Override
    public void dispatchConnectionState(QueryConnection connection) {
        for (QueryListener listener : listeners) {
            try {
                listener.onConnectionStateChange(connection);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        for (QueryEventBus parent : parents) {
            parent.dispatchConnectionState(connection);
        }
    }

}
