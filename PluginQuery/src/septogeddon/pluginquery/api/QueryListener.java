package septogeddon.pluginquery.api;

public interface QueryListener {

	/***
	 * Called when {@link QueryConnection#isConnected()} value changed
	 * @param connection
	 */
	public void onConnectionStateChange(QueryConnection connection);
	/***
	 * Called when the connection received a query message
	 * @param connection
	 * @param channel
	 * @param message
	 */
	public void onQueryReceived(QueryConnection connection, String channel, byte[] message);
	
}
