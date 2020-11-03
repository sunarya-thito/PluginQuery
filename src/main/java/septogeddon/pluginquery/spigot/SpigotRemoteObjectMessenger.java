package septogeddon.pluginquery.spigot;

import org.bukkit.Server;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.library.remote.RemoteObjectProvider;

public class SpigotRemoteObjectMessenger extends RemoteObjectProvider<Server> {

    SpigotRemoteObjectMessenger(QueryMessenger messenger, String channel, Server object) {
        super(messenger, channel, object);
    }

}
