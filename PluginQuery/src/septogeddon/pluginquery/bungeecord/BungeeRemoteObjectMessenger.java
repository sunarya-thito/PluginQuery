package septogeddon.pluginquery.bungeecord;

import net.md_5.bungee.api.ProxyServer;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.library.remote.RemoteObjectProvider;

public class BungeeRemoteObjectMessenger extends RemoteObjectProvider<ProxyServer> {

	BungeeRemoteObjectMessenger(QueryMessenger messenger, String channel, ProxyServer object) {
		super(messenger, channel, object);
	}

	
}
