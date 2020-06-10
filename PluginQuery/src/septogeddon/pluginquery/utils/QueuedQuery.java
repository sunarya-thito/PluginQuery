package septogeddon.pluginquery.utils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import septogeddon.pluginquery.QueryCompletableFuture;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryFuture;
import septogeddon.pluginquery.api.QueryListener;

public class QueuedQuery implements QueryListener {

	private QueryConnection connection;
	private String channel;
	private Queue<QueryCompletableFuture<byte[]>> queue = new LinkedList<>();
	public QueuedQuery(QueryConnection conn, String channel) {
		QueryUtil.nonNull(conn, "connection");
		QueryUtil.nonNull(channel, "channel");
		this.connection = conn;
		this.connection.getEventBus().registerListener(this);
		this.channel = channel;
	}
	
	public QueryFuture<byte[]> sendQuery(byte[] message) {
		QueryCompletableFuture<byte[]> future = new QueryCompletableFuture<>();
		QueryFuture<QueryConnection> fut = connection.sendQuery(channel, message, true);
		if (!queue.add(future)) throw new IllegalStateException("failed to add queue");
		fut.addListener(queryFuture->{
			if (!queryFuture.isSuccess()) {
				future.completeExceptionally(queryFuture.getCause());
				queue.remove(future);
			}
		});
		return future;
	}

	@Override
	public void onConnectionStateChange(QueryConnection connection) {
		if (!connection.isConnected()) {
			synchronized(this.connection) {
				QueryCompletableFuture<byte[]> que;
				while ((que = queue.poll()) != null) que.completeExceptionally(new IOException("connection closed"));
			}
		}
	}

	@Override
	public void onQueryReceived(QueryConnection connection, String channel, byte[] message) {
		if (this.channel.equals(channel)) {
			QueryCompletableFuture<byte[]> que = queue.poll();
			if (que != null) que.complete(message);
		}
	}
	
}
