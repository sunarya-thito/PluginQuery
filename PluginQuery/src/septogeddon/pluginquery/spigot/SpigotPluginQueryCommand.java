package septogeddon.pluginquery.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SpigotPluginQueryCommand implements CommandExecutor {

	private String prefix = "&8[&b&lPLUGINQUERY&8] &7";
	private SpigotPluginQuery plugin;
	public SpigotPluginQueryCommand(SpigotPluginQuery pl) {
		plugin = pl;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
				plugin.reloadConfig();
				send(sender, prefix+"Configuration has been reloaded!");
				return true;
			}
		}
		send(sender, prefix+"PluginQuery v"+plugin.getDescription().getVersion()+" by Septogeddon. Usage: /"+label+" <reload>");
		return true;
	}
	
	private void send(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

}
