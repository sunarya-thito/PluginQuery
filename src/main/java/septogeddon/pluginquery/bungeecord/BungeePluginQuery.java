package septogeddon.pluginquery.bungeecord;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.YamlQueryConfiguration;
import septogeddon.pluginquery.api.*;
import septogeddon.pluginquery.bungeecord.event.QueryMessageEvent;
import septogeddon.pluginquery.channel.*;
import septogeddon.pluginquery.utils.DataBuffer;
import septogeddon.pluginquery.utils.EncryptionToolkit;

import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class BungeePluginQuery extends Plugin implements Listener, QueryListener {

    /**
     * Metadata Key for {@link net.md_5.bungee.api.config.ServerInfo}
     */
    public static final QueryMetadataKey<ServerInfo> SERVER_INFO = QueryMetadataKey.newCastableKey("bungeeserverinfo", ServerInfo.class);
    /**
     * Metadata for {@link net.md_5.bungee.api.ProxyServer} on {@link septogeddon.pluginquery.library.remote.RemoteObject}
     */
    public static final QueryMetadataKey<BungeeRemoteObjectMessenger> REMOTEOBJECT_PROXYSERVER = QueryMetadataKey.newCastableKey(QueryContext.REMOTEOBJECT_BUNGEESERVER_CHANNEL, BungeeRemoteObjectMessenger.class);
    private static final QueryMetadataKey<Integer> RECONNECT_TRY_TIMES = QueryMetadataKey.newCastableKey("bungeepluginquery_retry_times", Integer.class);
    private final YamlQueryConfiguration config = new YamlQueryConfiguration();
    private EncryptionToolkit encryption;
    private boolean disabling = false;

    /**
     * Get active connection for a {@link net.md_5.bungee.api.config.ServerInfo}
     * @param info
     * @return a QueryConnection used to connect to specified server
     */
    public static QueryConnection getConnection(ServerInfo info) {
        for (QueryConnection conn : PluginQuery.getMessenger().getActiveConnections()) {
            if (conn.getMetadata().getData(SERVER_INFO) == info) {
                return conn;
            }
        }
        return null;
    }

    @Override
    public void onEnable() {
        disabling = false;
        getLogger().log(Level.INFO, "Initializing PluginQuery...");
        PluginQuery.initializeDefaultMessenger();
        PluginQuery.getMessenger().getMetadata().setData(REMOTEOBJECT_PROXYSERVER, new BungeeRemoteObjectMessenger(PluginQuery.getMessenger(), QueryContext.REMOTEOBJECT_BUNGEESERVER_CHANNEL, getProxy()));
        reloadConfig();
        PluginQuery.getMessenger().getEventBus().registerListener(this);
        getProxy().getPluginManager().registerCommand(this, new BungeePluginQueryCommand(this));
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().registerChannel(QueryContext.PLUGIN_MESSAGING_CHANNEL);
        getProxy().getServers().values().forEach(server -> {
            getLogger().log(Level.INFO, "Connecting to server \"" + server.getName() + "\"...");
            QueryConnection conn = PluginQuery.getMessenger().newConnection(server.getSocketAddress());
            conn.getMetadata().setData(SERVER_INFO, server);
            QueryFuture<QueryConnection> future = conn.connect();
            future.addListener(check -> {
                if (check.isSuccess()) {
                    getLogger().log(Level.INFO, "Successfully connected to server \"" + server.getName() + "\"!");
                } else {
                    getLogger().log(Level.SEVERE, "Failed to connect to server \"" + server.getName() + "\"");
                }
            });
        });
    }


    @Override
    public void onDisable() {
        disabling = true;
        for (QueryConnection conn : PluginQuery.getMessenger().getActiveConnections()) {
            conn.disconnect().joinThread();
        }
    }

    public QueryConfiguration getQueryConfig() {
        return config;
    }

    public EncryptionToolkit getEncryption() {
        return encryption;
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent e) {
        if (QueryContext.PLUGIN_MESSAGING_CHANNEL.equals(e.getTag())) {
            DataBuffer buffer = new DataBuffer(e.getData());
            String command = buffer.readUTF();
            String prefix = QueryContext.COMMAND_PREFIX;
            switch (command) {
                case QueryContext.RESPONSE_NO_PERMISSION:
                    sendMessage(e.getReceiver(), prefix + "&eYou don't have permission \"&b" + QueryContext.ADMIN_PERMISSION + "&e\" in your spigot server.");
                    break;
                case QueryContext.RESPONSE_ERROR:
                    sendMessage(e.getReceiver(), prefix + "&cAn error occurred: &f" + buffer.readUTF());
                    break;
                case QueryContext.RESPONSE_LOCKED:
                    sendMessage(e.getReceiver(), prefix + "&cCannot synchronize locked server");
                    break;
                case QueryContext.RESPONSE_SUCCESS:
                    sendMessage(e.getReceiver(), prefix + "&aSuccessfully bound your spigot server with your bungeecord server!");
                    break;
                default:
                    sendMessage(e.getReceiver(), prefix + "&eUnknown response: " + command);
                    break;
            }
        }
    }

    public void reloadConfig() {
        disabling = true;
        QueryMessenger messenger = PluginQuery.getMessenger();
        try {
            File file = new File(getDataFolder(), "config.yml");
            if (file.exists()) {
                config.loadConfiguration(file);
            } else {
                file.getParentFile().mkdirs();
                config.saveConfiguration(file);
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to load config.yml", e);
        }
        encryption = null;
        messenger.getMetadata().setData(QueryContext.METAKEY_MAX_RECONNECT_TRY, null);
        messenger.getMetadata().setData(QueryContext.METAKEY_RECONNECT_DELAY, null);
        List<String> whitelist = getQueryConfig().getOption(QueryContext.IP_WHITELIST);
        if (whitelist != null && !whitelist.isEmpty()) {
            messenger.getPipeline().addLast(new QueryWhitelist(whitelist));
        }
        long throttle = getQueryConfig().getOption(QueryContext.CONNECTION_THROTTLE).longValue();
        if (throttle > 0) {
            messenger.getPipeline().addLast(new QueryThrottle(throttle));
        }
        long reconnectDelay = getQueryConfig().getOption(QueryContext.RECONNECT_DELAY).longValue();
        if (reconnectDelay >= 0) {
            messenger.getMetadata().setData(QueryContext.METAKEY_RECONNECT_DELAY, reconnectDelay);
        }
        int maxConnection = getQueryConfig().getOption(QueryContext.CONNECTION_LIMIT).intValue();
        if (maxConnection >= 0) {
            messenger.getPipeline().addLast(new QueryLimiter(maxConnection));
        }
        int maxReconnectTry = getQueryConfig().getOption(QueryContext.MAX_RECONNECT_TRY).intValue();
        messenger.getMetadata().setData(QueryContext.METAKEY_MAX_RECONNECT_TRY, maxReconnectTry);
        File secret = new File(getDataFolder(), "secret.key");
        if (secret.exists()) {
            try {
                encryption = new EncryptionToolkit(EncryptionToolkit.readKey(secret));
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IOException e) {
                getLogger().log(Level.SEVERE, "Failed to load secret.key! Please delete it to generate a new one", e);
            }
        } else {
            try {
                Key generated = EncryptionToolkit.generateKey();
                encryption = new EncryptionToolkit(generated);
                encryption.writeKey(secret);
            } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                getLogger().log(Level.SEVERE, "Failed to generate secret.key!", e);
            }
        }
        if (encryption != null) {
            getLogger().log(Level.INFO, "Using encryption algorithm: " + encryption.getKey().getAlgorithm());
            PluginQuery.getMessenger().getPipeline().addLast(
                    new QueryDecryptor(encryption.getDecryptor()),
                    new QueryEncryptor(encryption.getEncryptor()));
        }
        QueryPipeline pipe = PluginQuery.getMessenger().getPipeline();
        QueryDeflater deflater = new QueryDeflater();
        QueryInflater inflater = new QueryInflater();
        if (!pipe.addBefore(QueryContext.HANDLER_ENCRYPTOR, deflater)) {
            pipe.addLast(deflater);
        }
        if (!pipe.addAfter(QueryContext.HANDLER_DECRYPTOR, inflater)) {
            pipe.addFirst(inflater);
        }
        disabling = false;
        for (QueryConnection conn : PluginQuery.getMessenger().getActiveConnections()) {
            getLogger().log(Level.INFO, "Disconnecting " + conn.getAddress());
            conn.disconnect();
        }
    }

    public void sendMessage(Object obj, String msg) {
        if (obj instanceof CommandSender) {
            ((CommandSender) obj).sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', msg)));
        }
    }

    @Override
    public void onQueryReceived(QueryConnection connection, String channel, byte[] message) {
        if (QueryContext.PLUGIN_MESSAGING_CHANNEL.equals(channel)) {
            DataBuffer buffer = new DataBuffer(message);
            String command = buffer.readUTF();
            if (QueryContext.COMMAND_VERSION_CHECK.equals(command)) {
                String spigotVersion = buffer.readUTF();
                String sourceServer = buffer.readUTF();
                getLogger().log(Level.INFO, sourceServer + " version: " + spigotVersion);
            }
        } else {
            // custom bungeecord event handling
            getProxy().getPluginManager().callEvent(new QueryMessageEvent(connection, channel, message));
        }
    }

    @Override
    public void onConnectionStateChange(QueryConnection connection) {
        if (!connection.isConnected()) {
            ServerInfo info = connection.getMetadata().getData(SERVER_INFO);
            if (info != null && !disabling) {
                int max = connection.getMetadata().getData(QueryContext.METAKEY_MAX_RECONNECT_TRY, -1);
                int times = connection.getMetadata().getData(RECONNECT_TRY_TIMES, 0);
                if (max > 0 && times >= max) {
                    return;
                }
                connection.getMetadata().setData(RECONNECT_TRY_TIMES, times + 1);
                connection.connect();
            }
        } else {
            connection.getMetadata().setData(RECONNECT_TRY_TIMES, null);
            ServerInfo server = connection.getMetadata().getData(SERVER_INFO);
            if (server != null) {
                DataBuffer buffer = new DataBuffer();
                buffer.writeUTF(QueryContext.COMMAND_VERSION_CHECK);
                buffer.writeUTF(getProxy().getVersion());
                buffer.writeUTF(server.getName());
                connection.sendQuery(QueryContext.PLUGIN_MESSAGING_CHANNEL, buffer.toByteArray());
            }
        }
    }

}