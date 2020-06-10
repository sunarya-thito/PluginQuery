package septogeddon.pluginquery.api;

public interface QueryConnectionStateListener extends QueryListener {

	public default void onQueryReceived(QueryConnection connection, String channel, byte[] message) {}
	
}
