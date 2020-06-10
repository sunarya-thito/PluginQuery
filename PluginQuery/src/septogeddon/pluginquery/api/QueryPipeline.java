package septogeddon.pluginquery.api;

import septogeddon.pluginquery.QueryChannelHandler;

public interface QueryPipeline {

	public boolean remove(String str);
	public default boolean remove(QueryChannelHandler handler) {
		return remove(handler.getName());
	}
	public boolean addFirst(QueryChannelHandler handler);
	public boolean addBefore(String before, QueryChannelHandler handler);
	public boolean addAfter(String after, QueryChannelHandler handler);
	public boolean addLast(QueryChannelHandler handler);
	public default boolean addLast(QueryChannelHandler... handlers) {
		boolean added = false;
		for (QueryChannelHandler handler : handlers) {
			if (addLast(handler)) added = true;
		}
		return added;
	}
	public default boolean addFirst(QueryChannelHandler... handlers) {
		boolean added = false;
		for (int i = handlers.length-1; i>=0; i--) {
			if (addFirst(handlers[i])) {
				added = true;
			}
		}
		return added;
	}
	public <T extends QueryChannelHandler> T get(String key);
	public void dispatchActive(QueryConnection connection);
	public void dispatchInactive(QueryConnection connection);
	public byte[] dispatchSending(QueryConnection connection, byte[] bytes);
	public byte[] dispatchReceiving(QueryConnection connection, byte[] bytes);
	public void dispatchUncaughtException(QueryConnection connection, Throwable thrown);

}
