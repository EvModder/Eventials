package _SpecificAndMisc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.TellrawUtils.ActionComponent;
import net.evmodder.EvLib.extras.TellrawUtils.ClickEvent;
import net.evmodder.EvLib.extras.TellrawUtils.HoverEvent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.extras.TellrawUtils.TellrawBlob;
import net.evmodder.EvLib.extras.TextUtils;

public class AC_Hardcore implements Listener{
	private final Eventials pl;
	final boolean fancyPl;

	public AC_Hardcore(){
		pl = Eventials.getPlugin();
		fancyPl = pl.getConfig().getBoolean("fancy-pl", true);
		pl.getServer().getPluginManager().registerEvents(this, pl);
		pl.getLogger().info("Loaded Hardcore-specific things (/pl, /engrave, /votes)");
	}

	ChatColor enableTest(String pluginName){
		Plugin plugin = Eventials.getPlugin().getServer().getPluginManager().getPlugin(pluginName);
		return (plugin != null && plugin.isEnabled()) ? ChatColor.GREEN : ChatColor.RED;
	}
	void showFancyPlugins(Player player){
		TellrawBlob blob = new TellrawBlob(
			new RawTextComponent("Plugins: "),
//			new ActionComponent(enableTest("Renewable")+"Essentials", HoverEvent.SHOW_TEXT, "Prevents unrenewable items from being destroyed"),
//			new RawTextComponent("§r, "),
			new ActionComponent(enableTest("Essentials")+"Essentials", HoverEvent.SHOW_TEXT, "Collection of useful tools and commands"),
			new RawTextComponent("§r, "),
			new ActionComponent(enableTest("HardcoreTweaks")+"HCTweaks", HoverEvent.SHOW_TEXT, "Package of custom-built features and tweaks"),
			new RawTextComponent("§r, "),
			new ActionComponent(enableTest("ChatManager")+"ChatManager", HoverEvent.SHOW_TEXT, "Keeps chat pg13 + Color/Format for chat & signs"),
			new RawTextComponent("§r,\n"),
			new ActionComponent(enableTest("DropHeads")+"DropHeads", HoverEvent.SHOW_TEXT, "Provides a chance to get heads from mobs/players"),
			new RawTextComponent("§r, "),
			new ActionComponent(enableTest("HorseOwners")+"HorseRank", HoverEvent.SHOW_TEXT, "Naming horses and viewing/ranking their stats"),
			new RawTextComponent("§r, "),
//			new ActionComponent(enableTest("EnchantBook")+"EnchantBook", HoverEvent.SHOW_TEXT, "Color item names in anvils, looting on axes, etc!"),
//			new RawTextComponent("§r, "),
			new ActionComponent("§aMore", HoverEvent.SHOW_TEXT,
//					enableTest("WorldEdit")+"WorldEdit§r, "+
//					enableTest("WorldGuard")+"WorldGuard§r, "+
//					enableTest("PluginLoader")+"PluginLoader§r, "+
					"§aEvNoCheat§r, "+
//					enableTest("PermissionsBukkit")+"PermissionsBukkit§r, "+
					enableTest("BungeeTabListPlus")+"TabList+§r, "+
					enableTest("Votifier")+"Votifier§r."),
			new RawTextComponent("\n§7§oHover over a plugin to see more details!")
		);
		Eventials.getPlugin().sendTellraw(player, blob.toString());
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

	enum Engraving{FORGE, ENGRAVE, BLESS, CURSE, SALUTE};
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
		/*if(command.contains("add_participant") && evt.getPlayer().getName().equals("EvDoc")){//TOOD: remove hacky 2019 aug event helper
			String name = message.split(" ")[1];
			@SuppressWarnings("deprecation")
			OfflinePlayer p = pl.getServer().getOfflinePlayer(name);
			try{new File("./plugins/EvFolder/aug_evt/"+p.getUniqueId()+".txt").createNewFile();}
			catch(IOException e){e.printStackTrace();}
			evt.getPlayer().sendMessage("Added: "+p.getName()+" ("+p.getUniqueId()+")");
		}*/
		if(command.equals("vote") || command.equals("votes")){
			pl.runCommand("minecraft:tellraw "+player.getName()+" ["
					+ "{\"text\":\"You currently have \",\"color\":\"gray\"},"
					+ "{\"score\":{\"name\":\""+player.getUniqueId().toString()+"\",\"objective\":\"votes-by-uuid\"},\"color\":\"dark_green\"},"
					+ "{\"text\":\" unused votes.\",\"color\":\"gray\"}]");
		}
		if(command.equals("engrave") || command.equals("embellish")){
			evt.setCancelled(true);
			ItemStack item = player.getInventory().getItemInMainHand();
			Engraving engraving = null;
			if(item != null && space > 0){
				try{engraving = Engraving.valueOf(message.substring(space+1).toUpperCase());}
				catch(IllegalArgumentException ex){engraving = null;}
			}
			if(item == null || engraving == null){
				TellrawBlob blob = new TellrawBlob(
						new RawTextComponent(ChatColor.GRAY
								+ "To embellish an item with your name using votes, hold the item\n"
								+ "you wish to modify in your main hand and select one option:\n"),
						new ActionComponent(TextUtils.translateAlternateColorCodes('&',
								"&8 • &#bbb&oEngrave &f(cost:&6 5&f)\n"), ClickEvent.RUN_COMMAND, "/embellish engrave"),
						new ActionComponent(TextUtils.translateAlternateColorCodes('&',
								"&8 • &#ec5&oForge &f(cost:&6 10&f)\n"), ClickEvent.RUN_COMMAND, "/embellish forge"),
						new ActionComponent(TextUtils.translateAlternateColorCodes('&',
								"&8 • &#adf&oBless &f(cost:&6 7&f)\n"), ClickEvent.RUN_COMMAND, "/embellish bless"),
						new ActionComponent(TextUtils.translateAlternateColorCodes('&',
								"&8 • &#e41&oCurse &f(cost:&6 7&f)\n"), ClickEvent.RUN_COMMAND, "/embellish curse"),
						new ActionComponent(TextUtils.translateAlternateColorCodes('&',
								"&8 • &#ec5&oSalute &f(cost:&6 6&f)\n"), ClickEvent.RUN_COMMAND, "/embellish salute")
				);
				pl.sendTellraw(player, blob.toString());
				pl.getLogger().info("TEMP DELETE THIS IN AC_HARDCORE:\ntellraw blob: "+blob.toString());
				return;
			}
			int votes = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("votes").getScore(player.getUniqueId().toString()).getScore();
			int cost = 10;
			String lore = null;
			switch(engraving){
				case FORGE: cost = 15; lore = "&#ec5&oForged by "; break;
				case BLESS: cost = 10; lore = "#adf&oBlessed by "; break;
				case CURSE: cost = 10; lore = "#e41&oCursed by "; break;
				case SALUTE: cost = 7; lore = "#ec5&oSaluted by "; break;
				case ENGRAVE: cost = 6; lore = "#bbb&oEngraved by "; break;
			}
			if(votes >= cost){
				ItemMeta meta = item.getItemMeta();
				List<String> lores = meta.hasLore() ? meta.getLore() : Arrays.asList();
				lores.add(lore);
				meta.setLore(lores);
				item.setItemMeta(meta);
				player.getInventory().setItemInMainHand(item);
				player.sendMessage(ChatColor.GREEN+"Loretext added!");
				pl.runCommand("scoreboard players set "+player.getUniqueId()+" votes "+(votes-cost));
			}
			else{
				player.sendMessage(ChatColor.RED+"You do not have enough votes");
			}
		}
	}
}