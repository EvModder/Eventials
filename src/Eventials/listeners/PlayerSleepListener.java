package Eventials.listeners;

import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;
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
import net.evmodder.EvLib.bukkit.TellrawUtils;
import net.evmodder.EvLib.bukkit.TellrawUtils.HoverEvent;
import net.evmodder.EvLib.bukkit.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.bukkit.TellrawUtils.TextHoverAction;
import net.evmodder.EvLib.bukkit.TellrawUtils.ListComponent;
import net.evmodder.EvLib.util.Pair;

public class PlayerSleepListener implements Listener{
	final double SKIP_NIGHT_PERCENT, SKIP_STORM_PERCENT, SKIP_THUNDER_PERCENT;
	final long BED_ENTER_START_TICK = 12540, BED_ENTER_END_TICK = 23460;
	final boolean INCLUDE_GM3, INCLUDE_GM1, ONLY_SKIP_IF_NIGHT, SKIP_IF_DAYLIGHT_CYCLE_IS_OFF;
	final boolean BROADCAST_VANILLA_SKIPS, BROADCAST_SKIPS_TO_ALL_WORLDS = false, PERCENT_INCLUSIVE;
	final String SKIP_NIGHT_PERCENT_STR;
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
		skipNightWorlds = new HashSet<>();
		skipStormWorlds = new HashSet<>();
		skipThunderWorlds = new HashSet<>();
		SKIP_NIGHT_PERCENT_STR = (int)(SKIP_NIGHT_PERCENT*100)+"%";
	}

	String getSkipNightTellrawMsg(int numSleeping, int numToCount, World world){
		int numInWorld = world.getPlayers().size();
		ListComponent sleepingPlayers = TellrawUtils.convertHexColorsToComponentsWithReset(world.getPlayers().stream().filter(p -> p.isSleeping())
				.map(p -> p.getDisplayName()).collect(Collectors.joining("§7, §r")));
		pl.getLogger().info("Sleeping players: "+sleepingPlayers);
		RawTextComponent sleepingPlayersComp = new RawTextComponent("§7"+numSleeping, new TextHoverAction(HoverEvent.SHOW_TEXT, sleepingPlayers));
		ListComponent blob = new ListComponent();
		if(numSleeping < numToCount){
			if(PERCENT_INCLUSIVE){
				blob.addComponent(ChatColor.GRAY+SKIP_NIGHT_PERCENT_STR+" or more of players in the overworld are sleeping (");
				blob.addComponent(sleepingPlayersComp);
				blob.addComponent("§7/"+numInWorld+"). Skipping the night...");
			}
			else{
				blob.addComponent("§7More than "+SKIP_NIGHT_PERCENT_STR+" of players in the overworld are sleeping (");
				blob.addComponent(sleepingPlayersComp);
				blob.addComponent("§7). Skipping the night...");
			}
		}
		else if(numToCount < numInWorld){ // Technically this is a vanilla-skip as well...
			blob.addComponent(new RawTextComponent("§7Everyone*", new TextHoverAction(HoverEvent.SHOW_TEXT, "§7*in gamemode survival")));
			blob.addComponent("§7 in the overworld is sleeping (");
			blob.addComponent(sleepingPlayersComp);
			blob.addComponent("§7). Skipping the night...");
		}
		else if(BROADCAST_VANILLA_SKIPS){
			blob.addComponent("§7Everyone in the overworld is sleeping (");
			blob.addComponent(sleepingPlayersComp);
			blob.addComponent("§7). Skipping the night...");
		}
		return blob.toString();
	}
	
	Pair<Integer, Integer> getNumSleepingAndCounted(World world, UUID triggerPlayer, boolean includeTrigger){
		int numSleeping = includeTrigger ? 1 : 0;
		int numInWorld = world.getPlayers().size();
		for(Player player : world.getPlayers()){
			if(player.getUniqueId().equals(triggerPlayer)){if(!includeTrigger) --numInWorld; continue;}
			if(player.isSleeping()) ++numSleeping;
			if(!INCLUDE_GM3 && player.getGameMode() == GameMode.SPECTATOR) --numInWorld;
			else if(!INCLUDE_GM1 && player.getGameMode() == GameMode.CREATIVE) --numInWorld;
		}
		return new Pair<>(numSleeping, numInWorld);
	}

	void attemptSkips(World world, UUID triggerPlayer, boolean includeTrigger){
		if(world.getEnvironment() != Environment.NORMAL) return;
		if(ONLY_SKIP_IF_NIGHT){
			if(world.getTime() < BED_ENTER_START_TICK || BED_ENTER_END_TICK < world.getTime()) return;
		}
		Pair<Integer, Integer> sleepingAndCounted = getNumSleepingAndCounted(world, triggerPlayer, includeTrigger);
		int CUR_SLEEPERS = sleepingAndCounted.a, MAX_SLEEPERS = sleepingAndCounted.b;
		if(sleepingAndCounted.a <= 0) return;
		double needToSkipNight = MAX_SLEEPERS*SKIP_NIGHT_PERCENT;
		double needToSkipStorm = MAX_SLEEPERS*SKIP_STORM_PERCENT;
		double needToSkipThunder = MAX_SLEEPERS*SKIP_THUNDER_PERCENT;
		int numToSkipNight = (int)(Math.ceil(needToSkipNight) > needToSkipNight ? Math.ceil(needToSkipNight)
				: needToSkipNight + (!PERCENT_INCLUSIVE && needToSkipNight < MAX_SLEEPERS ? 1 : 0));
		int numToSkipStorm = (int)(Math.ceil(needToSkipStorm) > needToSkipStorm ? Math.ceil(needToSkipStorm)
				: needToSkipStorm + (!PERCENT_INCLUSIVE && needToSkipStorm < MAX_SLEEPERS ? 1 : 0));
		int numToSkipThunder = (int)(Math.ceil(needToSkipThunder) > needToSkipThunder ? Math.ceil(needToSkipThunder)
				: needToSkipThunder + (!PERCENT_INCLUSIVE && needToSkipThunder < MAX_SLEEPERS ? 1 : 0));
		//pl.getLogger().info("cur_sleepers: "+CUR_SLEEPERS+", max_sleepers: "+MAX_SLEEPERS+", num_to_skip_night: "+numToSkipNight);
		if(SKIP_IF_DAYLIGHT_CYCLE_IS_OFF || world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE)){
			if(includeTrigger){
				Player player = pl.getServer().getPlayer(triggerPlayer);
				if(player != null){
					RawTextComponent curSleepersComp = new RawTextComponent("§b"+CUR_SLEEPERS,
						new TextHoverAction(HoverEvent.SHOW_TEXT,
							TellrawUtils.convertHexColorsToComponentsWithReset(
								"§7Currently in bed: " +
								world.getPlayers().stream().filter(p -> includeTrigger
										? (p.isSleeping() || p.getUniqueId().equals(triggerPlayer))
										: (p.isSleeping() && !p.getUniqueId().equals(triggerPlayer))
								)
								.map(p -> p.getDisplayName()).collect(Collectors.joining("§7, §r"))
							)
						)
					);
					RawTextComponent numToSkipComp = new RawTextComponent("§6"+numToSkipNight,
						new TextHoverAction(HoverEvent.SHOW_TEXT, "§7Number required to skip the night")
					);
					RawTextComponent maxSleepersComp = new RawTextComponent("§7"+MAX_SLEEPERS,
						new TextHoverAction(HoverEvent.SHOW_TEXT, "§7Number of considered players in the overworld")
					);
					pl.sendTellraw(player.getName(), new ListComponent(
							curSleepersComp, new RawTextComponent("§7 of "), numToSkipComp, new RawTextComponent("§8 | "), maxSleepersComp
					).toString());
				}
			}
			if(CUR_SLEEPERS >= numToSkipNight){
				if(skipNightWorlds.add(world.getUID())) {
					new BukkitRunnable(){@Override public void run(){
						Pair<Integer, Integer> sleepingAndCounted = getNumSleepingAndCounted(world, triggerPlayer, includeTrigger);
						final int CUR_SLEEPERS = sleepingAndCounted.a, MAX_SLEEPERS = sleepingAndCounted.b;
						final double needToSkipNight = MAX_SLEEPERS*SKIP_NIGHT_PERCENT;
						final int numToSkipNight = (int)(Math.ceil(needToSkipNight) > needToSkipNight ? Math.ceil(needToSkipNight)
								: needToSkipNight + (!PERCENT_INCLUSIVE && needToSkipNight < MAX_SLEEPERS ? 1 : 0));
						if(CUR_SLEEPERS >= Math.max(1, numToSkipNight)){
							String broadcastMsg = getSkipNightTellrawMsg(CUR_SLEEPERS, MAX_SLEEPERS, world);
							if(broadcastMsg != null)
								for(Player p : BROADCAST_SKIPS_TO_ALL_WORLDS
										? pl.getServer().getOnlinePlayers() : world.getPlayers()) pl.sendTellraw(p.getName(), broadcastMsg);
							long relativeTime = 24000 - world.getTime();
							world.setFullTime(world.getFullTime() + relativeTime);
						}
						skipNightWorlds.remove(world.getUID());
					}}.runTaskLater(Eventials.getPlugin(), 100);
				}
			}
		}
		if(sleepingAndCounted.a >= numToSkipStorm){
			if(skipStormWorlds.add(world.getUID()))
			new BukkitRunnable(){@Override public void run(){
				if(world.hasStorm()) world.setStorm(false);
				skipStormWorlds.remove(world.getUID());
			}}.runTaskLater(Eventials.getPlugin(), 200);
		}
		if(sleepingAndCounted.a >= numToSkipThunder){
			if(skipThunderWorlds.add(world.getUID()))
			new BukkitRunnable(){@Override public void run(){
				if(world.isThundering()) world.setThundering(false);
				skipThunderWorlds.remove(world.getUID());
			}}.runTaskLater(Eventials.getPlugin(), 200);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSleep(PlayerBedEnterEvent evt){
		if(evt.isCancelled()) return;
		attemptSkips(evt.getPlayer().getWorld(), evt.getPlayer().getUniqueId(), /*includePlayer=*/true);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt){
		attemptSkips(evt.getPlayer().getWorld(), evt.getPlayer().getUniqueId(), /*includePlayer=*/false);
	}
}