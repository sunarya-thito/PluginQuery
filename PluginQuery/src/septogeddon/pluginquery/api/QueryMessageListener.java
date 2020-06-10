package septogeddon.pluginquery.api;

public interface QueryMessageListener extends QueryListener {

	public default void onConnectionStateChange(QueryConnection connection) {};
	
}
