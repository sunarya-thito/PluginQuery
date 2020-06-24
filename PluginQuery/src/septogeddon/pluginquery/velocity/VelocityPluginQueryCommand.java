package septogeddon.pluginquery.velocity;

import java.util.ArrayList;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;

import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import net.kyori.text.format.TextDecoration;

public class VelocityPluginQueryCommand implements Command {

	public static void main(String[]args) {
		TextComponent co = legacy("This &ais &ljust &b&oa &rtest");
		System.out.println(co.toString());
	}
	@Override
	public void execute(CommandSource sender, String[] args) {
		if (!sender.hasPermission("pluginquery.admin")) {
			sender.sendMessage(TextComponent.of("You don't have permission to do this").color(TextColor.RED));
			return;
		}
		sender.sendMessage(legacy("&8[&bPluginQuery&8] &7"));
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
