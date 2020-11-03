package septogeddon.pluginquery.http;

/**
 * The protocol listener that handles client/browser request
 */
public interface ProtocolListener {
    /**
     * Called when there is a request coming from a client
     * @param client the client
     * @param request the request data
     */
    void onRequest(ProtocolClient client, ProtocolRequest request);
}
