package Eventials.listeners;

import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;

public class PlayerSleepListener implements Listener{
	final double SKIP_NIGHT_PERCENT, SKIP_STORM_PERCENT, SKIP_THUNDER_PERCENT;
	final boolean INCLUDE_GM3;
	final Eventials pl;

	public PlayerSleepListener(){
		pl = Eventials.getPlugin();
		SKIP_NIGHT_PERCENT = pl.getConfig().getDouble("skip-night-sleep-percent-required", 0.5);
		SKIP_STORM_PERCENT = pl.getConfig().getDouble("skip-storm-sleep-percent-required", 0.5);
		SKIP_THUNDER_PERCENT = pl.getConfig().getDouble("skip-thunder-sleep-percent-required", 0.5);
		INCLUDE_GM3 = pl.getConfig().getBoolean("count-spectators-in-sleep-required", false);
	}

	@EventHandler
	public void onPlayerSleep(PlayerBedEnterEvent evt){
		int numInWorld =  evt.getPlayer().getWorld().getPlayers().size();
		int numSleeping = 1;
		for(Player player : evt.getPlayer().getWorld().getPlayers()){
			if(player.isSleeping() && !player.getName().equals(evt.getPlayer().getName())) ++numSleeping;
			if(!INCLUDE_GM3 && player.getGameMode() == GameMode.SPECTATOR) --numInWorld;
		}
		final UUID worldId = evt.getPlayer().getWorld().getUID();
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
}