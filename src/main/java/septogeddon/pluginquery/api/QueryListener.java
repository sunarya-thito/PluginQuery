package septogeddon.pluginquery.api;

/**
 * Listen to Connection State Change and Incoming Query Message
 * @author Thito Yalasatria Sunarya
 *
 */
public interface QueryListener {

    /**
     * Called when {@link QueryConnection#isConnected()} value changed
     * @param connection
     * @throws Throwable any error that could possibly happen during the event listener execution
     */
    void onConnectionStateChange(QueryConnection connection) throws Throwable;

    /**
     * Called when the connection received a query message
     * @param connection
     * @param channel
     * @param message
     * @throws Throwable any error that could possibly happen during the event listener execution
     */
    void onQueryReceived(QueryConnection connection, String channel, byte[] message) throws Throwable;

}
