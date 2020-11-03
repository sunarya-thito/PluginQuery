package septogeddon.pluginquery.http;

public interface ProtocolListener {
    void onRequest(ProtocolClient client, ProtocolRequest request);
}
