package septogeddon.pluginquery.utils;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryFuture;
import septogeddon.pluginquery.api.QueryMessenger;

public class Debug {

	public static boolean ENABLED = true;
	public static void debug(Object obj) {
		System.out.println("DEBUG: "+obj);
	}
	
	public static void main(String[]args) throws Throwable {
		PluginQuery.initializeDefaultMessenger();
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
		QueuedQuery queue = new QueuedQuery(connection, QueryContext.PLUGIN_MESSAGING_CHANNEL);
		AtomicInteger count = new AtomicInteger();
		for (int i = 0; i < 1000; i++) {
			DataBuffer buffer = new DataBuffer();
			buffer.writeUTF(QueryContext.COMMAND_VERSION_CHECK);
			buffer.writeUTF("Standalone non-bungee");
			buffer.writeUTF("testServer");
			QueryFuture<byte[]> bytes = queue.sendQuery(buffer.toByteArray());
//			bytes.joinThread();
			bytes.thenAccept(x->{
				byte[] result = bytes.getResult();
				DataBuffer buf = new DataBuffer(result);
				String command = buf.readUTF();
				if (QueryContext.COMMAND_VERSION_CHECK.equals(command)) {
					String version = buf.readUTF();
					String source = buf.readUTF();
					debug(source+": "+version+" ("+count.getAndIncrement()+")");
				}
			});
		}
		debug("Done!");
	}
	
}
