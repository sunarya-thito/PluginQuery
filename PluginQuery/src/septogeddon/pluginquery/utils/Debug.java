package septogeddon.pluginquery.utils;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryFuture;
import septogeddon.pluginquery.api.QueryMessenger;

public class Debug {

	public static boolean ENABLED = true;
	public static void debug(Object obj) {
		System.out.println("DEBUG: "+obj);
	}
	
	public static void main(String[]args) throws Throwable {
		PluginQuery.initializeDefaultMessenger();
		AtomicInteger a = new AtomicInteger();
		QueryMessenger messenger = PluginQuery.getMessenger();
		QueryConnection connection = messenger.newConnection(new InetSocketAddress("localhost", 25565));
		QueryFuture<QueryConnection> future = connection.connect();
		future.addListener(fut->{
			if (fut.isSuccess()) {
				debug("Connected to localhost:25565");
			} else {
				debug("Failed to connect");
				fut.getCause().printStackTrace();
			}
		});
		connection.getEventBus().registerListener(conn->{
			if (conn.isConnected()) {
				System.out.println("Connected!");
			} else {
				System.out.println("Disconnected!");
			}
		});
		connection.getEventBus().registerListener((conn, channel, message)->{
			System.out.println("received channel "+channel+" ("+a.incrementAndGet()+")");
		});
		for (int i = 0; i < 1; i++) {
			connection.sendQuery("example:channel", "this is just a message".getBytes());
//			Thread.sleep(1000);
		}
	}
	
}
