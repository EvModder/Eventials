package Eventials.commands;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import net.ess3.api.IEssentials;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.earth2me.essentials.User;

import EvLib.CommandBase2;
import EvLib.EvPlugin;
import Eventials.Eventials;

public class CommandPig extends CommandBase2 implements Listener{
	private EvPlugin pl;
	private Set<UUID> piggyPigs;

	public CommandPig(EvPlugin p) {
		super(p);
		pl = p;
		piggyPigs = new HashSet<UUID>();
		teleports = new PriorityQueue<UUID>();
		justAte = new LinkedList<UUID>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		Player p;
		if(args.length > 0) p = Eventials.getPlugin().getServer().getPlayer(args[0]);
		else if(sender instanceof Player) p = (Player) sender;
		else{
			sender.sendMessage(ChatColor.RED+"Too few arguments!");
			return false;
		}
		if(p == null) sender.sendMessage(ChatColor.RED+"Could not find the specified player!");
		else{
			if(remove(p.getUniqueId())){
				p.sendMessage(ChatColor.GOLD+"PiggyPig disabled");
			}
			else{
				add(p.getUniqueId());
				if(p.getFoodLevel() == 20) p.setFoodLevel(19);
				p.sendMessage(ChatColor.GOLD+"PiggyPig enabled");
			}
		}
		return true;
	}

	List<UUID> justAte;
	@EventHandler
	public void onPlayerEatFoodEvent(PlayerItemConsumeEvent evt){
		if(piggyPigs.contains(evt.getPlayer().getUniqueId())){
			justAte.add(evt.getPlayer().getUniqueId());

			new BukkitRunnable(){@Override public void run(){
				Player p = pl.getServer().getPlayer(justAte.remove(0));
				if(p != null && p.getFoodLevel() == 20) p.setFoodLevel(19);
			}}.runTaskLater(pl, 1);
		}
	}

	@EventHandler
	public void onPlayerInteractBlock(PlayerInteractEvent evt){
		if(evt.getClickedBlock() != null && evt.getClickedBlock().getType() == Material.CAKE_BLOCK){
			if(piggyPigs.contains(evt.getPlayer().getUniqueId())){
				if(evt.getPlayer().getFoodLevel() >= 18) evt.getPlayer().setFoodLevel(18);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt){
		remove(evt.getPlayer().getUniqueId());
	}

	Queue<UUID> teleports;
	@EventHandler(priority = EventPriority.MONITOR)
	public void onTp(PlayerTeleportEvent evt){
		if(!evt.isCancelled() && piggyPigs.contains(evt.getPlayer().getUniqueId()) &&
				evt.getFrom().getWorld().getName().equals(evt.getTo().getWorld().getName()))
		{
			teleports.add(evt.getPlayer().getUniqueId());
			new BukkitRunnable(){@Override public void run() {
				Player p = pl.getServer().getPlayer(teleports.remove());

				if(p != null && new User(p, (IEssentials) pl.getServer().getPluginManager().getPlugin("Essentials"))
						.isAuthorized("eventials.pig") == false) remove(p.getUniqueId());
			}}.runTaskLater(pl, 1);
		}
	}

	public boolean remove(UUID player){
		boolean contained = piggyPigs.remove(player);
		if(contained && piggyPigs.isEmpty()) HandlerList.unregisterAll(this);
		return contained;
	}
	public void add(UUID player){
		if(piggyPigs.isEmpty()) pl.getServer().getPluginManager().registerEvents(this, pl);
		piggyPigs.add(player);
	}
}