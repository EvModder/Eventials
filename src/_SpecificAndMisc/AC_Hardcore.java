package _SpecificAndMisc;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.TellrawUtils.ActionComponent;
import net.evmodder.EvLib.extras.TellrawUtils.HoverEvent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.extras.TellrawUtils.TellrawBlob;

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
		TellrawBlob blob = new TellrawBlob(
			new RawTextComponent("Plugins: "),
//			new ActionComponent(enableTest("Renewable")+"Essentials", HoverEvent.SHOW_TEXT, "Prevents unrenewable items from being destroyed"),
//			new RawTextComponent("§r, "),
			new ActionComponent(enableTest("Essentials")+"Essentials", HoverEvent.SHOW_TEXT, "Collection of useful tools and commands"),
			new RawTextComponent("§r, "),
			new ActionComponent(enableTest("Eventials")+"Eventials", HoverEvent.SHOW_TEXT, "Package of custom-built features and tweaks"),
			new RawTextComponent("§r, "),
			new ActionComponent(enableTest("ChatManager")+"ChatTweaks", HoverEvent.SHOW_TEXT, "Keeps chat pg13 + Color/Format for chat & signs"),
			new RawTextComponent("§r,\n"),
			new ActionComponent(enableTest("DropHeads")+"DropHeads", HoverEvent.SHOW_TEXT, "Provides a chance to get heads from mobs/players"),
			new RawTextComponent("§r, "),
			new ActionComponent(enableTest("HorseOwners")+"HorseRanks", HoverEvent.SHOW_TEXT, "Claim, name, and view stats for horses"),
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

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent evt){
		if(evt.getEntityType() == EntityType.ZOMBIFIED_PIGLIN && ((Zombie)evt.getEntity()).isBaby()){
			evt.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBowShootEvent(EntityShootBowEvent evt){
		if(evt.getBow() != null && evt.getBow().hasItemMeta() &&
				evt.getBow().getItemMeta().hasCustomModelData() && evt.getBow().getItemMeta().getCustomModelData() == 2020){
			Vector lookingVector = evt.getEntity().getEyeLocation().toVector().normalize()
					.multiply(evt.getProjectile().getVelocity().length());
			evt.getProjectile().setVelocity(lookingVector);
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