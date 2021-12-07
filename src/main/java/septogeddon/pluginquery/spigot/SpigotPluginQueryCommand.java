package septogeddon.pluginquery.spigot;

import io.netty.channel.Channel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.message.QueryBroadcastMessage;
import septogeddon.pluginquery.utils.Debug;

import java.util.ArrayList;

public class SpigotPluginQueryCommand implements CommandExecutor {

    private final SpigotPluginQuery plugin;

    public SpigotPluginQueryCommand(SpigotPluginQuery pl) {
        plugin = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = QueryContext.COMMAND_PREFIX;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                plugin.reloadConfig();
                send(sender, prefix + "Configuration has been reloaded!");
                return true;
            }
            if (args[0].equalsIgnoreCase("check")) {
                ArrayList<String> str = new ArrayList<>();
                ArrayList<String> l = new ArrayList<>();
                for (Channel c : plugin.getListeners()) {
                    l.add(c.toString());
                }
                for (QueryConnection con : PluginQuery.getMessenger().getActiveConnections()) {
                    str.add(con.isConnected() ? "&a" + con.getAddress() + "&7" : "&c" + con.getAddress() + "&7");
                }
                send(sender, prefix + "Servers (&e" + str.size() + "&7)&8: &7" + String.join(", ", str));
                send(sender, prefix + "Listeners (&e" + l.size() + "&7)&8: &7" + String.join(", ", l));
                return true;
            }
            if (args[0].equalsIgnoreCase("debug")) {
                Debug.STATE_DEBUG = !Debug.STATE_DEBUG;
                send(sender, prefix + (Debug.STATE_DEBUG ? "Debug mode has been enabled" : "Debug mode has been disabled"));
                return true;
            }
            if (args[0].equalsIgnoreCase("broadcastMessage")) {
                if (args.length > 1) {
                    String msg = args[1];
                    for (int i = 2; i < args.length; i++) msg += " " + args[i];
                    for (QueryConnection con : PluginQuery.getMessenger().getActiveConnections()) {
                        String finalMsg = msg;
                        con.fetchActiveConnections().thenAccept(result -> {
                            if (result.isEmpty()) {
                                send(sender, prefix + "There is no active connections available.");
                                return;
                            }
                            for (QueryConnection connection : result) {
                                send(sender, prefix + "Sending message to "+connection.getAddress());
                                connection.sendQuery(QueryContext.PLUGIN_MESSAGING_CHANNEL, new QueryBroadcastMessage(finalMsg).toByteArraySafe()).thenAccept(result2 -> {
                                    send(sender, prefix + "Message sent to "+result2.getAddress());
                                });
                            }
                        });
                    }
                    return true;
                }
                send(sender, prefix + "Broadcast a message to all active connections. Usage: /"+label+" broadcastMessage <message>");
                return true;
            }
        }
        send(sender, prefix + "PluginQuery v" + plugin.getDescription().getVersion() + " by Septogeddon. Usage: &b/" + label + " <reload|check|debug|broadcastMessage>");
        return true;
    }

    private void send(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

}
