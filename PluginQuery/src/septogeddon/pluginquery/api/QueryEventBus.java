package septogeddon.pluginquery.api;

/***
 * Handle and manage incoming events
 * @author Thito Yalasatria Sunarya
 *
 */
public interface QueryEventBus {

	/***
	 * Add parent that listen to this event manager
	 * @param eventBus
	 */
	public void addParent(QueryEventBus eventBus);
	/***
	 * Remove a parent, become an orphan
	 * @param eventBus
	 */
	public void removeParent(QueryEventBus eventBus);
	/***
	 * Dispatch a Connection State change event
	 * @param connection
	 */
	public void dispatchConnectionState(QueryConnection connection);
	/***
	 * Dispatch a Query Message received event
	 * @param connection
	 * @param channel
	 * @param message
	 */
	public void dispatchMessage(QueryConnection connection, String channel, byte[] message);
	/***
	 * Register a listener
	 * @param listener
	 */
	public void registerListener(QueryListener listener);
	/***
	 * Unregister a listener
	 * @param listener
	 */
	public void unregisterListener(QueryListener listener);
	/***
	 * Register a message listener
	 * @param listener
	 */
	public default void registerListener(QueryMessageListener listener) {
		this.registerListener((QueryListener)listener);
	}
	/***
	 * Register a connection state change listener
	 * @param listener
	 */
	public default void registerListener(QueryConnectionStateListener listener) {
		this.registerListener((QueryListener)listener);
	}
	
}
