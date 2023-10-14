package septogeddon.pluginquery.message;

import java.net.SocketAddress;

public class QueryDispatchDisconnect extends QueryObject {
    private static final long serialVersionUID = 1L;

    private final SocketAddress address;

    public QueryDispatchDisconnect(SocketAddress address) {
        this.address = address;
    }

    public SocketAddress getAddress() {
        return address;
    }
}
