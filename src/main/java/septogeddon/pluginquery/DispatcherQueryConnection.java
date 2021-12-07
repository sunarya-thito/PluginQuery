package septogeddon.pluginquery;

import io.netty.channel.Channel;
import septogeddon.pluginquery.api.*;
import septogeddon.pluginquery.message.QueryDispatchConnect;
import septogeddon.pluginquery.message.QueryDispatchDisconnect;
import septogeddon.pluginquery.message.QueryDispatchSendQuery;

import java.net.SocketAddress;
import java.util.Set;

public class DispatcherQueryConnection implements QueryConnection {

    private SocketAddress address;
    private PreparedQueryConnection delegated;
    private QueryEventBus queryEventBus = new QueryEventBusImpl();
    private QueryMetadata queryMetadata = new QueryMetadataImpl();

    public DispatcherQueryConnection(SocketAddress address, PreparedQueryConnection delegated) {
        this.address = address;
        this.delegated = delegated;
    }

    @Override
    public Channel getChannel() {
        return delegated.getChannel();
    }

    @Override
    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public QueryFuture<Set<QueryConnection>> fetchActiveConnections() {
        return delegated.fetchActiveConnections();
    }

    @Override
    public boolean isConnected() {
        return delegated.isConnected();
    }

    @Override
    public boolean isHandshaken() {
        return delegated.isHandshaken();
    }

    @Override
    public QueryMessenger getMessenger() {
        return delegated.getMessenger();
    }

    @Override
    public QueryFuture<QueryConnection> connect() {
        return delegated.sendQuery(QueryContext.PLUGIN_MESSAGING_CHANNEL, new QueryDispatchConnect(address).toByteArraySafe());
    }

    @Override
    public QueryFuture<QueryConnection> disconnect() {
        return delegated.sendQuery(QueryContext.PLUGIN_MESSAGING_CHANNEL, new QueryDispatchDisconnect(address).toByteArraySafe());
    }

    @Override
    public QueryMetadata getMetadata() {
        return queryMetadata;
    }

    @Override
    public QueryEventBus getEventBus() {
        return queryEventBus;
    }

    @Override
    public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message) {
        return delegated.sendQuery(QueryContext.PLUGIN_MESSAGING_CHANNEL, new QueryDispatchSendQuery(address, channel, message, true).toByteArraySafe(), true);
    }

    @Override
    public QueryFuture<QueryConnection> sendQuery(String channel, byte[] message, boolean queue) {
        return delegated.sendQuery(QueryContext.PLUGIN_MESSAGING_CHANNEL, new QueryDispatchSendQuery(address, channel, message, queue).toByteArraySafe(), queue);
    }
}
