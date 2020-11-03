package septogeddon.pluginquery.api;

/**
 * Listen to {@link #onQueryReceived(QueryConnection, String, byte[])} only
 * @author Thito Yalasatria Sunarya
 * @see QueryListener
 */
public interface QueryMessageListener extends QueryListener {

    /**
     * Does nothing
     */
    default void onConnectionStateChange(QueryConnection connection) {
    }

}
