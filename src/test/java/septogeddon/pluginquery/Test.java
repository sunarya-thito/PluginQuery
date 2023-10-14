package septogeddon.pluginquery;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.api.QueryMetadataKey;
import septogeddon.pluginquery.spigot.SpigotPluginQuery;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Test {
    public static void main(String[]args) {
        QueryMessenger m = PluginQuery.getMessenger();
        m.getActiveConnections().forEach(conn -> {
            conn.fetchActiveConnections().thenAccept(conns -> {
                conns.forEach(con -> {
                    con.sendQuery("myplugin:mymessage", "HELLO".getBytes(StandardCharsets.UTF_8));
                });
            });
        });
//QueryConnection proxyConnection;
//InetAddress targetAddress;
//proxyConnection.fetchActiveConnections()
//        .thenAccept(spigotConnections -> {
//            for (QueryConnection spigotConnection : spigotConnections) {
//                if (spigotConnection.getAddress().equals(targetAddress)) {
//                    spigotConnection.sendQuery("myplugin:mymessage", "HELLO".getBytes(StandardCharsets.UTF_8));
//                }
//            }
//        });
    }
}
