package septogeddon.pluginquery.http;

public interface ProtocolMethod {
    String name();
    enum General implements ProtocolMethod {
        GET, PUT, POST, PATCH, DELETE, TRACE, CONNECT, OPTIONS, HEAD;
    }
}
