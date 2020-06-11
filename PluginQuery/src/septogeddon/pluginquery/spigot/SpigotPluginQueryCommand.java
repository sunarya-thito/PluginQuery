package septogeddon.pluginquery.spigot;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

public class SpigotPluginQueryCommand implements CommandExecutor {

	private SpigotPluginQuery plugin;
	public SpigotPluginQueryCommand(SpigotPluginQuery pl) {
		plugin = pl;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String prefix = QueryContext.COMMAND_PREFIX;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
				plugin.reloadConfig();
				send(sender, prefix+"Configuration has been reloaded!");
				return true;
			}
			if (args[0].equalsIgnoreCase("check")) {
				ArrayList<String> str = new ArrayList<>();
				for (QueryConnection con : PluginQuery.getMessenger().getActiveConnections()) {
					str.add(con.isConnected() ? "&a"+con.getAddress()+"&7" : "&c"+con.getAddress()+"&7");
				}
				send(sender, prefix+"Servers (&e"+str.size()+"&7)&8: &7"+String.join(", ", str));
				return true;
			}
		}
		send(sender, prefix+"PluginQuery v"+plugin.getDescription().getVersion()+" by Septogeddon. Usage: &b/"+label+" <reload|check>");
		return true;
	}
	
	private void send(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

}
