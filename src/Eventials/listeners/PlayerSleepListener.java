package Eventials.listeners;

import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;

public class PlayerSleepListener implements Listener{
	final double SKIP_NIGHT_PERCENT, SKIP_STORM_PERCENT, SKIP_THUNDER_PERCENT;
	final long BED_ENTER_START_TICK = 12540, BED_ENTER_END_TICK = 23460;
	final boolean INCLUDE_GM3, INCLUDE_GM1, ONLY_SKIP_IF_NIGHT;
	final Eventials pl;

	public PlayerSleepListener(){
		pl = Eventials.getPlugin();
		SKIP_NIGHT_PERCENT = pl.getConfig().getDouble("skip-night-sleep-percent-required", 0.5);
		SKIP_STORM_PERCENT = pl.getConfig().getDouble("skip-storm-sleep-percent-required", 0.5);
		SKIP_THUNDER_PERCENT = pl.getConfig().getDouble("skip-thunder-sleep-percent-required", 0.5);
		INCLUDE_GM3 = pl.getConfig().getBoolean("count-gm3-in-sleep-required", false);
		INCLUDE_GM1 = pl.getConfig().getBoolean("count-gm1-in-sleep-required", false);
		ONLY_SKIP_IF_NIGHT = pl.getConfig().getBoolean("only-skip-if-nighttime", true);
	}

	void attemptSkips(UUID worldId, int numSleeping, int numInWorld){
		if(numSleeping >= (int)Math.ceil(numInWorld*SKIP_NIGHT_PERCENT)){
			new BukkitRunnable(){@Override public void run(){
				World world = pl.getServer().getWorld(worldId);
				long Relative_Time = 24000 - world.getTime();
				world.setFullTime(world.getFullTime() + Relative_Time);
			}}.runTaskLater(Eventials.getPlugin(), 200);
		}
		if(numSleeping >= (int)Math.ceil(numInWorld*SKIP_STORM_PERCENT)){
			new BukkitRunnable(){@Override public void run(){
				World world = pl.getServer().getWorld(worldId);
				if(world.hasStorm()) world.setStorm(false);
			}}.runTaskLater(Eventials.getPlugin(), 200);
		}
		if(numSleeping >= (int)Math.ceil(numInWorld*SKIP_THUNDER_PERCENT)){
			new BukkitRunnable(){@Override public void run(){
				World world = pl.getServer().getWorld(worldId);
				if(world.isThundering()) world.setThundering(false);
			}}.runTaskLater(Eventials.getPlugin(), 200);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSleep(PlayerBedEnterEvent evt){
		if(evt.isCancelled()) return;
		if(ONLY_SKIP_IF_NIGHT){
			long time = evt.getPlayer().getWorld().getTime();
			if(time < BED_ENTER_START_TICK || time > BED_ENTER_END_TICK) return;
		}
		int numInWorld = evt.getPlayer().getWorld().getPlayers().size();
		int numSleeping = 1;
		for(Player player : evt.getPlayer().getWorld().getPlayers()){
			if(player.getName().equals(evt.getPlayer().getName())) continue;
			if(player.isSleeping()) ++numSleeping;
			if(!INCLUDE_GM3 && player.getGameMode() == GameMode.SPECTATOR) --numInWorld;
			else if(!INCLUDE_GM1 && player.getGameMode() == GameMode.CREATIVE) --numInWorld;
		}
		attemptSkips(evt.getPlayer().getWorld().getUID(), numSleeping, numInWorld);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt){
		if(ONLY_SKIP_IF_NIGHT){
			long time = evt.getPlayer().getWorld().getTime();
			if(time < BED_ENTER_START_TICK || time > BED_ENTER_END_TICK) return;
		}
		int numInWorld = 0;
		int numSleeping = 0;
		for(Player player : evt.getPlayer().getWorld().getPlayers()){
			if(player.getName().equals(evt.getPlayer().getName())) continue;
			if(player.isSleeping()) ++numSleeping;
			if((INCLUDE_GM3 || player.getGameMode() != GameMode.SPECTATOR) && 
				(INCLUDE_GM1 || player.getGameMode() != GameMode.CREATIVE)) ++numInWorld;
		}
		attemptSkips(evt.getPlayer().getWorld().getUID(), numSleeping, numInWorld);
	}
}