package septogeddon.pluginquery.bungeecord;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.utils.DataBuffer;
import septogeddon.pluginquery.utils.EncryptionToolkit;

public class BungeePluginQueryCommand extends Command {

	private BungeePluginQuery plugin;
	public BungeePluginQueryCommand(BungeePluginQuery query) {
		super("pluginquery", QueryContext.ADMIN_PERMISSION, "pq","pluginq","pquery","query");
		plugin = query;
	}
	
	private String prefix = "&8[&b&lPLUGINQUERY&8] &7";

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("sync") || args[0].equalsIgnoreCase("synchronize")) {
				if (sender instanceof ProxiedPlayer) {
					ProxiedPlayer player = (ProxiedPlayer)sender;
					EncryptionToolkit toolkit = plugin.getEncryption();
					plugin.sendMessage(sender, prefix+"Synchronizing secret.key ...");
					DataBuffer buffer = new DataBuffer();
					buffer.writeUTF(QueryContext.REQUEST_KEY_SHARE);
					buffer.writeBytes(new String(toolkit.getKey().getEncoded()));
					player.sendData(QueryContext.PLUGIN_MESSAGING_CHANNEL, buffer.toByteArray());
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
		}
		plugin.sendMessage(sender, prefix+"PluginQuery v"+plugin.getDescription().getVersion()+" by Septogeddon. Usage: &f/pq <sync|reload>");
	}

}
