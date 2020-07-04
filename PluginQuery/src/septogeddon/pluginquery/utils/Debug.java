package septogeddon.pluginquery.utils;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Supplier;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.channel.QueryDecryptor;
import septogeddon.pluginquery.channel.QueryDeflater;
import septogeddon.pluginquery.channel.QueryEncryptor;
import septogeddon.pluginquery.channel.QueryInflater;
import septogeddon.pluginquery.library.remote.ClassRegistry;
import septogeddon.pluginquery.library.remote.RemoteObject;
import septogeddon.pluginquery.library.remote.Substitute;
//QueryConnection connection = messenger.newConnection(new InetSocketAddress("131.153.48.90", 25619));
public class Debug {

	public static boolean STATE_DEBUG = false;
	public static void debug(Supplier<String> msg) {
		if (STATE_DEBUG) System.out.println("[PluginQueryDebug] "+msg.get());
	}
	public static void main(String[]args) throws Throwable {
		PluginQuery.initializeDefaultMessenger();
		QueryMessenger messenger = PluginQuery.getMessenger();
		QueryConnection connection = messenger.newConnection(new InetSocketAddress("131.153.48.90", 25619));
		EncryptionToolkit toolkit = new EncryptionToolkit(EncryptionToolkit.readKey(new File("D:\\TestServer\\plugins\\PluginQuery\\secret.key")));
		messenger.getPipeline().addLast(
				new QueryDecryptor(toolkit.getDecryptor()),
				new QueryDeflater(),
				new QueryInflater(),
				new QueryEncryptor(toolkit.getEncryptor())
				);
		connection.connect();
		
		RemoteObject<BukkitServer> server = new RemoteObject<>(QueryContext.REMOTEOBJECT_BUKKITSERVER_CHANNEL, connection, BukkitServer.class, new ClassRegistry());
		BukkitServer serv = server.getObject();
		System.out.println("Connected to "+serv.getVersion());
		serv.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&aHello &clmao &ethis &dtext &fsent &bfrom &1another &2client"));
		for (OfflinePlayer pl : serv.getOfflinePlayers()) {
			System.out.println("Player: "+pl.getName()+" UUID:"+pl.getUniqueId());
		}
		for (BukkitPlugin plugin : serv.getPluginManager().getPlugins()) {
			System.out.println("Plugin: "+plugin.getName()+" Version: "+plugin.getDescription().getVersion()+" Authors: "+String.join(", ", plugin.getDescription().getAuthors()));
		}
	}
	
	@Substitute(Server.class)
	public static interface BukkitServer {
		public OfflinePlayer[] getOfflinePlayers();
		public BukkitPluginManager getPluginManager(); 
		public ConsoleCommandSender getConsoleSender();
		public String getVersion();
	}
	
	@Substitute(PluginManager.class)
	public static interface BukkitPluginManager {
		public BukkitPlugin[] getPlugins();
	}
	
	@Substitute(Plugin.class)
	public static interface BukkitPlugin {
		public String getName();
		public BukkitPluginDescription getDescription();
	}
	
	@Substitute(PluginDescriptionFile.class)
	public static interface BukkitPluginDescription {
		public String getVersion();
		public List<String> getAuthors();
	}
	
}
