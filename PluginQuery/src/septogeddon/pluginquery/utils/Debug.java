package septogeddon.pluginquery.utils;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryFuture;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.channel.QueryDecryptor;
import septogeddon.pluginquery.channel.QueryDeflater;
import septogeddon.pluginquery.channel.QueryEncryptor;
import septogeddon.pluginquery.channel.QueryInflater;

public class Debug {

	public static boolean ENABLED = true;
	public static void debug(Object obj) {
		System.out.println("DEBUG: "+obj);
	}
	
	public static void main(String[]args) throws Throwable {
		PluginQuery.initializeDefaultMessenger();
		QueryMessenger messenger = PluginQuery.getMessenger();
		QueryConnection connection = messenger.newConnection(new InetSocketAddress("localhost", 25565));
		EncryptionToolkit toolkit = new EncryptionToolkit(EncryptionToolkit.readKey(new File("D:\\TestServer\\plugins\\PluginQuery\\secret.key")));
		messenger.getPipeline().addLast(
				new QueryDecryptor(toolkit.getDecryptor()),
				new QueryInflater(),
				new QueryDeflater(),
				new QueryEncryptor(toolkit.getEncryptor())
				);
		messenger.getPipeline().getPipes().forEach(pipe->Debug.debug(pipe.getName()));
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
		Debug.debug("waiting to connect");
		future.joinThread();
		Debug.debug("CONNECTED");
		QueuedQuery queue = new QueuedQuery(connection, QueryContext.PLUGIN_MESSAGING_CHANNEL);
		AtomicInteger count = new AtomicInteger();
		for (int i = 0; i < 100; i++) {
			DataBuffer buffer = new DataBuffer();
			buffer.writeUTF(QueryContext.COMMAND_VERSION_CHECK);
			buffer.writeUTF("Standalone non-bungee");
			buffer.writeUTF("testServer");
			QueryFuture<byte[]> bytes = queue.sendQuery(buffer.toByteArray());
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
		connection.disconnect().joinThread();
		connection.connect().joinThread();
		for (int i = 0; i < 1000; i++) {
			DataBuffer buffer = new DataBuffer();
			buffer.writeUTF(QueryContext.COMMAND_VERSION_CHECK);
			buffer.writeUTF("Standalone non-bungee");
			buffer.writeUTF("testServer");
			QueryFuture<byte[]> bytes = queue.sendQuery(buffer.toByteArray());
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
