package septogeddon.pluginquery.api;

import java.util.List;

public interface QueryContext {

	public static final String PACKET_HANDSHAKE = "query";
	
	public static final String HANDSHAKE_UNIQUE = "handshake";
	
	public static final String HANDLER_LIMITER = "query_limiter";
	public static final String HANDLER_ENCRYPTOR = "query_encryptor";
	public static final String HANDLER_DECRYPTOR = "query_decryptor";
	public static final String HANDLER_DEFLATER = "query_deflater";
	public static final String HANDLER_INFLATER = "query_inflater";
	public static final String HANDLER_WHITELIST = "query_whitelist";
	public static final String HANDLER_THROTTLE = "query_throttle";
	
	public static final String PIPELINE_TIMEOUT = "query_timeout";
	
	public static final String PLUGIN_MESSAGING_CHANNEL = "pluginquery:query";
	public static final String REQUEST_KEY_SHARE = "keyShare";
	public static final String RESPONSE_NO_PERMISSION = "noPermission";
	public static final String RESPONSE_LOCKED = "locked";
	public static final String RESPONSE_SUCCESS = "success";
	public static final String RESPONSE_ERROR = "error";
	
	public static final String COMMAND_PREFIX = "&8[&bPluginQuery&8] &7";
	
	public static final String ADMIN_PERMISSION = "pluginquery.admin";
	
	public static final QueryConfigurationKey<Boolean> LOCK = QueryConfigurationKey.newBoolean("lock");
	public static final QueryConfigurationKey<List<String>> IP_WHITELIST = QueryConfigurationKey.newStringList("ip-whitelist");
	public static final QueryConfigurationKey<Number> CONNECTION_THROTTLE = QueryConfigurationKey.newNumber("connection-throttle");
	public static final QueryConfigurationKey<Number> RECONNECT_DELAY = QueryConfigurationKey.newNumber("reconnect-delay");
	public static final QueryConfigurationKey<Number> MAX_RECONNECT_TRY = QueryConfigurationKey.newNumber("max-reconnect-try");
	public static final QueryConfigurationKey<Number> CONNECTION_LIMIT = QueryConfigurationKey.newNumber("connection-limit");
	
	public static final QueryConnectionStateListener RECONNECT_HANDLER = connection->{
		if (!connection.isConnected()) {
			connection.connect();
		}
	};
	
	public static final String COMMAND_VERSION_CHECK = "versionCheck";
	
	public static final QueryMetadataKey<Integer> METAKEY_MAX_RECONNECT_TRY = QueryMetadataKey.newCastableKey("max-reconnect-try", Integer.class);
	public static final QueryMetadataKey<Long> METAKEY_READ_TIMEOUT = QueryMetadataKey.newCastableKey("readtimeout", Long.class);
	public static final QueryMetadataKey<Long> METAKEY_RECONNECT_DELAY = QueryMetadataKey.newCastableKey("reconnect-delay", Long.class);
	
}
