package Eventials.commands;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import net.ess3.api.IEssentials;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import com.earth2me.essentials.User;
import EvLib.CommandBase2;
import EvLib.EvPlugin;
import EvLib.UsefulUtils;
import Eventials.Eventials;

public class CommandBreakPhysics extends CommandBase2 implements Listener{
	private EvPlugin pl;
	private Set<UUID> breakPhysics;

	public CommandBreakPhysics(EvPlugin p) {
		super(p);
		pl = p;
		breakPhysics = new HashSet<UUID>();
		teleports = new PriorityQueue<UUID>();
	}

	@SuppressWarnings("deprecation") @Override
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
				p.sendMessage(ChatColor.YELLOW+"Toggled break-physics: off");
			}
			else{
				add(p.getUniqueId());
				p.sendMessage(ChatColor.YELLOW+"Toggled break-physics: on");
			}
		}
		return true;
	}

	@EventHandler
	public void onBlockPhysicsEvent(BlockPhysicsEvent evt){
		if(!evt.isCancelled())
		for(Player p : pl.getServer().getOnlinePlayers()){
			if(breakPhysics.contains(p.getUniqueId()) && UsefulUtils.notFar(p.getLocation(), evt.getBlock().getLocation())){
				evt.setCancelled(true);
				return;
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
		if(!evt.isCancelled() && breakPhysics.contains(evt.getPlayer().getUniqueId()) &&
				evt.getFrom().getWorld().getName().equals(evt.getTo().getWorld().getName()))
		{
			teleports.add(evt.getPlayer().getUniqueId());
			new BukkitRunnable(){@Override public void run() {
				Player p = pl.getServer().getPlayer(teleports.remove());

				if(p != null && new User(p, (IEssentials) pl.getServer().getPluginManager().getPlugin("Essentials"))
						.isAuthorized("evp.evm.breakPhysics") == false) remove(p.getUniqueId());
			}}.runTaskLater(pl, 1);
		}
	}

	public boolean remove(UUID player){
		boolean contained = breakPhysics.remove(player);
		if(contained && breakPhysics.isEmpty()) HandlerList.unregisterAll(this);
		return contained;
	}
	public void add(UUID player){
		if(breakPhysics.isEmpty()) pl.getServer().getPluginManager().registerEvents(this, pl);
		breakPhysics.add(player);
	}
}