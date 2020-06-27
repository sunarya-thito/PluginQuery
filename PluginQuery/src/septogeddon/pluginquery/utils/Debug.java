package septogeddon.pluginquery.utils;

import java.io.File;
import java.net.InetSocketAddress;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

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
//QueryConnection connection = messenger.newConnection(new InetSocketAddress("131.153.48.90", 25619));
public class Debug {

	public static void main(String[]args) throws Throwable {
		PluginQuery.initializeDefaultMessenger();
		QueryMessenger messenger = PluginQuery.getMessenger();
		QueryConnection connection = messenger.newConnection(new InetSocketAddress("localhost", 25565));
		EncryptionToolkit toolkit = new EncryptionToolkit(EncryptionToolkit.readKey(new File("D:\\TestServer\\plugins\\PluginQuery\\secret.key")));
		messenger.getPipeline().addLast(
				new QueryDecryptor(toolkit.getDecryptor()),
				new QueryDeflater(),
				new QueryInflater(),
				new QueryEncryptor(toolkit.getEncryptor())
				);
		connection.connect();
		RemoteObject<Server> server = new RemoteObject<>(QueryContext.REMOTEOBJECT_BUKKITSERVER_CHANNEL, connection, Server.class, ClassRegistry.GLOBAL_REGISTRY);
		Server serv = server.getObject();
		System.out.println("Connected to "+serv.getVersion());
		serv.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&aHello &clmao &ethis &dtext &fsent &bfrom &1another &2client"));
		for (OfflinePlayer pl : serv.getOfflinePlayers()) {
			System.out.println(pl.getName()+":"+pl.getUniqueId());
		}
		for (Plugin plugin : serv.getPluginManager().getPlugins()) {
			System.out.println(plugin.getName());
		}
	}
	
}
