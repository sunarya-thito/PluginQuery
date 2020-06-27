package septogeddon.pluginquery.velocity;

import com.velocitypowered.api.proxy.ProxyServer;

import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.library.remote.RemoteObjectProvider;

public class VelocityRemoteObjectMessenger extends RemoteObjectProvider<ProxyServer> {

	VelocityRemoteObjectMessenger(QueryMessenger messenger, String channel, ProxyServer object) {
		super(messenger, channel, object);
	}

}
