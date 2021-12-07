package septogeddon.pluginquery.api;

import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Connection instance
 * @author Thito Yalasatria Sunarya
 */
public interface QueryConnection {

    /**
     * Get the channel wrapped by this connection
     * @return Netty Channel
     */
    Channel getChannel();

    /**
     * Get the connection remote address
     * @return Connection Socket Address
     */
    SocketAddress getAddress();

    /**
     * Get the active connection addresses from the other side of this connection as dispatcher connections,
     * where you can send query directly to the connection without having to redirect it on this connection.
     * @return QueryFuture that results a set of active connection addresses
     */
    QueryFuture<Set<QueryConnection>> fetchActiveConnections();

    /**
     * Check if the connection is connected
     * @return true if its connected
     */
    boolean isConnected();

    /**
     * Check if both connection already handshaken and ready to use
     * @return true if the connection already send a handshake packet
     */
    boolean isHandshaken();

    /**
     * Get the messenger
     * @return QueryMessenger instance
     */
    QueryMessenger getMessenger();

    /**
     * Connect to the remote address
     * @return QueryFuture for future handling
     */
    QueryFuture<QueryConnection> connect();

    /**
     * Disconnect the connection. Won't get removed from connection pool, allows you to re-use this connection.
     * @return QueryFuture for future handling
     */
    QueryFuture<QueryConnection> disconnect();

    /**
     * Metadata Storage
     * @return the storage
     */
    QueryMetadata getMetadata();

    /**
     * Event Handler
     * @return the event handler
     */
    QueryEventBus getEventBus();

    /**
     * Send query to this connection
     * @param channel
     * @param message
     * @return QueryFuture for future handling
     */
    QueryFuture<QueryConnection> sendQuery(String channel, byte[] message);

    /**
     * Send query to this connection
     * @param channel
     * @param message
     * @param queue
     * @return QueryFuture for future handling
     */
    QueryFuture<QueryConnection> sendQuery(String channel, byte[] message, boolean queue);

}
