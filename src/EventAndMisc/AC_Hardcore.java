package EventAndMisc;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.TextUtils;

public class AC_Hardcore implements Listener{
	private final Eventials pl;
	final boolean fancyPl;

	public AC_Hardcore(){
		pl = Eventials.getPlugin();
		fancyPl = pl.getConfig().getBoolean("fancy-pl", true);
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}

	ChatColor enableTest(String pluginName){
		Plugin plugin = Eventials.getPlugin().getServer().getPluginManager().getPlugin(pluginName);
		return (plugin != null && plugin.isEnabled()) ? ChatColor.GREEN : ChatColor.RED;
	}
	void showFancyPlugins(Player player){
		String raw = TextUtils.TextAction.parseToRaw(
			"Plugins: §a\\" +
//			enableTest("Renewable")+"Renewable=>Prevents unrenewable items from being destroyed§r, §a\\" +
			enableTest("Essentials")+"Essentials=>Collection of useful tools and commands§r, §a\\" +
			enableTest("Eventials")+"Eventials=>Package of custom-built features and tweaks§r, §a\\" +
			enableTest("ChatManager")+"Chat++=>Keeps chat pg13 + Color/Format for chat & signs§r, \\\\n§a\\" +
			enableTest("DropHeads")+"DropHeads=>Provides a chance to get heads from mobs/players§r, §a\\" +
			enableTest("HorseOwners")+"HorseOwners=>Claim, name, and view stats for horses§r, §a\\" +
//			enableTest("EnchantBook")+"EnchantBook=>Color item names in anvils, looting on axes, etc!§r, §a\\" +
			"More=>\\"+
//			enableTest("WorldEdit")+"WorldEdit\\§f, \\" +
//			enableTest("WorldGuard")+"WorldGuard\\§f, \\" +
//			enableTest("PluginLoader")+"PluginLoader\\§f, \\" +
			ChatColor.GREEN+"EvNoCheat\\§f, \\" +
//			enableTest("PermissionsBukkit")+"PermissionsBukkit\\§f, \\" +
			enableTest("BungeeTabListPlus")+"BungeeTabList\\§f, \\" +
			enableTest("Votifier")+"Votifier§r" +
			".\\\\n\\§7\\§oHover over a plugin to see more details!",
			"§r"
		);
		Eventials.getPlugin().runCommand("tellraw "+player.getName()+' '+raw);
	}

	@EventHandler
	public void onPreCommand(PlayerCommandPreprocessEvent evt){
		if(evt.getMessage().charAt(0) != '/') return;
		String message = evt.getMessage().trim();
		String command = message.toLowerCase();
		int space = command.indexOf(' ');
		command = (space > 0 ? command.substring(1, space) : command.substring(1));
		Player player = evt.getPlayer();

		if(command.equals("pl") || command.equals("plugins")){
			if(fancyPl && player.hasPermission("bukkit.command.plugins")){
				evt.setCancelled(true);
				showFancyPlugins(player);
			}
		}
	}
}