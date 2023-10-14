package septogeddon.pluginquery.message;

import java.net.SocketAddress;
import java.util.List;

public class QuerySendActiveConnections extends QueryObject {
    private static final long serialVersionUID = 1L;
    private final List<SocketAddress> addresses;

    public QuerySendActiveConnections(List<SocketAddress> addresses) {
        this.addresses = addresses;
    }

    public List<SocketAddress> getAddresses() {
        return addresses;
    }
}
