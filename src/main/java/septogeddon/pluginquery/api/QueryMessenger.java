package septogeddon.pluginquery.api;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.net.SocketAddress;
import java.util.Collection;

/**
 * HUB for active connections
 * @author Thito Yalasatria Sunarya
 */
// add USE
public interface QueryMessenger {

    /**
     * Prepare new connection using specified address
     * @param address
     * @return
     */
    QueryConnection newConnection(SocketAddress address);

    /**
     * Inject served channel to handle query protocols
     * @param channel
     * @return
     */
    QueryConnection injectConnection(Channel channel);

    /**
     * Get all connections (inactive connections aren't included here, they're all goes GC'ed when you stop referring to them)
     * @return
     */
    Collection<? extends QueryConnection> getActiveConnections();

    /**
     * Get Messenger metadata
     * @return
     */
    QueryMetadata getMetadata();

    /**
     * Event Manager that listen to all connections
     * @return
     */
    QueryEventBus getEventBus();

    /**
     * Pipeline for all connections
     * @return
     */
    QueryPipeline getPipeline();

    /**
     * Netty Event Loop Group
     * @return
     */
    EventLoopGroup getEventLoopGroup();

    /**
     * Netty Channel Class
     * @return
     */
    Class<? extends Channel> getChannelClass();

    /**
     * broadcast query to all active connections
     * @param channel
     * @param message
     * @return true if there is at least 1 active connection
     */
    default boolean broadcastQuery(String channel, byte[] message) {
        int count = 0;
        for (QueryConnection connection : getActiveConnections()) {
            connection.sendQuery(channel, message);
            count++;
        }
        return count != 0;
    }

    /**
     * broadcast query to all active connections
     * @param channel
     * @param message
     * @param queue
     * @return
     */
    default boolean broadcastQuery(String channel, byte[] message, boolean queue) {
        int count = 0;
        for (QueryConnection connection : getActiveConnections()) {
            connection.sendQuery(channel, message, queue);
            count++;
        }
        return count != 0;
    }

}
