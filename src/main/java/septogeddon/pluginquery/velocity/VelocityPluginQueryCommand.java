package septogeddon.pluginquery.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.utils.DataBuffer;
import septogeddon.pluginquery.utils.Debug;
import septogeddon.pluginquery.utils.EncryptionToolkit;

import java.util.ArrayList;

public class VelocityPluginQueryCommand implements SimpleCommand {

    private final VelocityPluginQuery plugin;

    public VelocityPluginQueryCommand(VelocityPluginQuery plugin) {
        this.plugin = plugin;
    }

    public static TextComponent legacy(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    @Override
    public void execute(Invocation invocation) {
        execute(invocation.source(), invocation.arguments());
    }

    public void execute(CommandSource sender, String[] args) {
        if (!sender.hasPermission("pluginquery.admin")) {
            sender.sendMessage(legacy("&cYou don't have permission to do this"));
            return;
        }
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("sync") || args[0].equalsIgnoreCase("synchronize")) {
                if (sender instanceof Player) {
                    EncryptionToolkit toolkit = plugin.getEncryption();
                    DataBuffer buffer = new DataBuffer();
                    buffer.writeUTF(QueryContext.REQUEST_KEY_SHARE);
                    buffer.write(toolkit.encode());
                    byte[] byteArray = buffer.toByteArray();
                    ((Player) sender).getCurrentServer().ifPresent(server -> {
                        server.sendPluginMessage(VelocityPluginQuery.MODERN_IDENTIFIER, byteArray);
                    });
                } else {
                    sender.sendMessage(legacy(QueryContext.COMMAND_PREFIX + "&cYou must be a player to do this!"));
                }
                return;
            }
            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                plugin.reloadConfig();
                sender.sendMessage(legacy(QueryContext.COMMAND_PREFIX + "Configuration has been reloaded!"));
                return;
            }
            if (args[0].equalsIgnoreCase("check")) {
                ArrayList<String> str = new ArrayList<>();
                for (QueryConnection con : PluginQuery.getMessenger().getActiveConnections()) {
                    RegisteredServer info = con.getMetadata().getData(VelocityPluginQuery.REGISTERED_SERVER);
                    if (info == null) continue;
                    str.add(con.isConnected() ? "&a" + info.getServerInfo().getName() + "&7" : "&c" + info.getServerInfo().getName() + "&7");
                }
                sender.sendMessage(legacy(QueryContext.COMMAND_PREFIX + "Servers (&e" + str.size() + "&7)&8: &7" + String.join(", ", str)));
                return;
            }
            if (args[0].equalsIgnoreCase("debug")) {
                Debug.STATE_DEBUG = !Debug.STATE_DEBUG;
                sender.sendMessage(legacy(QueryContext.COMMAND_PREFIX + "Debug mode has been " + (Debug.STATE_DEBUG ? "enabled" : "disabled")));
                return;
            }
        }
        sender.sendMessage(legacy(QueryContext.COMMAND_PREFIX + "PluginQuery v" + plugin.getServer().getPluginManager().getPlugin("pluginquery").get().getDescription().getVersion().get() + " by Septogeddon. Usage: &f/pq <sync|reload|check|debug>"));
    }

}
