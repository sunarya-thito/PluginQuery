package septogeddon.pluginquery.api;

import java.net.SocketAddress;

import io.netty.channel.Channel;

public interface QueryConnection {

	/***
	 * Get the channel wrapped by this connection
	 * @return
	 */
	public Channel getChannel();
	/***
	 * Get the connection remote address
	 * @return
	 */
	public SocketAddress getAddress();
	/***
	 * Check if the connection is connected
	 * @return
	 */
	public boolean isConnected();
	/***
	 * Check if the connection is currently trying to connect
	 * @return
	 */
	public boolean isConnecting();
	/***
	 * Get the messenger
	 * @return
	 */
	public QueryMessenger getMessenger();
	/***
	 * Connect to the remote address
	 * @return
	 */
	public QueryFuture<QueryConnection> connect();
	/***
	 * Disconnect the connection. Won't get removed from connection pool, allows you to re-use this connection.
	 * @see QueryMessenger#closeConnection(QueryConnection)
	 */
	public void disconnect();
	/***
	 * Metadata Storage
	 * @return
	 */
	public QueryMetadata getMetadata();
	/***
	 * Event Handler
	 * @return
	 */
	public QueryEventBus getEventBus();
	/***
	 * Send query to this connection
	 * @param channel
	 * @param message
	 * @return
	 */
	public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message);
	/***
	 * Send query to this connection
	 * @param channel
	 * @param message
	 * @param queue
	 * @return
	 */
	public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message, boolean queue);
	
}
