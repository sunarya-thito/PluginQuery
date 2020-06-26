package septogeddon.pluginquery.library.remote;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.utils.QueryUtil;

public class Example {

	public static void bungeecord() {
		// Prepare the object
		ProxyServer server = BungeeCord.getInstance();
		// Create a Thread pool
		ExecutorService service = Executors.newCachedThreadPool();
		// Broadcast to all active connections
		for (QueryConnection active : PluginQuery.getMessenger().getActiveConnections()) {
			// Create a RemoteObject and provide the object
			RemoteObject<ProxyServer> proxyServer = new RemoteObject<ProxyServer>("MyProxyServerChannel", active, server);
			// Apply the thread pool
			proxyServer.setExecutorService(service);
		}
		// The thing we want to do in Spigot Server
		ServerInfo lobby = server.getServerInfo("lobby");
		Collection<ProxiedPlayer> players = server.getPlayers();
		for (ProxiedPlayer player : players) {
			player.connect(lobby);
		}
	}
	
	public static void spigot() {
		// Get the BungeeCord QueryConnection
		QueryConnection bungeecordConnection = QueryUtil.first(PluginQuery.getMessenger().getActiveConnections());
		// Create a RemoteObject
		RemoteObject<BungeeProxy> proxyServer = new RemoteObject<>("MyProxyServerChannel", bungeecordConnection, BungeeProxy.class);
		// Get the provided object
		BungeeProxy server = proxyServer.getCrossoverObject();
		// Do everything with it
		UnknownObject lobby = server.getServerInfo("lobby");
		Collection<BungeePlayer> players = server.getPlayers();
		for (BungeePlayer pl : players) {
			pl.connect(lobby);
		}
	}
	
	public static interface BungeeProxy {
		public Collection<BungeePlayer> getPlayers();
		public UnknownObject getServerInfo(String name);
	}
	
	public static interface BungeePlayer {
		public void connect(UnknownObject server);
	}
	
}
