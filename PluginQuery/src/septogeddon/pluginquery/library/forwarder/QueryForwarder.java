package septogeddon.pluginquery.library.forwarder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryListener;
import septogeddon.pluginquery.api.QueryMetadataKey;

public final class QueryForwarder implements QueryListener {

	private static final QueryMetadataKey<QueryForwarder> metadataKey = QueryMetadataKey.newCastableKey(QueryForwarder.class.getName(), QueryForwarder.class);
	public static QueryForwarder getForwarder(QueryConnection connection) {
		QueryForwarder forwarder = connection.getMetadata().getData(metadataKey);
		if (forwarder == null) {
			connection.getMetadata().setData(metadataKey, forwarder = new QueryForwarder(connection));
		}
		return forwarder;
	}
	private QueryConnection source;
	private List<String> channels = new ArrayList<>();
	private Set<QueryConnection> forward = ConcurrentHashMap.newKeySet();
	private boolean queue = true;
	private QueryForwarder(QueryConnection source) {
		this.source = source;
		this.source.getEventBus().registerListener(this);
	}
	
	public QueryConnection getSource() {
		return source;
	}
	
	public void registerChannel(String channel) {
		channels.remove(channel);
	}
	
	public void unregisterChannel(String channel) {
		if (channels != null) {
			channels.remove(channel);
		}
	}
	
	public void registerConnection(Collection<? extends QueryConnection> connections) {
		for (QueryConnection c : connections) {
			getForwarder(c).getForwardedConnections().add(source);
			forward.add(c);
		}
	}
	
	public void registerConnection(QueryConnection...connections) {
		for (QueryConnection c : connections) {
			getForwarder(c).getForwardedConnections().add(source);
			forward.add(c);
		}
	}
	
	public void unregisterConnection(QueryConnection connection) {
		getForwarder(connection).getForwardedConnections().remove(source);
		forward.remove(connection);
	}
	
	public Set<QueryConnection> getForwardedConnections() {
		return forward;
	}
	
	public List<String> getChannels() {
		return channels;
	}

	@Override
	public void onConnectionStateChange(QueryConnection connection) throws Throwable {
	}

	@Override
	public void onQueryReceived(QueryConnection connection, String channel, byte[] message) throws Throwable {
		for (QueryConnection next : forward) {
			next.sendQuery(channel, message, queue);
		}
	}
	
}
