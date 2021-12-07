package septogeddon.pluginquery.message;

import java.net.SocketAddress;

public class QueryDispatchSendQuery extends QueryObject {
    private static final long serialVersionUID = 1L;
    private SocketAddress address;
    private String channel;
    private byte[] message;
    private boolean queue;

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
