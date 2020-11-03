package septogeddon.pluginquery.library.remote;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryListener;
import septogeddon.pluginquery.api.QueryMessenger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Provide Object to all active connections
 * @author Thito Yalasatria Sunarya
 *
 * @param <T>
 */
public class RemoteObjectProvider<T> {

    private final String channel;
    private Class<T> clazz;
    private final T object;
    private ExecutorService service;
    private long futureTimeout = 1000 * 30;
    private boolean queueQuery = true;
    private final Map<QueryConnection, RemoteObject<T>> remoteObject = new ConcurrentHashMap<>();
    private final QueryMessenger messenger;
    private final RemoteListener listener = new RemoteListener();
    private ClassRegistry registry = new ClassRegistry();

    /**
     * Initialize RemoteObjectProvider for specified channel and messenger
     * @param messenger the messenger
     * @param channel the channel
     * @param object the object
     */
    public RemoteObjectProvider(QueryMessenger messenger, String channel, T object) {
        this.object = object;
        this.channel = channel;
        this.messenger = messenger;
        prepare();
    }

    /**
     * Get the current class registry for this provider
     * @return
     */
    public ClassRegistry getRegistry() {
        return registry;
    }

    /**
     * Set class registry for this provider.
     * @param registry the class registry
     */
    public void setRegistry(ClassRegistry registry) {
        this.registry = registry;
        remoteObject.values().forEach(remote -> {
            remote.classRegistry = registry;
        });
    }

    protected void prepare() {
        messenger.getEventBus().registerListener(listener);
    }

    /**
     * Get the messenger
     * @return messenger
     */
    public QueryMessenger getMessenger() {
        return messenger;
    }

    /**
     * Set the ThreadPool handler for this provider
     * @param service
     */
    public void setExecutorService(ExecutorService service) {
        this.service = service;
        remoteObject.values().forEach(remote -> {
            remote.executorService = service;
        });
    }

    /**
     * Get future timeout for this provider
     * @return future timeout time
     */
    public long getFutureTimeout() {
        return futureTimeout;
    }

    /**
     * Set future timeout for this handler
     * @param futureTimeout
     */
    public void setFutureTimeout(long futureTimeout) {
        this.futureTimeout = futureTimeout;
        remoteObject.values().forEach(remote -> {
            remote.futureTimeout = futureTimeout;
        });
    }

    /**
     * Get queue option for this provider
     * @return true if its queue-enabled
     */
    public boolean isQueueQuery() {
        return queueQuery;
    }

    /**
     * Set whether the query should queue when the connection goes inactive
     * @param queueQuery
     */
    public void setQueueQuery(boolean queueQuery) {
        this.queueQuery = queueQuery;
        remoteObject.values().forEach(remote -> {
            remote.setQueueQuery(queueQuery);
        });
    }

    class RemoteListener implements QueryListener {

        @Override
        public void onConnectionStateChange(QueryConnection connection) throws Throwable {
            if (connection.isConnected() && !remoteObject.containsKey(connection)) {
                RemoteObject<T> remote;
                remoteObject.put(connection, remote =
                        clazz == null ? new RemoteObject<T>(channel, connection, object, registry) :
                                new RemoteObject<T>(channel, connection, clazz, registry));
                remote.setExecutorService(service);
                remote.setFutureTimeout(futureTimeout);
                remote.setQueueQuery(queueQuery);
            }
        }

        @Override
        public void onQueryReceived(QueryConnection connection, String channel, byte[] message) throws Throwable {
        }

    }

}
