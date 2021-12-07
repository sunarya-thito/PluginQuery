package septogeddon.pluginquery.api;

import java.util.List;

/**
 * Context
 * @author Thito Yalasatria Sunarya
 *
 */
public interface QueryContext {

    /**
     * The handshake unique string
     */
    String PACKET_HANDSHAKE = "query";

    /**
     * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryLimiter}
     */
    String HANDLER_LIMITER = "query_limiter";
    /**
     * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryEncryptor}
     */
    String HANDLER_ENCRYPTOR = "query_encryptor";
    /**
     * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryDecryptor}
     */
    String HANDLER_DECRYPTOR = "query_decryptor";
    /**
     * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryDeflater}
     */
    String HANDLER_DEFLATER = "query_deflater";
    /**
     * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryInflater}
     */
    String HANDLER_INFLATER = "query_inflater";
    /**
     * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryWhitelist}
     */
    String HANDLER_WHITELIST = "query_whitelist";
    /**
     * The QueryPipeline handler name for {@link septogeddon.pluginquery.channel.QueryThrottle}
     */
    String HANDLER_THROTTLE = "query_throttle";

    /**
     * The Netty pipeline handler name for {@link septogeddon.pluginquery.netty.QueryReadTimeout}
     */
    String PIPELINE_TIMEOUT = "query_timeout";

    /**
     * The PluginMessaging channel namespace used by PluginQuery
     */
    String PLUGIN_MESSAGING_CHANNEL_NAMESPACE = "pluginquery";
    /**
     * The PluginMessaging channel name used by PluginQuery
     */
    String PLUGIN_MESSAGING_CHANNEL_NAME = "query";
    /**
     * The PluginMessaging channel used by PluginQuery
     */
    String REDIRECT_MESSAGING_CHANNEL = PLUGIN_MESSAGING_CHANNEL_NAMESPACE+":redirect";
    /**
     * The PluginMessaging channel used by PluginQuery
     */
    String PLUGIN_MESSAGING_CHANNEL = PLUGIN_MESSAGING_CHANNEL_NAMESPACE + ":" + PLUGIN_MESSAGING_CHANNEL_NAME;
    /**
     * The PluginMessaging sub-channel used by PluginQuery for key synchronizing
     */
    String REQUEST_KEY_SHARE = "keyShare";
    /**
     * The PluginMessaging response sub-channel used by PluginQuery for indicating no permission
     */
    String RESPONSE_NO_PERMISSION = "noPermission";
    /**
     * The PluginMessaging response sub-channel used by PluginQuery for indicating locked state
     */
    String RESPONSE_LOCKED = "locked";
    /**
     * The PluginMessaging response sub-channel used by PluginQuery for indicating success state
     */
    String RESPONSE_SUCCESS = "success";
    /**
     * The PluginMessaging response sub-channel used by PluginQuery for indicating error state
     */
    String RESPONSE_ERROR = "error";

    /**
     * PluginQuery command prefix
     */
    String COMMAND_PREFIX = "&8[&bPluginQuery&8] &7";

    /**
     * PluginQuery administrator command permission
     */
    String ADMIN_PERMISSION = "pluginquery.admin";

    /**
     * Lock option for QueryConfiguration
     */
    QueryConfigurationKey<Boolean> LOCK = QueryConfigurationKey.newBoolean("lock");
    /**
     * IP-Whitelist option for QueryConfiguration
     */
    QueryConfigurationKey<List<String>> IP_WHITELIST = QueryConfigurationKey.newStringList("ip-whitelist");
    /**
     * Connection Throttle option for QueryConfiguration
     */
    QueryConfigurationKey<Number> CONNECTION_THROTTLE = QueryConfigurationKey.newNumber("connection-throttle");
    /**
     * Reconnect Delay option for QueryConfiguration
     */
    QueryConfigurationKey<Number> RECONNECT_DELAY = QueryConfigurationKey.newNumber("reconnect-delay");
    /**
     * Max Reconnect Try option for QueryConfiguration
     */
    QueryConfigurationKey<Number> MAX_RECONNECT_TRY = QueryConfigurationKey.newNumber("max-reconnect-try");
    /**
     * Connection Limit option for QueryConfiguration
     */
    QueryConfigurationKey<Number> CONNECTION_LIMIT = QueryConfigurationKey.newNumber("connection-limit");

    /**
     * Reconnect Handler for QueryConnection failures
     */
    QueryConnectionStateListener RECONNECT_HANDLER = connection -> {
        if (!connection.isConnected()) {
            connection.connect();
        }
    };

    /**
     * The QueryMessaging sub-channel used by PluginQuery for version check
     */
    String COMMAND_VERSION_CHECK = "versionCheck";

    /**
     * Metadata Key for Max Reconnect Try
     */
    QueryMetadataKey<Integer> METAKEY_MAX_RECONNECT_TRY = QueryMetadataKey.newCastableKey("max-reconnect-try", Integer.class);
    /**
     * Metadata Key for Read Timeout
     */
    QueryMetadataKey<Long> METAKEY_READ_TIMEOUT = QueryMetadataKey.newCastableKey("readtimeout", Long.class);
    /**
     * Metadata Key for Reconnect Delay
     */
    QueryMetadataKey<Long> METAKEY_RECONNECT_DELAY = QueryMetadataKey.newCastableKey("reconnect-delay", Long.class);

    /**
     * Preserved channel for {@link org.bukkit.Server} on {@link septogeddon.pluginquery.library.remote.RemoteObject}
     */
    String REMOTEOBJECT_BUKKITSERVER_CHANNEL = "spigotpluginquery:remoteobject:bukkitserver";

    /**
     * Preserved channel for {@link net.md_5.bungee.api.ProxyServer} on {@link septogeddon.pluginquery.library.remote.RemoteObject}
     */
    String REMOTEOBJECT_BUNGEESERVER_CHANNEL = "spigotpluginquery:remoteobject:bungeeserver";

    /**
     * Preserved channel for {@link com.velocitypowered.api.proxy.ProxyServer} on {@link septogeddon.pluginquery.library.remote.RemoteObject}
     */
    String REMOTEOBJECT_VELOCITYSERVER_CHANNEL = "spigotpluginquery:remoteobject:velocityserver";

}
