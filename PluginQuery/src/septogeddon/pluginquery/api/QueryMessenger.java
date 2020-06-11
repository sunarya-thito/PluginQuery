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
	 * Get all connections (including inactive connections)
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
	
}
