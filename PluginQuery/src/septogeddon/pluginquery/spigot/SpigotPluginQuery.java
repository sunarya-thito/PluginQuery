package septogeddon.pluginquery.spigot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Key;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

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
import septogeddon.pluginquery.api.QueryPipeline;
import septogeddon.pluginquery.channel.QueryDecryptor;
import septogeddon.pluginquery.channel.QueryDeflater;
import septogeddon.pluginquery.channel.QueryEncryptor;
import septogeddon.pluginquery.channel.QueryInflater;
import septogeddon.pluginquery.channel.QueryLimiter;
import septogeddon.pluginquery.channel.QueryThrottle;
import septogeddon.pluginquery.channel.QueryWhitelist;
import septogeddon.pluginquery.netty.QueryInterceptor;
import septogeddon.pluginquery.netty.QueryPushback;
import septogeddon.pluginquery.spigot.event.QueryMessageEvent;
import septogeddon.pluginquery.utils.DataBuffer;
import septogeddon.pluginquery.utils.Debug;
import septogeddon.pluginquery.utils.EncryptionToolkit;

public class SpigotPluginQuery extends JavaPlugin implements QueryMessageListener, PluginMessageListener {
	
	private Set<Channel> listeners = ConcurrentHashMap.newKeySet();
	
	private QueryConfigurationImpl config = new QueryConfigurationImpl();
	private EncryptionToolkit encryption;
	
	public void onEnable() {
		PluginQuery.initializeDefaultMessenger();
		PluginQuery.getMessenger().getEventBus().registerListener(this);
		getCommand("spigotpluginquery").setExecutor(new SpigotPluginQueryCommand(this));
		getServer().getServicesManager()
			.register(QueryMessenger.class, PluginQuery.getMessenger(), this, ServicePriority.Normal);
		getLogger().log(Level.INFO, "Registering plugin channel: "+QueryContext.PLUGIN_MESSAGING_CHANNEL);
		getServer().getMessenger().registerIncomingPluginChannel(this, QueryContext.PLUGIN_MESSAGING_CHANNEL, this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, QueryContext.PLUGIN_MESSAGING_CHANNEL);
		getServer().getScheduler().runTask(this, ()->{
			register(true);
		});
	}
	
	public void onDisable() {
		unregister();
		for (QueryConnection conn : PluginQuery.getMessenger().getActiveConnections()) {
			conn.disconnect().joinThread();
		}
	}
	
	public Set<Channel> getListeners() {
		return listeners;
	}
	
	protected void register(boolean tryAgain) {
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
							QueryPushback.lock = list;
							break;
						}
						Channel future = ((ChannelFuture)obj).channel();
						getLogger().log(Level.INFO, "Injected server connection listener: "+future);
						listeners.add(future);
						// begin listening to incoming channels
						future.pipeline().addFirst("query_interceptor", new QueryInterceptor(PluginQuery.getMessenger()));
					}
				}
			}
			if (listeners.isEmpty()) throw new IllegalStateException("empty listener");
			reloadConfig();
		} catch (Throwable t) {
			if (tryAgain) {
				getLogger().log(Level.WARNING, "Failed to inject server connection. Retrying...");
				getServer().getScheduler().scheduleSyncDelayedTask(this, ()->{
					register(false);
				});
			} else {
				getLogger().log(Level.SEVERE, "Failed to inject server connection", t);
			}
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
		if (reconnectDelay > 0) {
			messenger.getMetadata().setData(QueryContext.METAKEY_RECONNECT_DELAY, reconnectDelay);
		}
		int maxConnection = getQueryConfig().getOption(QueryContext.CONNECTION_LIMIT).intValue();
		if (maxConnection >= 0) {
			messenger.getPipeline().addLast(new QueryLimiter(maxConnection));
		}
		int maxReconnectTry = getQueryConfig().getOption(QueryContext.MAX_RECONNECT_TRY).intValue();
		messenger.getMetadata().setData(QueryContext.METAKEY_MAX_RECONNECT_TRY, maxReconnectTry);
		reloadKey();
	}
	
	protected void unregister() {
		for (Channel ch : listeners) {
			try {
				ch.pipeline().remove("query_interceptor");
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
				buffer.clear();
				buffer.writeUTF(QueryContext.COMMAND_VERSION_CHECK);
				buffer.writeUTF(getServer().getVersion());
				buffer.writeUTF(configuredServerName);
				connection.sendQuery(channel, buffer.toByteArray());
			}
		} else {
			// custom QueryMessageEvent handling
			getServer().getPluginManager().callEvent(new QueryMessageEvent(connection, channel, message));
		}
	}
	
	public void reloadKey() {
		encryption = null;
		File secret = new File(getDataFolder(), "secret.key");
		if (secret.exists()) {
			try {
				encryption = new EncryptionToolkit(EncryptionToolkit.readKey(secret));
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, "Failed to load secret.key! Please delete it to generate a new one", e);
			}
		} else {
			try {
				Key generated = EncryptionToolkit.generateKey();
				encryption = new EncryptionToolkit(generated);
				encryption.writeKey(secret);
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, "Failed to generate secret.key!", e);
			}
		}
		if (encryption != null) {
			getLogger().log(Level.INFO, "Using encryption algorithm: "+encryption.getKey().getAlgorithm());
			PluginQuery.getMessenger().getPipeline().addLast(
					new QueryDecryptor(encryption.getDecryptor()),
					new QueryEncryptor(encryption.getEncryptor()));
		} else {
			getLogger().log(Level.SEVERE, "Failed to register encryption!.");
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
		for (QueryConnection conn : PluginQuery.getMessenger().getActiveConnections()) {
			getLogger().log(Level.INFO, "Disconnecting "+conn.getAddress());
			conn.disconnect();
		}
	}
	
	@Override
	public void onPluginMessageReceived(String arg0, Player arg1, byte[] arg2) {
		if (QueryContext.PLUGIN_MESSAGING_CHANNEL.equals(arg0)) {
			DataBuffer buffer = new DataBuffer(arg2);
			String command = buffer.readUTF();
			if (QueryContext.REQUEST_KEY_SHARE.equals(command)) {
				if (arg1 != null) {
					if (getQueryConfig().getOption(QueryContext.LOCK)) {
						buffer.clear();
						buffer.writeUTF(QueryContext.RESPONSE_LOCKED);
					} else if (arg1.hasPermission(QueryContext.ADMIN_PERMISSION)) {
						byte[] keys = buffer.toByteArray();
						try {
							FileOutputStream output = new FileOutputStream(new File(getDataFolder(), "secret.key"));
							output.write(keys);
							output.close();
							reloadKey();
							buffer.clear();
							buffer.writeUTF(QueryContext.RESPONSE_SUCCESS);
							// lock the synchronization
							getQueryConfig().setOption(QueryContext.LOCK, true);
							try {
								getQueryConfig().saveConfiguration(new File(getDataFolder(),"config.yml"));
							} catch (IOException e) {
								getLogger().log(Level.SEVERE, "Failed to save config", e);
							}
							//
							getLogger().log(Level.INFO, "secret.key has been synchronized by player "+arg1.getName());
						} catch (Throwable t) {
							t.printStackTrace();
							buffer.clear();
							buffer.writeUTF(QueryContext.RESPONSE_ERROR);
							buffer.writeUTF(t.toString());
						}
					} else {
						buffer.clear();
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
