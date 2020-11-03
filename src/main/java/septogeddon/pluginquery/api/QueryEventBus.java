package septogeddon.pluginquery.api;

/**
 * Handle and manage incoming events
 * @author Thito Yalasatria Sunarya
 *
 */
public interface QueryEventBus {

    /**
     * Add parent that listen to this event manager
     * @param eventBus
     */
    void addParent(QueryEventBus eventBus);

    /**
     * Remove a parent, become an orphan
     * @param eventBus
     */
    void removeParent(QueryEventBus eventBus);

    /**
     * Dispatch a Connection State change event
     * @param connection
     */
    void dispatchConnectionState(QueryConnection connection);

    /**
     * Dispatch a Query Message received event
     * @param connection
     * @param channel
     * @param message
     */
    void dispatchMessage(QueryConnection connection, String channel, byte[] message);

    /**
     * Register a listener
     * @param listener
     */
    void registerListener(QueryListener listener);

    /**
     * Unregister a listener
     * @param listener
     */
    void unregisterListener(QueryListener listener);

    /**
     * Register a message listener
     * @param listener
     */
    default void registerListener(QueryMessageListener listener) {
        this.registerListener((QueryListener) listener);
    }

    /**
     * Register a connection state change listener
     * @param listener
     */
    default void registerListener(QueryConnectionStateListener listener) {
        this.registerListener((QueryListener) listener);
    }

}
