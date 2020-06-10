package septogeddon.pluginquery.utils;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import septogeddon.pluginquery.QueryCompletableFuture;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryFuture;
import septogeddon.pluginquery.api.QueryListener;

public class QueuedQuery implements QueryListener {

	private String salt = UUID.randomUUID().toString();
	private QueryConnection connection;
	private AtomicLong id = new AtomicLong();
	private Map<Long,QueryCompletableFuture<byte[]>> futures = new ConcurrentHashMap<>();
	private String channel;
	public QueuedQuery(QueryConnection conn, String channel) {
		QueryUtil.nonNull(conn, "connection");
		QueryUtil.nonNull(channel, "channel");
		this.connection = conn;
		this.channel = channel;
	}
	
	public QueryFuture<byte[]> sendQuery(byte[] message) {
		QueryCompletableFuture<byte[]> future = new QueryCompletableFuture<>();
		DataBuffer buffer = new DataBuffer();
		long id = this.id.getAndIncrement();
		buffer.writeUTF(salt);
		buffer.writeLong(id);
		buffer.write(message);
		QueryFuture<QueryConnection> fut = connection.sendQuery(channel, buffer.toByteArray(), true);
		fut.addListener(queryFuture->{
			if (queryFuture.isSuccess()) {
				futures.put(id, future);
			} else {
				future.completeExceptionally(queryFuture.getCause());
			}
		});
		return future;
	}

	@Override
	public void onConnectionStateChange(QueryConnection connection) {
		if (!connection.isConnected()) {
			synchronized(futures) {
				futures.forEach((key, value)->value.completeExceptionally(new IOException("connection closed")));
				futures.clear();
			}
		}
	}

	@Override
	public void onQueryReceived(QueryConnection connection, String channel, byte[] message) {
		if (this.channel.equals(channel)) {
			DataBuffer buffer = new DataBuffer(message);
			QueryCompletableFuture<byte[]> future = null;
			try {
				String salt = buffer.readUTF();
				if (!this.salt.equals(salt)) {
					return;
				}
				long id = buffer.readLong();
				future = futures.remove(id);
			} catch (Throwable t) {
				return;
			}
			byte[] originalMessage = buffer.toByteArray();
			if (future != null) {
				future.complete(originalMessage);
			}
		}
	}
	
}
