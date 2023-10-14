package septogeddon.pluginquery.message;

import java.net.SocketAddress;

public class QueryDispatchSendQuery extends QueryObject {
    private static final long serialVersionUID = 1L;
    private final SocketAddress address;
    private final String channel;
    private final byte[] message;
    private final boolean queue;

    public QueryDispatchSendQuery(SocketAddress address, String channel, byte[] message, boolean queue) {
        this.address = address;
        this.channel = channel;
        this.message = message;
        this.queue = queue;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public boolean isQueue() {
        return queue;
    }

    public String getChannel() {
        return channel;
    }

    public byte[] getMessage() {
        return message;
    }
}
