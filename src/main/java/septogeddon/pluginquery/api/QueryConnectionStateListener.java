package septogeddon.pluginquery.api;

/**
 * Listen to {@link #onConnectionStateChange(QueryConnection)} only
 * @author Thito Yalasatria Sunarya
 * @see QueryListener
 */
public interface QueryConnectionStateListener extends QueryListener {

    /**
     * Does nothing
     */
    default void onQueryReceived(QueryConnection connection, String channel, byte[] message) {
    }

}
