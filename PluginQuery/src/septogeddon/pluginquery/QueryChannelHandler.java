package septogeddon.pluginquery;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryPipeline;
import septogeddon.pluginquery.utils.QueryUtil;

public abstract class QueryChannelHandler {

	QueryChannelHandler parent;
	QueryChannelHandler child;
	protected final String name;
	public QueryChannelHandler(String name) {
		QueryUtil.nonNull(name, "name");
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void onAdded(QueryPipeline pipeline) throws Exception {
	}
	public void onRemoved(QueryPipeline pipeline) throws Exception {
	}
	public void onActive(QueryConnection connection) throws Exception {
		if (child != null) child.onActive(connection);
	}
	public void onInactive(QueryConnection connection) throws Exception {
		if (child != null) child.onInactive(connection);
	}
	public void onHandshake(QueryConnection connection) throws Exception {
		if (child != null) child.onHandshake(connection);
	}
	public byte[] onSending(QueryConnection connection, byte[] bytes) throws Exception {
		if (child != null) return child.onSending(connection, bytes);
		return bytes;
	}
	public byte[] onReceiving(QueryConnection connection, byte[] bytes) throws Exception {
		if (child != null) return child.onReceiving(connection, bytes);
		return bytes;
	}
	public void onUncaughtException(QueryConnection connection, Throwable thrown) throws Exception {
		if (child != null) child.onUncaughtException(connection, thrown);
		else thrown.printStackTrace();
	}
	
}
