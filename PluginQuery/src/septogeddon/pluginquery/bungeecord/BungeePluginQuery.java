package septogeddon.pluginquery.bungeecord;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;

import javax.crypto.NoSuchPaddingException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.QueryConfigurationImpl;
import septogeddon.pluginquery.api.QueryConfiguration;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryFuture;
import septogeddon.pluginquery.api.QueryMessageListener;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.api.QueryMetadataKey;
import septogeddon.pluginquery.channel.QueryDeflater;
import septogeddon.pluginquery.channel.QueryEncryptor;
import septogeddon.pluginquery.channel.QueryInflater;
import septogeddon.pluginquery.channel.QueryLimiter;
import septogeddon.pluginquery.channel.QueryThrottle;
import septogeddon.pluginquery.channel.QueryWhitelist;
import septogeddon.pluginquery.utils.DataBuffer;
import septogeddon.pluginquery.utils.EncryptionToolkit;

public class BungeePluginQuery extends Plugin implements Listener, QueryMessageListener {

	public static final QueryMetadataKey<ServerInfo> SERVER_INFO = QueryMetadataKey.newCastableKey("bungeeserverinfo", ServerInfo.class);

	public static QueryConnection getConnection(ServerInfo info) {
		for (QueryConnection conn : PluginQuery.getMessenger().getConnections()) {
			if (conn.getMetadata().getData(SERVER_INFO) == info) {
				return conn;
			}
		}
		return null;
	}
	
	private QueryConfigurationImpl config = new QueryConfigurationImpl();
	private EncryptionToolkit encryption;
	public void onEnable() {
		getLogger().log(Level.INFO, "Initializing PluginQuery...");
		PluginQuery.initializeDefaultMessenger();
		reloadConfig();
		PluginQuery.getMessenger().getPipeline().addFirst(
				new QueryInflater(),
				new QueryDeflater()
				);
		// the reconnect handler, not affected by MAX_RECONNECT_TRY
		PluginQuery.getMessenger().getEventBus().registerListener(QueryContext.RECONNECT_HANDLER);
		getProxy().getServers().values().forEach(server->{
			getLogger().log(Level.INFO, "Connecting to server \""+server.getName()+"\"...");
			QueryConnection conn = PluginQuery.getMessenger().newConnection(server.getSocketAddress());
			QueryFuture<QueryConnection> future = conn.connect();
			future.addListener(check->{
				if (check.isSuccess()) {
					getLogger().log(Level.INFO, "Successfully connected to server \""+server.getName()+"\"!");
				} else {
					getLogger().log(Level.SEVERE, "Failed to connect to server \""+server.getName()+"\"");
				}
			});
		});
		getProxy().getPluginManager().registerListener(this, this);
		getProxy().registerChannel(QueryContext.PLUGIN_MESSAGING_CHANNEL);
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
			switch (command) {
			case QueryContext.RESPONSE_NO_PERMISSION:
				sendMessage(e.getReceiver(), "&eYou don't have permission \"&b"+QueryContext.ADMIN_PERMISSION+"&e\" in your spigot server.");
				break;
			case QueryContext.RESPONSE_ERROR:
				sendMessage(e.getReceiver(), "&cAn error occured: &f"+buffer.readUTF());
				break;
			case QueryContext.RESPONSE_SUCCESS:
				sendMessage(e.getReceiver(), "&aSuccessfully binded your spigot server with your bungeecord server!");
				break;
			}
		}
	}
	
	public void reloadConfig() {
		QueryMessenger messenger = PluginQuery.getMessenger();
		try {
			config.loadConfiguration(new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to load config.yml", e);
		}
		encryption = null;
		messenger.getPipeline().remove(QueryContext.HANDLER_ENCRYPTOR);
		messenger.getPipeline().remove(QueryContext.HANDLER_DECRYPTOR);
		messenger.getPipeline().remove(QueryContext.HANDLER_LIMITER);
		messenger.getPipeline().remove(QueryContext.HANDLER_WHITELIST);
		messenger.getPipeline().remove(QueryContext.HANDLER_THROTTLE);
		messenger.getMetadata().setData(QueryContext.METAKEY_MAX_RECONNECT_TRY, null);
		messenger.getMetadata().setData(QueryContext.METAKEY_RECONNECT_DELAY, null);
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
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
				getLogger().log(Level.SEVERE, "Failed to generate secret.key!", e);
			}
		}
		if (encryption != null) {
			messenger.getPipeline().addFirst(
					new QueryEncryptor(encryption.getEncryptor()),
					new QueryEncryptor(encryption.getDecryptor()));
		}
		List<String> whitelist = getQueryConfig().getOption(QueryContext.IP_WHITELIST);
		if (whitelist != null && !whitelist.isEmpty()) {
			messenger.getPipeline().addFirst(new QueryWhitelist(whitelist));
		}
		long throttle = getQueryConfig().getOption(QueryContext.CONNECTION_THROTTLE).longValue();
		if (throttle > 0) {
			messenger.getPipeline().addFirst(new QueryThrottle(throttle));
		}
		long reconnectDelay = getQueryConfig().getOption(QueryContext.RECONNECT_DELAY).longValue();
		if (reconnectDelay > 0) {
			messenger.getMetadata().setData(QueryContext.METAKEY_RECONNECT_DELAY, reconnectDelay);
		}
		int maxConnection = getQueryConfig().getOption(QueryContext.CONNECTION_LIMIT).intValue();
		if (maxConnection >= 0) {
			messenger.getPipeline().addFirst(new QueryLimiter(maxConnection));
		}
		int maxReconnectTry = getQueryConfig().getOption(QueryContext.MAX_RECONNECT_TRY).intValue();
		messenger.getMetadata().setData(QueryContext.METAKEY_MAX_RECONNECT_TRY, maxReconnectTry);
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
				getLogger().log(Level.INFO, sourceServer+" version: "+spigotVersion);
			}
		}
	}
	
}