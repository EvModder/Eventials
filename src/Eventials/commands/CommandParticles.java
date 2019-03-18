package Eventials.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import EvLib.EvPlugin;
import EvLib.CommandBase2;
import Eventials.Eventials;
import ParticleEffects.CustomParticleEffect;
import net.ess3.api.IEssentials;
import com.earth2me.essentials.User;

public class CommandParticles extends CommandBase2 {
	Map<UUID, List<CustomParticleEffect>> particlePpl = new HashMap<UUID, List<CustomParticleEffect>>();
	private Listener teleportListener;
	Eventials pl;

	public CommandParticles(EvPlugin p){
		super(p);
		pl = Eventials.getPlugin();
	}

	@SuppressWarnings("deprecation") @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		Player p = sender instanceof Player ? (Player)sender : null;
		if(args.length != 0 && sender.hasPermission("eventials.particles.others")){
			Player target = Eventials.getPlugin().getServer().getPlayer(args[0]);
			if(target != null){
				args = Arrays.copyOfRange(args, 1, args.length);
				p = target;
			}
		}
		else if(p == null){
			sender.sendMessage(ChatColor.RED+"Too few arguments!");
			return true;
		}
		if(p == null){
			sender.sendMessage(ChatColor.RED+"Could not find the specified player!");
			return true;
		}

		if(args.length == 0){
			particlePpl.remove(p.getUniqueId());
			sender.sendMessage(ChatColor.GOLD+"Removed all particle effects");
		}
		else{
			CustomParticleEffect effect;
			try{effect = CustomParticleEffect.valueOf(args[0].toUpperCase());}
			catch(IllegalArgumentException ex){
				p.sendMessage(ChatColor.RED+"Unknown particle effect");
				return true;
			}

			List<CustomParticleEffect> effects = particlePpl.get(p.getUniqueId());
			if(effects == null){
				effects = new ArrayList<CustomParticleEffect>();
				particlePpl.put(p.getUniqueId(), effects);
			}

			if(effects.contains(effect)){
				if(effects.remove(effect) && effects.isEmpty()){
					particlePpl.remove(p.getUniqueId());
				}
				sender.sendMessage(ChatColor.GOLD+"Removed the specified effect");
			}
			else{
				//If this is the first person to start particling
				if(effects.isEmpty() && particlePpl.size() == 1){
					new EffectDisplayer().runTaskTimer(pl, 1, 1);
					pl.getServer().getPluginManager().registerEvents(teleportListener = new Listener(){
						LinkedList<UUID> teleports = new LinkedList<UUID>();
						IEssentials essentials = (IEssentials)pl.getServer().getPluginManager().getPlugin("Essentials");
						
						@EventHandler(priority = EventPriority.MONITOR)
						public void onTp(PlayerTeleportEvent evt){
							if(!evt.isCancelled() && particlePpl.containsKey(evt.getPlayer().getUniqueId()) &&
									!evt.getFrom().getWorld().getName().equals(evt.getTo().getWorld().getName()))
							{
								teleports.addFirst(evt.getPlayer().getUniqueId());
								new BukkitRunnable(){
									@Override public void run() {
										Player p = pl.getServer().getPlayer(teleports.pollLast());
										if(p != null && !new User(p, essentials).isAuthorized("evp.evm.particles"))
											particlePpl.remove(p.getUniqueId());
									}
								}.runTaskLater(pl, 20);
							}
						}
					}, pl);
				}
				effects.add(effect);
				sender.sendMessage(ChatColor.GREEN+"Added the specified effect");
			}
		}
		return true;
	}

	private class EffectDisplayer extends BukkitRunnable{
		private long time;
		@Override public void run(){
			if(particlePpl.isEmpty()){
				cancel();
				HandlerList.unregisterAll(teleportListener);
			}
			else for(UUID uuid : particlePpl.keySet()){
				Player p = pl.getServer().getPlayer(uuid);
				if(p == null) particlePpl.remove(uuid);
				else for(CustomParticleEffect effect : particlePpl.get(uuid)) effect.display(p, time);
			}
			if(++time == Long.MAX_VALUE) time = 0;
		}
	}
}