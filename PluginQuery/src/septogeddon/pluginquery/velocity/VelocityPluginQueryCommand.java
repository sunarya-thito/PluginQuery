package septogeddon.pluginquery.velocity;

import java.util.ArrayList;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import net.kyori.text.format.TextDecoration;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;
import septogeddon.pluginquery.utils.DataBuffer;
import septogeddon.pluginquery.utils.Debug;
import septogeddon.pluginquery.utils.EncryptionToolkit;

public class VelocityPluginQueryCommand implements Command {

	private VelocityPluginQuery plugin;
	public VelocityPluginQueryCommand(VelocityPluginQuery plugin) {
		this.plugin = plugin;
	}
	@Override
	public void execute(CommandSource sender, String[] args) {
		if (!sender.hasPermission("pluginquery.admin")) {
			sender.sendMessage(TextComponent.of("You don't have permission to do this").color(TextColor.RED));
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
					((Player)sender).getCurrentServer().ifPresent(server->{
						server.sendPluginMessage(VelocityPluginQuery.MODERN_IDENTIFIER, byteArray);
					});
				} else {
					sender.sendMessage(legacy(QueryContext.COMMAND_PREFIX+"&cYou must be a player to do this!"));
				}
				return;
			}
			if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
				plugin.reloadConfig();
				sender.sendMessage(legacy(QueryContext.COMMAND_PREFIX+"Configuration has been reloaded!"));
				return;
			}
			if (args[0].equalsIgnoreCase("check")) {
				ArrayList<String> str = new ArrayList<>();
				for (QueryConnection con : PluginQuery.getMessenger().getActiveConnections()) {
					RegisteredServer info = con.getMetadata().getData(VelocityPluginQuery.REGISTERED_SERVER);
					if (info == null) continue;
					str.add(con.isConnected() ? "&a"+info.getServerInfo().getName()+"&7" : "&c"+info.getServerInfo().getName()+"&7");
				}
				sender.sendMessage(legacy(QueryContext.COMMAND_PREFIX+"Servers (&e"+str.size()+"&7)&8: &7"+String.join(", ", str)));
				return;
			}
			if (args[0].equalsIgnoreCase("debug")) {
				Debug.STATE_DEBUG = !Debug.STATE_DEBUG;
				sender.sendMessage(legacy(QueryContext.COMMAND_PREFIX+"Debug mode has been "+(Debug.STATE_DEBUG ? "enabled" : "disabled")));
				return;
			}
		}
		sender.sendMessage(legacy(QueryContext.COMMAND_PREFIX+"PluginQuery v"+plugin.getServer().getPluginManager().getPlugin("pluginquery").get().getDescription().getVersion().get()+" by Septogeddon. Usage: &f/pq <sync|reload|check|debug>"));
	}
	
	public static TextComponent legacy(String s) {
		TextComponent.Builder component = TextComponent.builder();
		boolean color = false;
		TextColor lastColor = null;
		TextColor futureColor = null;
		ArrayList<TextDecoration> decorations = new ArrayList<>(2);
		StringBuilder built = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (c == '&') {
				color = true;
			} else {
				if (color) {
					component.append(built.toString(), futureColor, decorations.toArray(new TextDecoration[0]));
					built = new StringBuilder();
					switch(c) {
					case 'a':
						futureColor = TextColor.GREEN;
						break;
					case 'b':
						futureColor = TextColor.AQUA;
						break;
					case 'c':
						futureColor = TextColor.RED;
						break;
					case 'd':
						futureColor = TextColor.LIGHT_PURPLE;
						break;
					case 'e':
						futureColor = TextColor.YELLOW;
						break;
					case 'f':
						futureColor = TextColor.WHITE;
						break;
					case '0':
						futureColor = TextColor.BLACK;
						break;
					case '1':
						futureColor = TextColor.DARK_BLUE;
						break;
					case '2':
						futureColor = TextColor.DARK_GREEN;
						break;
					case '3':
						futureColor = TextColor.DARK_AQUA;
						break;
					case '4':
						futureColor = TextColor.DARK_RED;
						break;
					case '5':
						futureColor = TextColor.DARK_PURPLE;
						break;
					case '6':
						futureColor = TextColor.GOLD;
						break;
					case '7':
						futureColor = TextColor.GRAY;
						break;
					case '8':
						futureColor = TextColor.DARK_GRAY;
						break;
					case '9':
						futureColor = TextColor.BLUE;
						break;
					case 'k':
						decorations.add(TextDecoration.OBFUSCATED);
						break;
					case 'l':
						decorations.add(TextDecoration.BOLD);
						break;
					case 'm':
						decorations.add(TextDecoration.STRIKETHROUGH);
						break;
					case 'n':
						decorations.add(TextDecoration.UNDERLINED);
						break;
					case 'o':
						decorations.add(TextDecoration.ITALIC);
						break;
					case 'r':
						futureColor = lastColor;
						break;
					default: 
						built.append("&"+c);
						continue;
					}
					if (('a' <= c  && c <= 'f') || ('0' <= c && c <= '9')) {
						decorations.clear();
						lastColor = futureColor;
					}
					color = false;
				} else {
					built.append(c);
				}
			}
		}
		if (built.length() > 0) {
			component.append(built.toString(), futureColor, decorations.toArray(new TextDecoration[0]));
		}
		return component.build();
	}
	
}
