package septogeddon.pluginquery;

import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.utils.QueryUtil;

public class PluginQuery {

	private static QueryMessenger messengerInstance;
	
	@SuppressWarnings("all")
	public static QueryMessenger getMessenger() {
		return messengerInstance;
	}
	
	public static void setMessenger(QueryMessenger messenger) {
		QueryUtil.illegalState(messengerInstance != null, "instance already set");
		messengerInstance = messenger;
	}
	
	public static void initializeDefaultMessenger() {
		setMessenger(new QueryMessengerImpl());
	}
	
}
