package EventAndMisc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;

public class SpectatorListener implements Listener{
	final Eventials pl;
	final HashSet<UUID> spectators;

	public SpectatorListener(){
		pl = Eventials.getPlugin();
		spectators = new HashSet<UUID>();
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}

	Location getClosest(Location loc, HashSet<Location> points){
		Location closest = null;
		double cDist = Double.MAX_VALUE;
		for(Location point : points){
			double pDist = loc.distanceSquared(point);
			if(pDist < cDist){
				closest = point;
				cDist = pDist;
			}
		}
		return closest != null ? closest : (points.isEmpty() ? null : points.iterator().next());
	}

	boolean loopActive = false;
	void runSpecatorLoop(){
		if(loopActive) return;
		loopActive = true;
		new BukkitRunnable(){@Override public void run(){
			if(spectators.size() >= pl.getServer().getOnlinePlayers().size()){
				for(UUID uuid : spectators){
					OfflinePlayer p = pl.getServer().getPlayer(uuid);
					if(p != null && p.isOnline()) p.getPlayer().kickPlayer(
							ChatColor.RED+"There is nobody online to spectate right now");
				}
				spectators.clear();
			}
			else{
				Iterator<UUID> it = spectators.iterator();
				while(it.hasNext()){
					UUID uuid = it.next();
					OfflinePlayer p = pl.getServer().getPlayer(uuid);
					if(p == null || !p.isOnline() ||
							p.getPlayer().getGameMode() != GameMode.SPECTATOR) it.remove();
				}
			}
			if(spectators.isEmpty()){
				pl.getLogger().info("No spectators remaining, setting loopAtive=false");
				//HandlerList.unregisterAll(SpectatorListener.this);
				cancel();
				loopActive = false;
			}
			else{
				HashSet<Location> nonSpecLocs = new HashSet<Location>();
				for(Player p : pl.getServer().getOnlinePlayers()){
					if(!spectators.contains(p.getUniqueId())) nonSpecLocs.add(p.getLocation());
				}
				for(UUID uuid : spectators){
					Player specP = pl.getServer().getPlayer(uuid).getPlayer();
					Location aliveP = getClosest(specP.getLocation(), nonSpecLocs);
					if(specP.getLocation().distanceSquared(aliveP) > 25*25){
						specP.teleport(aliveP);
					}
				}
			}
		}}.runTaskTimer(pl, 20*3, 20*3);
	}

	public void addSpectator(Player player){
		if(spectators.add(player.getUniqueId()))
			pl.getLogger().info("Added spectator: "+player.getName());
		player.setFlySpeed(0.1f);
		runSpecatorLoop();
	}
	public void removeSpectator(UUID uuid){
		if(spectators.remove(uuid))
			pl.getLogger().info("Removed spectator: "+uuid);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent evt){
		removeSpectator(evt.getPlayer().getUniqueId());
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent evt){
		if(evt.getPlayer().getGameMode() == GameMode.SPECTATOR
				&& !evt.getPlayer().isOp())
			addSpectator(evt.getPlayer());
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent evt){
		if(evt.getPlayer().getGameMode() == GameMode.SPECTATOR)
			addSpectator(evt.getPlayer());
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent evt){
		addSpectator(evt.getEntity());
	}
}