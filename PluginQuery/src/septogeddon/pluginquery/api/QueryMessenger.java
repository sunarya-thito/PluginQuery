package septogeddon.pluginquery.api;

import java.net.SocketAddress;
import java.util.Collection;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

public interface QueryMessenger {

	/***
	 * Prepare new connection using specified address
	 * @param address
	 * @return
	 */
	public QueryConnection newConnection(SocketAddress address);
	/***
	 * Inject served channel to handle query protocols
	 * @param channel
	 * @return
	 */
	public QueryConnection injectConnection(Channel channel);
	/***
	 * Get all connections (inactive connections aren't included here, they're all goes GC'ed when you stop referring to them)
	 * @return
	 */
	public Collection<? extends QueryConnection> getActiveConnections();
	/***
	 * Get Messenger metadata
	 * @return
	 */
	public QueryMetadata getMetadata();
	/***
	 * Event Manager that listen to all connections
	 * @return
	 */
	public QueryEventBus getEventBus();
	/***
	 * Pipeline for all connections
	 * @return
	 */
	public QueryPipeline getPipeline();
	/***
	 * Netty Event Loop Group
	 * @return
	 */
	public EventLoopGroup getEventLoopGroup();
	/***
	 * Netty Channel Class
	 * @return
	 */
	public Class<? extends Channel> getChannelClass();
	/***
	 * broadcast query to all active connections
	 * @param channel
	 * @param message
	 * @return true if there is at least 1 active connection
	 */
	public default boolean broadcastQuery(String channel, byte[] message) {
		int count = 0;
		for (QueryConnection connection : getActiveConnections()) {
			connection.sendQuery(channel, message);
			count++;
		}
		return count != 0;
	}
	
	/***
	 * broadcast query to all active connections
	 * @param channel
	 * @param message
	 * @param queue
	 * @return
	 */
	public default boolean broadcastQuery(String channel, byte[] message, boolean queue) {
		int count = 0;
		for (QueryConnection connection : getActiveConnections()) {
			connection.sendQuery(channel, message, queue);
			count++;
		}
		return count != 0;
	}
	
}
