package septogeddon.pluginquery.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import septogeddon.pluginquery.InjectedQueryConnection;
import septogeddon.pluginquery.PreparedQueryConnection;
import septogeddon.pluginquery.QueryMessage;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.message.*;
import septogeddon.pluginquery.utils.Debug;

import java.util.stream.Collectors;

public class QueryManager extends SimpleChannelInboundHandler<QueryMessage> {

    private final QueryProtocol protocol;

    public QueryManager(QueryProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext arg0, QueryMessage arg1) throws Exception {
        Debug.debug("Manager: RECEIVED: " + arg1.getChannel());
        if (arg1.getChannel().equals(QueryContext.REDIRECT_MESSAGING_CHANNEL)) {
            QueryObject queryObject = QueryObject.fromByteArraySafe(arg1.getMessage());
            Debug.debug("Received redirect message: "+queryObject);
            if (queryObject instanceof QueryGetActiveConnections) {
                protocol.getConnection().sendQuery(arg1.getChannel(), new QuerySendActiveConnections(protocol.getMessenger().getActiveConnections().stream().map(QueryConnection::getAddress).collect(Collectors.toList())).toByteArraySafe());
            } else if (queryObject instanceof QuerySendActiveConnections && protocol.getConnection() instanceof InjectedQueryConnection) {
                ((InjectedQueryConnection) protocol.getConnection()).consumeQueryConnections(((QuerySendActiveConnections) queryObject).getAddresses());
            } else if (queryObject instanceof QueryDispatchDisconnect) {
                protocol.getMessenger().getActiveConnections().stream().filter(connection -> connection.getAddress().equals(((QueryDispatchDisconnect) queryObject).getAddress()))
                        .findAny().ifPresent(QueryConnection::disconnect);
            } else if (queryObject instanceof QueryDispatchConnect) {
                protocol.getMessenger().getActiveConnections().stream().filter(connection -> connection.getAddress().equals(((QueryDispatchConnect) queryObject).getAddress()))
                        .findAny().ifPresent(QueryConnection::connect);
            } else if (queryObject instanceof QueryDispatchSendQuery) {
                protocol.getMessenger().getActiveConnections().stream().filter(connection -> connection.getAddress().equals(((QueryDispatchSendQuery) queryObject).getAddress()))
                        .findAny().ifPresent(queryConnection -> queryConnection.sendQuery(
                            ((QueryDispatchSendQuery) queryObject).getChannel(),
                            ((QueryDispatchSendQuery) queryObject).getMessage(),
                            ((QueryDispatchSendQuery) queryObject).isQueue()
                        ));
            }
        }
        protocol.getConnection().getEventBus().dispatchMessage(protocol.getConnection(), arg1.getChannel(), arg1.getMessage());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        protocol.getConnection().disconnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    }

}
