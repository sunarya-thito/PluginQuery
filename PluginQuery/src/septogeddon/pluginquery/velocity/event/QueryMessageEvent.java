package septogeddon.pluginquery.velocity.event;

import net.kyori.event.Cancellable;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

/***
 * This class made to let you migrate from PluginMessaging easily. This event does not listen to {@link QueryContext#PLUGIN_MESSAGING_CHANNEL}
 * @author Septogeddon
 *
 */
public class QueryMessageEvent implements Cancellable {

	private final String channel;
	private final byte[] message;
	private final QueryConnection connection;
	private boolean cancel;
	
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
	 * @see #getData()
	 */
	public byte[] getMessage() {
		return message;
	}
	
	/***
	 * Synonym of {@link #getMessage()}
	 * @return
	 * @see #getMessage()
	 */
	public byte[] getData() {
		return message;
	}

	/***
	 * Check if this event has been cancelled by previous lower priority listener
	 */
	@Override
	public boolean cancelled() {
		return cancel;
	}
	
	/***
	 * Cancel this event
	 */
	@Override
	public void cancelled(boolean arg0) {
		this.cancel = arg0;
	}
	
}