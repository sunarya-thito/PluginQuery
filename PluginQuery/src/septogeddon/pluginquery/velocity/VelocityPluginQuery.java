package septogeddon.pluginquery.velocity;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.ChannelMessageSink;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.ProxyVersion;

import net.kyori.text.TextComponent;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.PropertiesQueryConfiguration;
import septogeddon.pluginquery.api.QueryConfiguration;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryFuture;
import septogeddon.pluginquery.api.QueryListener;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.api.QueryMetadataKey;
import septogeddon.pluginquery.api.QueryPipeline;
import septogeddon.pluginquery.channel.QueryDecryptor;
import septogeddon.pluginquery.channel.QueryDeflater;
import septogeddon.pluginquery.channel.QueryEncryptor;
import septogeddon.pluginquery.channel.QueryInflater;
import septogeddon.pluginquery.channel.QueryLimiter;
import septogeddon.pluginquery.channel.QueryThrottle;
import septogeddon.pluginquery.channel.QueryWhitelist;
import septogeddon.pluginquery.utils.DataBuffer;
import septogeddon.pluginquery.utils.EncryptionToolkit;
import septogeddon.pluginquery.velocity.event.QueryMessageEvent;

@Plugin(id = "PluginQuery", name = "PluginQuery", authors = {"Septogeddon"}, version = "1.0.30", description = "Allows you to send plugin message between servers")
public class VelocityPluginQuery implements QueryListener, ChannelIdentifier {

	/***
	 * Metadata Key for {@link com.velocitypowered.api.proxy.server.RegisteredServer}
	 */
	public static final QueryMetadataKey<RegisteredServer> REGISTERED_SERVER = QueryMetadataKey.newCastableKey("velocityregisteredserver", RegisteredServer.class);
	
	private static final QueryMetadataKey<Integer> RECONNECT_TRY_TIMES = QueryMetadataKey.newCastableKey("velocitypluginquery_retry_times", Integer.class);
	
	/***
	 * Get active connection for {@link com.velocitypowered.api.proxy.server.RegisteredServer}
	 * @param server
	 * @return a QueryConnection used to connect to specified server
	 */
	public static QueryConnection getConnection(RegisteredServer server) {
		for (QueryConnection conn : PluginQuery.getMessenger().getActiveConnections()) {
			if (conn.getMetadata().getData(REGISTERED_SERVER) == server) {
				return conn;
			}
		}
		return null;
	}
	
	private final ProxyServer server;
	private final Logger logger;
	private boolean disabling;
	private EncryptionToolkit encryption;
	private Path dataFolder;
	private QueryConfiguration config;
	@Inject
	public VelocityPluginQuery(ProxyServer server, Logger logger, @DataDirectory Path folder) {
		this.server = server;
		this.logger = logger;
		this.dataFolder = folder;
		getLogger().info("Initializing PluginQuery...");
		PluginQuery.initializeDefaultMessenger();
		config = new PropertiesQueryConfiguration();
		getServer().getCommandManager().register(new VelocityPluginQueryCommand(), "pluginquery", "velocitypluginquery", "pq", "vpq", "query");
	}
	
	public void initializeConnectors() {
		disabling = false;
		QueryMessenger messenger = PluginQuery.getMessenger();
		getServer().getAllServers().forEach(server->{
			InetSocketAddress address = server.getServerInfo().getAddress();
			getLogger().info("Connecting to server \""+server.getServerInfo().getName()+"\"...");
			QueryConnection conn = messenger.newConnection(address);
			conn.getMetadata().setData(REGISTERED_SERVER, server);
			QueryFuture<QueryConnection> future = conn.connect();
			future.addListener(connection->{
				if (connection.isSuccess()) {
					getLogger().info("Successfully connected to server \""+server.getServerInfo().getName()+"\"!");
				} else {
					getLogger().error("Failed to connect to server \""+server.getServerInfo().getName());
				}
			});
		});
		getServer().getChannelRegistrar().register(this);
	}
	
	@Subscribe
	public void pluginMessageEvent(PluginMessageEvent event) {
		if (QueryContext.PLUGIN_MESSAGING_CHANNEL.equals(event.getIdentifier().getId())) {
			DataBuffer buffer = new DataBuffer(event.getData());
			String command = buffer.readUTF();
			String prefix = QueryContext.COMMAND_PREFIX;
			ChannelMessageSink receiver = event.getTarget();
			if (receiver instanceof Player) {
				Player player = (Player)receiver;
				switch (command) {
				case QueryContext.RESPONSE_NO_PERMISSION:
					sendMessage(player, prefix+"&eYou don't have permission \"&b"+QueryContext.ADMIN_PERMISSION+"&e\" in your spigot server.");
					break;
				case QueryContext.RESPONSE_ERROR:
					sendMessage(player, prefix+"&cAn error occured: &f"+buffer.readUTF());
					break;
				case QueryContext.RESPONSE_LOCKED:
					sendMessage(player, prefix+"&cCannot synchronize locked server");
					break;
				case QueryContext.RESPONSE_SUCCESS:
					sendMessage(player, prefix+"&aSuccessfully binded your spigot server with your bungeecord server!");
					break;
				default:
					sendMessage(player, prefix+"&eUnknown response: "+command);
					break;
				}
			}
		}
	}
	
	public void sendMessage(Player player, String message) {
		TextComponent component = VelocityPluginQueryCommand.legacy(message);
		player.sendMessage(component);
	}
	
	@Override
	public String getId() {
		return QueryContext.PLUGIN_MESSAGING_CHANNEL;
	}
	
	public EncryptionToolkit getEncryption() {
		return encryption;
	}
	
	public void shutdownConnectors() {
		disabling = true;
		for (QueryConnection conn : PluginQuery.getMessenger().getActiveConnections()) {
			conn.disconnect().joinThread();
		}
	}
	
	public void reloadConfig() {
		disabling = true;
		QueryMessenger messenger = PluginQuery.getMessenger();
		try {
			File file = new File(dataFolder.toFile(), "config.properties");
			if (!file.exists()) {
				config.loadConfiguration(file);
			} else {
				file.getParentFile().mkdirs();
				config.saveConfiguration(file);
			}
		} catch (Throwable t) {
			getLogger().error("Failed to load config.properties", t);
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
				getLogger().error("Failed to load secret.key! Please delete it to generate a new one", e);
			}
		} else {
			try {
				Key generated = EncryptionToolkit.generateKey();
				encryption = new EncryptionToolkit(generated);
				encryption.writeKey(secret);
			} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
				getLogger().error("Failed to generate secret.key!", e);
			}
		}
		if (encryption != null) {
			getLogger().info("Using encryption algorithm: "+encryption.getKey().getAlgorithm());
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
			getLogger().info("Disconnecting "+conn.getAddress());
			conn.disconnect();
		}
	}
	
	public File getDataFolder() {
		return dataFolder.toFile();
	}
	
	public QueryConfiguration getQueryConfig() {
		return config;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public ProxyServer getServer() {
		return server;
	}

	@Override
	public void onConnectionStateChange(QueryConnection connection) {
		if (!connection.isConnected()) {
			RegisteredServer info = connection.getMetadata().getData(REGISTERED_SERVER);
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
			RegisteredServer server = connection.getMetadata().getData(REGISTERED_SERVER);
			if (server != null) {
				DataBuffer buffer = new DataBuffer();
				buffer.writeUTF(QueryContext.COMMAND_VERSION_CHECK);
				ProxyVersion version = getServer().getVersion();
				buffer.writeUTF(version.getName()+" "+version.getVendor()+" "+version.getVersion());
				buffer.writeUTF(server.getServerInfo().getName());
				connection.sendQuery(QueryContext.PLUGIN_MESSAGING_CHANNEL, buffer.toByteArray());
			}
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
				getLogger().info(sourceServer+" version: "+spigotVersion);
			}
		} else {
			// custom bungeecord event handling
			getServer().getEventManager().fire(new QueryMessageEvent(connection, channel, message));
		}
	}
	
}
