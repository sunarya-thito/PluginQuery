package septogeddon.pluginquery.spigot.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryEventBus;

/**
 * Listen to received Query Message. Does not listen to {@link QueryContext#PLUGIN_MESSAGING_CHANNEL} channel.
 * @author Septogeddon
 *
 */
public class QueryMessageEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final String channel;
    private final byte[] message;
    private final QueryConnection connection;
    private boolean cancel;

    public QueryMessageEvent(QueryConnection connection, String channel, byte[] message) {
        super(!Bukkit.isPrimaryThread());
        this.connection = connection;
        this.channel = channel;
        this.message = message;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    /**
     * Get the connection
     * @return
     */
    public QueryConnection getConnection() {
        return connection;
    }

    /**
     * Check if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Cancel the event to stop the event pipeline. Does not cancel other listeners from {@link QueryEventBus}
     */
    @Override
    public void setCancelled(boolean arg0) {
        cancel = arg0;
    }

    /**
     * The query channel
     * @return
     */
    public String getChannel() {
        return channel;
    }

    /**
     * The query message
     * @return
     */
    public byte[] getMessage() {
        return message;
    }

}
