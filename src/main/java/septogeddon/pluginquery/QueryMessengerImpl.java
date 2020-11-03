package septogeddon.pluginquery;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import septogeddon.pluginquery.api.*;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryMessengerImpl implements QueryMessenger {

    protected final List<QueryConnection> connections = new ArrayList<>();
    private final QueryMetadata metadata = new QueryMetadataImpl();
    private final QueryEventBus eventBus = new QueryEventBusImpl();
    private final QueryPipeline pipeline = new QueryPipelineImpl();
    private EventLoopGroup eventLoop;
    private Class<? extends Channel> channelClass;

    public QueryMessengerImpl() {
        try {
            if (io.netty.channel.epoll.Epoll.isAvailable()) {
                eventLoop = new io.netty.channel.epoll.EpollEventLoopGroup();
                channelClass = io.netty.channel.epoll.EpollSocketChannel.class;
            } else {
                eventLoop = new NioEventLoopGroup();
                channelClass = NioSocketChannel.class;
            }
        } catch (Throwable t) {
            eventLoop = new NioEventLoopGroup();
            channelClass = NioSocketChannel.class;
        }
    }

    @Override
    public QueryConnection newConnection(SocketAddress address) {
        PreparedQueryConnection conn = new PreparedQueryConnection(this, address) {

            @Override
            protected void connectionDisconnected() {
                synchronized (connections) {
                    connections.remove(this);
                }
                super.connectionDisconnected();
            }

            @Override
            protected void connectionConnected() {
                super.connectionConnected();
                synchronized (connections) {
                    connections.add(this);
                }
            }

        };
        conn.getEventBus().addParent(getEventBus());
        conn.getMetadata().addParent(getMetadata());
        return conn;
    }

    @Override
    public QueryConnection injectConnection(Channel channel) {
        synchronized (connections) {
            for (int i = 0; i < connections.size(); i++) {
                QueryConnection conn = connections.get(i);
                if (channel.equals(conn.getChannel())) {
                    // already injected, wont inject any other.
                    return conn;
                }
            }
        }
        InjectedQueryConnection conn = new InjectedQueryConnection(this, channel) {

            @Override
            protected void connectionDisconnected() {
                synchronized (connections) {
                    connections.remove(this);
                }
                super.connectionDisconnected();
            }

            @Override
            protected void connectionConnected() {
                super.connectionConnected();
                synchronized (connections) {
                    connections.add(this);
                }
            }

        };
        conn.getEventBus().addParent(getEventBus());
        conn.getMetadata().addParent(getMetadata());
        return conn;
    }

    @Override
    public Collection<? extends QueryConnection> getActiveConnections() {
        return new ArrayList<>(connections);
    }

    @Override
    public QueryMetadata getMetadata() {
        return metadata;
    }

    @Override
    public QueryEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public QueryPipeline getPipeline() {
        return pipeline;
    }

    @Override
    public EventLoopGroup getEventLoopGroup() {
        return eventLoop;
    }

    @Override
    public Class<? extends Channel> getChannelClass() {
        return channelClass;
    }
}
