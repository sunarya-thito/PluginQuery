package septogeddon.pluginquery.api;

import java.util.List;

/***
 * Context
 * @author Thito Yalasatria Sunarya
 *
 */
public interface QueryContext {

	/***
	 * The handshake unique string
	 */
	public static final String PACKET_HANDSHAKE = "query";

	/***
	 * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryLimiter}
	 */
	public static final String HANDLER_LIMITER = "query_limiter";
	/***
	 * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryEncryptor}
	 */
	public static final String HANDLER_ENCRYPTOR = "query_encryptor";
	/***
	 * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryDecryptor}
	 */
	public static final String HANDLER_DECRYPTOR = "query_decryptor";
	/***
	 * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryDeflater}
	 */
	public static final String HANDLER_DEFLATER = "query_deflater";
	/***
	 * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryInflater}
	 */
	public static final String HANDLER_INFLATER = "query_inflater";
	/***
	 * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryWhitelist}
	 */
	public static final String HANDLER_WHITELIST = "query_whitelist";
	/***
	 * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryThrottle}
	 */
	public static final String HANDLER_THROTTLE = "query_throttle";
	
	/***
	 * The Netty pipeline handler name for {@link septogeddon.pluginquery.netty.QueryReadTimeout}
	 */
	public static final String PIPELINE_TIMEOUT = "query_timeout";

	/***
	 * The PluginMessaging channel namespace used by PluginQuery
	 */
	public static final String PLUGIN_MESSAGING_CHANNEL_NAMESPACE = "pluginquery";
	/***
	 * The PluginMessaging channel name used by PluginQuery
	 */
	public static final String PLUGIN_MESSAGING_CHANNEL_NAME = "query";
	/***
	 * The PluginMessaging channel used by PluginQuery
	 */
	public static final String PLUGIN_MESSAGING_CHANNEL = PLUGIN_MESSAGING_CHANNEL_NAMESPACE + ":" + PLUGIN_MESSAGING_CHANNEL_NAME;
	/***
	 * The PluginMessaging sub-channel used by PluginQuery for key synchronizing
	 */
	public static final String REQUEST_KEY_SHARE = "keyShare";
	/***
	 * The PluginMessaging response sub-channel used by PluginQuery for indicating no permission
	 */
	public static final String RESPONSE_NO_PERMISSION = "noPermission";
	/***
	 * The PluginMessaging response sub-channel used by PluginQuery for indicating locked state
	 */
	public static final String RESPONSE_LOCKED = "locked";
	/***
	 * The PluginMessaging response sub-channel used by PluginQuery for indicating success state
	 */
	public static final String RESPONSE_SUCCESS = "success";
	/***
	 * The PluginMessaging response sub-channel used by PluginQuery for indicating error state
	 */
	public static final String RESPONSE_ERROR = "error";
	
	/***
	 * PluginQuery command prefix
	 */
	public static final String COMMAND_PREFIX = "&8[&bPluginQuery&8] &7";
	
	/***
	 * PluginQuery administrator command permission
	 */
	public static final String ADMIN_PERMISSION = "pluginquery.admin";
	
	/***
	 * Lock option for QueryConfiguration
	 */
	public static final QueryConfigurationKey<Boolean> LOCK = QueryConfigurationKey.newBoolean("lock");/***
	 * IP-Whitelist option for QueryConfiguration
	 */
	public static final QueryConfigurationKey<List<String>> IP_WHITELIST = QueryConfigurationKey.newStringList("ip-whitelist");
	/***
	 * Connection Throttle option for QueryConfiguration
	 */
	public static final QueryConfigurationKey<Number> CONNECTION_THROTTLE = QueryConfigurationKey.newNumber("connection-throttle");
	/***
	 * Reconnect Delay option for QueryConfiguration
	 */
	public static final QueryConfigurationKey<Number> RECONNECT_DELAY = QueryConfigurationKey.newNumber("reconnect-delay");
	/***
	 * Max Reconnect Try option for QueryConfiguration
	 */
	public static final QueryConfigurationKey<Number> MAX_RECONNECT_TRY = QueryConfigurationKey.newNumber("max-reconnect-try");
	/***
	 * Connection Limit option for QueryConfiguration
	 */
	public static final QueryConfigurationKey<Number> CONNECTION_LIMIT = QueryConfigurationKey.newNumber("connection-limit");
	
	/***
	 * Reconnect Handler for QueryConnection failures
	 */
	public static final QueryConnectionStateListener RECONNECT_HANDLER = connection->{
		if (!connection.isConnected()) {
			connection.connect();
		}
	};
	
	/***
	 * The QueryMessaging sub-channel used by PluginQuery for version check
	 */
	public static final String COMMAND_VERSION_CHECK = "versionCheck";
	
	/***
	 * Metadata Key for Max Reconnect Try
	 */
	public static final QueryMetadataKey<Integer> METAKEY_MAX_RECONNECT_TRY = QueryMetadataKey.newCastableKey("max-reconnect-try", Integer.class);
	/***
	 * Metadata Key for Read Timeout
	 */
	public static final QueryMetadataKey<Long> METAKEY_READ_TIMEOUT = QueryMetadataKey.newCastableKey("readtimeout", Long.class);
	/***
	 * Metadata Key for Reconnect Delay
	 */
	public static final QueryMetadataKey<Long> METAKEY_RECONNECT_DELAY = QueryMetadataKey.newCastableKey("reconnect-delay", Long.class);
	
}
