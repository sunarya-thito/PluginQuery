package septogeddon.pluginquery.message;

import java.net.SocketAddress;

public class QueryDispatchConnect extends QueryObject {
    private static final long serialVersionUID = 1L;
    private SocketAddress address;

    public QueryDispatchConnect(SocketAddress address) {
        this.address = address;
    }

    public SocketAddress getAddress() {
        return address;
    }
}
