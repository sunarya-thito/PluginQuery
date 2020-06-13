package septogeddon.pluginquery;

import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.utils.QueryUtil;

public class PluginQuery {

	private static QueryMessenger messengerInstance;
	
	/***
	 * Get the {@link QueryMessenger} instance
	 * @return
	 */
	@SuppressWarnings("all")
	public static QueryMessenger getMessenger() {
		return messengerInstance;
	}
	
	/***
	 * Set the {@link QueryMessenger} instance. Will throw an exception if the instance is already set.
	 * @param messenger
	 */
	public static void setMessenger(QueryMessenger messenger) {
		QueryUtil.illegalState(messengerInstance != null, "instance already set");
		messengerInstance = messenger;
	}
	
	/***
	 * Initialize PluginQuery with default {@link QueryMessenger}. Will not set if there is already an instance.
	 */
	public static void initializeDefaultMessenger() {
		if (getMessenger() != null) return;
		setMessenger(new QueryMessengerImpl());
	}
	
}
