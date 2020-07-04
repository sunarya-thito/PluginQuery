package septogeddon.pluginquery.bungeecord;

import java.util.ArrayList;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.utils.DataBuffer;
import septogeddon.pluginquery.utils.Debug;
import septogeddon.pluginquery.utils.EncryptionToolkit;

public class BungeePluginQueryCommand extends Command {

	private BungeePluginQuery plugin;
	public BungeePluginQueryCommand(BungeePluginQuery query) {
		super("pluginquery", QueryContext.ADMIN_PERMISSION, "pq","pluginq","pquery","query");
		plugin = query;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String prefix = QueryContext.COMMAND_PREFIX;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("sync") || args[0].equalsIgnoreCase("synchronize")) {
				if (sender instanceof ProxiedPlayer) {
					ProxiedPlayer player = (ProxiedPlayer)sender;
					EncryptionToolkit toolkit = plugin.getEncryption();
					DataBuffer buffer = new DataBuffer();
					buffer.writeUTF(QueryContext.REQUEST_KEY_SHARE);
					buffer.write(toolkit.encode());
					player.getServer().sendData(QueryContext.PLUGIN_MESSAGING_CHANNEL, buffer.toByteArray());
					plugin.sendMessage(sender, prefix+"Synchronizing secret.key ...");
				} else {
					plugin.sendMessage(sender, prefix+"&cYou must be a player to do this!");
				}
				return;
			}
			if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
				plugin.reloadConfig();
				plugin.sendMessage(sender, prefix+"Configuration has been reloaded!");
				return;
			}
			if (args[0].equalsIgnoreCase("check")) {
				ArrayList<String> str = new ArrayList<>();
				for (QueryConnection con : PluginQuery.getMessenger().getActiveConnections()) {
					ServerInfo info = con.getMetadata().getData(BungeePluginQuery.SERVER_INFO);
					if (info == null) continue;
					str.add(con.isConnected() ? "&a"+info.getName()+"&7" : "&c"+info.getName()+"&7");
				}
				plugin.sendMessage(sender, prefix+"Servers (&e"+str.size()+"&7)&8: &7"+String.join(", ", str));
				return;
			}
			if (args[0].equalsIgnoreCase("debug")) {
				Debug.STATE_DEBUG = !Debug.STATE_DEBUG;
				if (Debug.STATE_DEBUG) {
					plugin.sendMessage(sender, prefix+"Debug mode has been enabled");
				} else {
					plugin.sendMessage(sender, prefix+"Debug mode has been disabled");
				}
				return;
			}
		}
		plugin.sendMessage(sender, prefix+"PluginQuery v"+plugin.getDescription().getVersion()+" by Septogeddon. Usage: &f/pq <sync|reload|check|debug>");
	}
	
}
