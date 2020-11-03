package septogeddon.pluginquery.utils;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.channel.QueryDecryptor;
import septogeddon.pluginquery.channel.QueryDeflater;
import septogeddon.pluginquery.channel.QueryEncryptor;
import septogeddon.pluginquery.channel.QueryInflater;
import septogeddon.pluginquery.library.remote.ClassRegistry;
import septogeddon.pluginquery.library.remote.RemoteObject;
import septogeddon.pluginquery.library.remote.Substitute;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Supplier;

//QueryConnection connection = messenger.newConnection(new InetSocketAddress("131.153.48.90", 25619));
public class Debug {

    public static boolean STATE_DEBUG = false;

    public static void debug(Supplier<String> msg) {
        if (STATE_DEBUG) System.out.println("[PluginQueryDebug] " + msg.get());
    }

}
