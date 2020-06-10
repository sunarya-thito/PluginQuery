package septogeddon.pluginquery.spigot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.crypto.NoSuchPaddingException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.QueryConfigurationImpl;
import septogeddon.pluginquery.api.QueryConfiguration;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryMessageListener;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.channel.QueryEncryptor;
import septogeddon.pluginquery.channel.QueryLimiter;
import septogeddon.pluginquery.channel.QueryThrottle;
import septogeddon.pluginquery.channel.QueryWhitelist;
import septogeddon.pluginquery.netty.QueryInterceptor;
import septogeddon.pluginquery.utils.DataBuffer;
import septogeddon.pluginquery.utils.EncryptionToolkit;

public class SpigotPluginQuery extends JavaPlugin implements QueryMessageListener, PluginMessageListener {
	
	private Set<Channel> listeners = ConcurrentHashMap.newKeySet();
	
	private QueryConfigurationImpl config = new QueryConfigurationImpl();
	private EncryptionToolkit encryption;
	
	public void onEnable() {
		getServer().getMessenger().registerIncomingPluginChannel(this, QueryContext.PLUGIN_MESSAGING_CHANNEL, this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, QueryContext.PLUGIN_MESSAGING_CHANNEL);
		PluginQuery.initializeDefaultMessenger();
		register();
		PluginQuery.getMessenger().getEventBus().registerListener(this);
		getServer().getServicesManager()
			.register(QueryMessenger.class, PluginQuery.getMessenger(), this, ServicePriority.Normal);
	}
	
	public void onDisable() {
		unregister();
	}
	
	protected void register() {
		try {
			// TODO Check latebind
			Object craftserver = Bukkit.getServer();
			Object server = craftserver.getClass().getMethod("getServer").invoke(craftserver);
			Object serverConnection = server.getClass().getMethod("getServerConnection").invoke(server);
			Field[] decl = serverConnection.getClass().getDeclaredFields();
			for (Field f : decl) {
				if (f.getType().equals(List.class)) {
					f.setAccessible(true);
					List<?> list = (List<?>)f.get(serverConnection);
					for (Object obj : list) {
						if (!(obj instanceof ChannelFuture)) {
							break;
						}
						Channel future = ((ChannelFuture)obj).channel();
						listeners.add(future);
						// begin listening to incoming channels
						future.pipeline().addFirst(new QueryInterceptor(PluginQuery.getMessenger()));
					}
				}
			}
		} catch (Throwable t) {
			getLogger().log(Level.SEVERE, "Failed to inject server connection", t);
		}
	}
	
	public QueryConfiguration getQueryConfig() {
		return config;
	}
	
	public EncryptionToolkit getEncryption() {
		return encryption;
	}
	
	public void reloadConfig() {
		QueryMessenger messenger = PluginQuery.getMessenger();
		try {
			config.loadConfiguration(new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to load config.yml", e);
		}
		messenger.getPipeline().remove(QueryContext.HANDLER_ENCRYPTOR);
		messenger.getPipeline().remove(QueryContext.HANDLER_DECRYPTOR);
		messenger.getPipeline().remove(QueryContext.HANDLER_LIMITER);
		messenger.getPipeline().remove(QueryContext.HANDLER_WHITELIST);
		messenger.getPipeline().remove(QueryContext.HANDLER_THROTTLE);
		messenger.getMetadata().setData(QueryContext.METAKEY_MAX_RECONNECT_TRY, null);
		messenger.getMetadata().setData(QueryContext.METAKEY_RECONNECT_DELAY, null);
		reloadKey();
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
	
	protected void unregister() {
		for (Channel ch : listeners) {
			try {
				ch.pipeline().remove(QueryInterceptor.class);
			} catch (Throwable t) {
			}
		}
		listeners.clear();
	}

	@Override
	public void onQueryReceived(QueryConnection connection, String channel, byte[] message) {
		if (QueryContext.PLUGIN_MESSAGING_CHANNEL.equals(channel)) {
			DataBuffer buffer = new DataBuffer(message);
			String command = buffer.readUTF();
			if (QueryContext.COMMAND_VERSION_CHECK.equals(command)) {
				String bungeeCordVersion = buffer.readUTF();
				String configuredServerName = buffer.readUTF();
				getLogger().log(Level.INFO, "BungeeCord version: "+bungeeCordVersion+" ("+connection.getAddress()+")");
				getLogger().log(Level.INFO, "Configured server name: "+configuredServerName);
				buffer.writeUTF(QueryContext.COMMAND_VERSION_CHECK);
				buffer.writeUTF(getServer().getVersion());
				buffer.writeUTF(configuredServerName);
				connection.sendQuery(channel, buffer.toByteArray());
			}
		}
	}
	
	public void reloadKey() {
		encryption = null;
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
			PluginQuery.getMessenger().getPipeline().addFirst(
					new QueryEncryptor(encryption.getEncryptor()),
					new QueryEncryptor(encryption.getDecryptor()));
		} else {
			getLogger().log(Level.SEVERE, "Failed to register encryption! Will be disable this plugin for your safety.");
			setEnabled(false);
		}
	}
	
	@Override
	public void onPluginMessageReceived(String arg0, Player arg1, byte[] arg2) {
		if (QueryContext.PLUGIN_MESSAGING_CHANNEL.equals(arg0)) {
			DataBuffer buffer = new DataBuffer(arg2);
			String command = buffer.readUTF();
			if (QueryContext.REQUEST_KEY_SHARE.equals(command)) {
				if (arg1 != null) {
					if (arg1.hasPermission(QueryContext.ADMIN_PERMISSION)) {
						byte[] keys = buffer.readBytes().getBytes();
						try (FileOutputStream output = new FileOutputStream(new File(getDataFolder(), "secret.key"))) {
							output.write(keys);
							reloadKey();
							buffer.writeUTF(QueryContext.RESPONSE_SUCCESS);
							// lock the synchronization
							getQueryConfig().setOption(QueryContext.LOCK, true);
							saveConfig();
							//
						} catch (Throwable t) {
							buffer.writeUTF(QueryContext.RESPONSE_ERROR);
							buffer.writeUTF(t.toString());
						}
					} else {
						buffer.writeUTF(QueryContext.RESPONSE_NO_PERMISSION);
					}
				}
			}
			if (buffer.available() > 0 && arg1 != null) {
				arg1.sendPluginMessage(this, QueryContext.PLUGIN_MESSAGING_CHANNEL, buffer.toByteArray());
			}
		}
	}
}
