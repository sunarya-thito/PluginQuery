package septogeddon.pluginquery.api;

import java.util.Collection;

import septogeddon.pluginquery.QueryChannelHandler;

public interface QueryPipeline {

	/***
	 * Remove handler from pipeline
	 * @param str
	 * @return
	 */
	public boolean remove(String str);
	/***
	 * Remove handler from pipeline
	 * @param handler
	 * @return
	 */
	public default boolean remove(QueryChannelHandler handler) {
		return remove(handler.getName());
	}
	/***
	 * Add handler at the first index
	 * @param handler
	 * @return
	 */
	public boolean addFirst(QueryChannelHandler handler);
	/***
	 * Add handler before another handler
	 * @param before
	 * @param handler
	 * @return
	 */
	public boolean addBefore(String before, QueryChannelHandler handler);
	/***
	 * Add handler after another handler
	 * @param after
	 * @param handler
	 * @return
	 */
	public boolean addAfter(String after, QueryChannelHandler handler);
	/***
	 * Add handler at the last index
	 * @param handler
	 * @return
	 */
	public boolean addLast(QueryChannelHandler handler);
	/***
	 * Find the handler next to this handler
	 * @param of
	 * @return
	 */
	public QueryChannelHandler nextHandler(QueryChannelHandler of);
	/***
	 * The first handler on this pipeline
	 * @return
	 */
	public QueryChannelHandler first();
	/***
	 * The last handler on this pipeline
	 * @return
	 */
	public QueryChannelHandler last();
	/***
	 * Add handlers at the last index
	 * @param handlers
	 * @return
	 */
	public default boolean addLast(QueryChannelHandler... handlers) {
		boolean added = false;
		for (QueryChannelHandler handler : handlers) {
			if (addLast(handler)) added = true;
		}
		return added;
	}
	/***Que
	 * Add handlers at the first index
	 * @param handlers
	 * @return
	 */
	public default boolean addFirst(QueryChannelHandler... handlers) {
		boolean added = false;
		for (int i = handlers.length-1; i>=0; i--) {
			if (addFirst(handlers[i])) {
				added = true;
			}
		}
		return added;
	}
	/***
	 * Get handler by name
	 * @param <T>
	 * @param key
	 * @return
	 */
	public <T extends QueryChannelHandler> T get(String key);
	/***
	 * Get all handlers
	 * @return
	 */
	public Collection<? extends QueryChannelHandler> getPipes();
	/***
	 * Call {@link QueryChannelHandler#onActive(QueryConnection)} on all handlers
	 * @param connection
	 */
	public void dispatchActive(QueryConnection connection);
	/***
	 * Call {@link QueryChannelHandler#onInactive(QueryConnection)} on all handlers
	 * @param connection
	 */
	public void dispatchInactive(QueryConnection connection);
	/***
	 * Call {@link QueryChannelHandler#onSending(QueryConnection, byte[])} on all handlers
	 * @param connection
	 * @param bytes
	 * @return modified byte
	 */
	public byte[] dispatchSending(QueryConnection connection, byte[] bytes);
	/***
	 * Call {@link QueryChannelHandler#onReceiving(QueryConnection, byte[])} on all handlers
	 * @param connection
	 * @param bytes
	 * @return modified byte
	 */
	public byte[] dispatchReceiving(QueryConnection connection, byte[] bytes);
	/***
	 * Call {@link QueryChannelHandler#onCaughtException(QueryConnection, Throwable)} on all handlers
	 * @param connection
	 * @param thrown
	 */
	public void dispatchUncaughtException(QueryConnection connection, Throwable thrown);

}
