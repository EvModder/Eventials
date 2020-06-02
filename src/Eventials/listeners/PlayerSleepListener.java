package Eventials.listeners;

import java.util.HashSet;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.TextUtils;

public class PlayerSleepListener implements Listener{
	final double SKIP_NIGHT_PERCENT, SKIP_STORM_PERCENT, SKIP_THUNDER_PERCENT;
	final long BED_ENTER_START_TICK = 12540, BED_ENTER_END_TICK = 23460;
	final boolean INCLUDE_GM3, INCLUDE_GM1, ONLY_SKIP_IF_NIGHT, SKIP_IF_DAYLIGHT_CYCLE_IS_OFF;
	final boolean BROADCAST_VANILLA_SKIPS, BROADCAST_SKIPS_TO_ALL_WORLDS = false, PERCENT_INCLUSIVE;
	final HashSet<UUID> skipNightWorlds, skipStormWorlds, skipThunderWorlds;
	final Eventials pl;

	public PlayerSleepListener(){
		pl = Eventials.getPlugin();
		SKIP_NIGHT_PERCENT = pl.getConfig().getDouble("skip-night-sleep-percent-required", 0.5);
		SKIP_STORM_PERCENT = pl.getConfig().getDouble("skip-storm-sleep-percent-required", 0.5);
		SKIP_THUNDER_PERCENT = pl.getConfig().getDouble("skip-thunder-sleep-percent-required", 0.5);
		PERCENT_INCLUSIVE = pl.getConfig().getBoolean("skip-percent-inclusive-bound", true);
		INCLUDE_GM3 = pl.getConfig().getBoolean("count-gm3-in-sleep-required", false);
		INCLUDE_GM1 = pl.getConfig().getBoolean("count-gm1-in-sleep-required", false);
		ONLY_SKIP_IF_NIGHT = pl.getConfig().getBoolean("only-skip-if-nighttime", true);
		BROADCAST_VANILLA_SKIPS = pl.getConfig().getBoolean("skip-night-notify-if-natural", false);
		SKIP_IF_DAYLIGHT_CYCLE_IS_OFF = false;//TODO: config
		skipNightWorlds = new HashSet<UUID>();
		skipStormWorlds = new HashSet<UUID>();
		skipThunderWorlds = new HashSet<UUID>();
	}

	void attemptSkips(UUID worldId, int numSleeping, int numInWorld){
		if(numSleeping <= 0) return;
		if(numSleeping >= (int)Math.ceil(numInWorld*SKIP_NIGHT_PERCENT) + (PERCENT_INCLUSIVE && numSleeping != numInWorld ? 0.1 : 0.0)){
			if(skipNightWorlds.add(worldId)){
				String sleepPercentStr = ""+(int)(SKIP_NIGHT_PERCENT*100);
				String broadcastMsg = null;
				if(numSleeping < numInWorld){
					if(PERCENT_INCLUSIVE) broadcastMsg = ChatColor.GRAY
						+sleepPercentStr+"% or more of players in the overworld are now sleeping ("+numSleeping+"). Skipping the night...";
					else broadcastMsg = ChatColor.GRAY+"More than "
						+sleepPercentStr+"% of players in the overworld are now sleeping ("+numSleeping+"). Skipping the night...";
				}
				else if(BROADCAST_VANILLA_SKIPS) broadcastMsg =
						ChatColor.GRAY+"Everyone in the overworld is sleeping ("+numSleeping+"). Skipping the night...";
				if(broadcastMsg != null){
					if(BROADCAST_SKIPS_TO_ALL_WORLDS) pl.getServer().broadcastMessage(broadcastMsg);
					else for(Player p : pl.getServer().getWorld(worldId).getPlayers()){
						p.sendMessage(broadcastMsg);
					}
				}
				new BukkitRunnable(){@Override public void run(){
					World world = pl.getServer().getWorld(worldId);
					long relativeTime = 24000 - world.getTime();
					world.setFullTime(world.getFullTime() + relativeTime);
					skipNightWorlds.remove(worldId);
				}}.runTaskLater(Eventials.getPlugin(), 200);
			}
		}
		if(numSleeping >= (int)Math.ceil(numInWorld*SKIP_STORM_PERCENT)){
			if(skipStormWorlds.add(worldId))
			new BukkitRunnable(){@Override public void run(){
				World world = pl.getServer().getWorld(worldId);
				if(world.hasStorm()) world.setStorm(false);
				skipStormWorlds.remove(worldId);
			}}.runTaskLater(Eventials.getPlugin(), 200);
		}
		if(numSleeping >= (int)Math.ceil(numInWorld*SKIP_THUNDER_PERCENT)){
			if(skipThunderWorlds.add(worldId))
			new BukkitRunnable(){@Override public void run(){
				World world = pl.getServer().getWorld(worldId);
				if(world.isThundering()) world.setThundering(false);
				skipThunderWorlds.remove(worldId);
			}}.runTaskLater(Eventials.getPlugin(), 200);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSleep(PlayerBedEnterEvent evt){
		if(evt.isCancelled() || evt.getPlayer().getWorld().getEnvironment() != Environment.NORMAL) return;
		if(!SKIP_IF_DAYLIGHT_CYCLE_IS_OFF && !evt.getPlayer().getWorld().getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE)) return;
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
		int numToSkipNight = (int)Math.ceil(numInWorld*SKIP_NIGHT_PERCENT);
		evt.getPlayer().sendMessage(TextUtils.translateAlternateColorCodes('&',
				"&b"+numSleeping+" &7of&6 "+numToSkipNight+" &8|&7 "+numInWorld));
		attemptSkips(evt.getPlayer().getWorld().getUID(), numSleeping, numInWorld);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt){
		if(evt.getPlayer().getWorld().getEnvironment() != Environment.NORMAL) return;
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