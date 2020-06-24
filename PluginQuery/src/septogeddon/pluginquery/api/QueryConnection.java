package septogeddon.pluginquery.api;

import java.net.SocketAddress;

import io.netty.channel.Channel;

/***
 * Connection instance
 * @author Thito Yalasatria Sunarya
 */
public interface QueryConnection {

	/***
	 * Get the channel wrapped by this connection
	 * @return Netty Channel
	 */
	public Channel getChannel();
	/***
	 * Get the connection remote address
	 * @return Connection Socket Address
	 */
	public SocketAddress getAddress();
	/***
	 * Check if the connection is connected
	 * @return true if its connected
	 */
	public boolean isConnected();
	/***
	 * Check if both connection already handshaken and ready to use
	 * @return true if the connection already send a handshake packet
	 */
	public boolean isHandshaken();
	/***
	 * Get the messenger
	 * @return QueryMessenger instance
	 */
	public QueryMessenger getMessenger();
	/***
	 * Connect to the remote address
	 * @return QueryFuture for future handling
	 */
	public QueryFuture<QueryConnection> connect();
	/***
	 * Disconnect the connection. Won't get removed from connection pool, allows you to re-use this connection.
	 * @see QueryMessenger#closeConnection(QueryConnection)
	 * @return QueryFuture for future handling
	 */
	public QueryFuture<QueryConnection> disconnect();
	/***
	 * Metadata Storage
	 * @return the storage
	 */
	public QueryMetadata getMetadata();
	/***
	 * Event Handler
	 * @return the event handler
	 */
	public QueryEventBus getEventBus();
	/***
	 * Send query to this connection
	 * @param channel
	 * @param message
	 * @return QueryFuture for future handling
	 */
	public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message);
	/***
	 * Send query to this connection
	 * @param channel
	 * @param message
	 * @param queue
	 * @return QueryFuture for future handling
	 */
	public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message, boolean queue);
	
}
