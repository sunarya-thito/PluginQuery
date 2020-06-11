package septogeddon.pluginquery.bungeecord.event;

import net.md_5.bungee.api.plugin.Event;
import septogeddon.pluginquery.api.QueryConnection;

/***
 * This class made to let you migrate from PluginMessaging easily
 * @author Septogeddon
 *
 */
public class QueryMessageEvent extends Event {

	private final String channel;
	private final byte[] message;
	private final QueryConnection connection;
	
	public QueryMessageEvent(QueryConnection connection,String channel, byte[] message) {
		this.connection = connection;
		this.channel = channel;
		this.message = message;
	}
	
	/***
	 * The connection sender
	 * @return
	 * @see #getReceiver()
	 */
	public QueryConnection getSender() {
		return connection;
	}
	
	/***
	 * Synonym of {@link #getSender()}
	 * @return
	 * @see #getSender()
	 */
	public QueryConnection getReceiver() {
		return connection;
	}
	
	/***
	 * The channel
	 * @return
	 * @see #getTag()
	 */
	public String getChannel() {
		return channel;
	}
	
	/***
	 * Synonym of {@link #getChannel()}
	 * @return
	 * @see #getChannel()
	 */
	public String getTag() {
		return channel;
	}
	
	/***
	 * The query message
	 * @return
	 */
	public byte[] getMessage() {
		return message;
	}
	
}
