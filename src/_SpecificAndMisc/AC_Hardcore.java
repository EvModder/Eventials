package _SpecificAndMisc;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
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
			enableTest("ChatManager")+"ChatTweaks=>Keeps chat pg13 + Color/Format for chat & signs§r, \\\\n§a\\" +
			enableTest("DropHeads")+"DropHeads=>Provides a chance to get heads from mobs/players§r, §a\\" +
			enableTest("HorseOwners")+"HorseRanks=>Claim, name, and view stats for horses§r, §a\\" +
//			enableTest("EnchantBook")+"EnchantBook=>Color item names in anvils, looting on axes, etc!§r, §a\\" +
			"More=>\\"+
//			enableTest("WorldEdit")+"WorldEdit\\§f, \\" +
//			enableTest("WorldGuard")+"WorldGuard\\§f, \\" +
//			enableTest("PluginLoader")+"PluginLoader\\§f, \\" +
			ChatColor.GREEN+"EvNoCheat\\§f, \\" +
//			enableTest("PermissionsBukkit")+"PermissionsBukkit\\§f, \\" +
			enableTest("BungeeTabListPlus")+"TabList+\\§f, \\" +
			enableTest("Votifier")+"Votifier§r" +
			".\\\\n\\§7\\§oHover over a plugin to see more details!",
			"§r"
		);
		Eventials.getPlugin().runCommand("tellraw "+player.getName()+' '+raw);
	}

	@EventHandler
	public void onPreLogin(PlayerLoginEvent evt){
		final UUID uuid = evt.getPlayer().getUniqueId();
		OfflinePlayer offP = pl.getServer().getOfflinePlayer(uuid);
		if(offP.hasPlayedBefore() && offP.getLastPlayed() <= 1566802800000L){
			new BukkitRunnable(){@Override public void run(){
				Player player = pl.getServer().getPlayer(uuid);
				player.addScoreboardTag("event_participant");
			}}.runTaskLater(pl, 20);
			try{new File("./plugins/EvFolder/aug_evt/"+uuid+".txt").createNewFile();}
			catch(IOException e){e.printStackTrace();}
		}
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
		if(command.contains("add_participant") && evt.getPlayer().getName().equals("EvDoc")){
			String name = message.split(" ")[1];
			@SuppressWarnings("deprecation")
			OfflinePlayer p = pl.getServer().getOfflinePlayer(name);
			try{new File("./plugins/EvFolder/aug_evt/"+p.getUniqueId()+".txt").createNewFile();}
			catch(IOException e){e.printStackTrace();}
			evt.getPlayer().sendMessage("Added: "+p.getName()+" ("+p.getUniqueId()+")");
		}
	}
}