package Eventials;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import com.google.common.collect.ImmutableSet;
import net.evmodder.HorseOwners.HorseUtils;
import net.evmodder.HorseOwners.api.events.HorseClaimEvent;
import net.evmodder.HorseOwners.api.events.HorseDeathEvent;
import net.evmodder.HorseOwners.api.events.HorseRenameEvent;

class HorseScoardboardTracker implements Listener{
	private final Plugin pl;
	private final Set<String> horseObjectives = ImmutableSet.of(
			"horse-speed", "horse-jump", "horse-health",
			"donkey-speed", "donkey-jump", "donkey-health",
			"mule-speed", "mule-jump", "mule-health",
			"llama-speed", "llama-jump", "llama-health",
			"trader_llama-health", "skeleton_horse-jump"
	);
	
	HorseScoardboardTracker(Plugin plugin){
		pl = plugin;

		try{
			final Scoreboard board = pl.getServer().getScoreboardManager().getMainScoreboard();
			//board.registerNewObjective("buildscore", "dummy", "§[■] Blocks Placed [■]");
			//board.registerNewObjective("advancements", "dummy ", "");
			//board.registerNewObjective("deaths", "deathCount", "");
			//board.registerNewObjective("murderscore", "playerKillCount ", "");
			//board.registerNewObjective("levels", "level", "§e- §bLevels §e-");
//			//board.registerNewObjective("health", "health", "Health");
			board.registerNewObjective("horse-speed", "dummy", "§9§m  §a Horse Speed §9§m  ");
			board.registerNewObjective("horse-health", "dummy", "§9§m  §a Horse Health §9§m  ");
			board.registerNewObjective("horse-jump", "dummy", "§9§m  §a Horse Jump §9§m  ");
			board.registerNewObjective("donkey-speed", "dummy", "§9§m  §a Donkey Speed §9§m  ");
			board.registerNewObjective("donkey-health", "dummy", "§9§m  §a Donkey Health §9§m  ");
			board.registerNewObjective("donkey-jump", "dummy", "§9§m  §a Donkey Jump §9§m  ");
			board.registerNewObjective("mule-speed", "dummy", "§9§m  §a Mule Speed §9§m  ");
			board.registerNewObjective("mule-health", "dummy", "§9§m  §a Mule Health §9§m  ");
			board.registerNewObjective("mule-jump", "dummy", "§9§m  §a Mule Jump §9§m  ");
			board.registerNewObjective("llama-speed", "dummy", "§9§m  §a Llama Speed §9§m  ");
			board.registerNewObjective("llama-health", "dummy", "§9§m  §a Llama Health §9§m  ");
			board.registerNewObjective("llama-jump", "dummy", "§9§m  §a Llama Jump §9§m  ");
			board.registerNewObjective("trader_llama-h", "dummy", "§9§m  §a TraderLlama Health §9§m  ");
			board.registerNewObjective("skeleton_horse-j", "dummy", "§9§m  §a SkeleHorse Jump §9§m  ");
		}
		catch(IllegalArgumentException ex){}
		// Display all different equine stats in a loop
		/*new BukkitRunnable(){
			final Scoreboard board = pl.getServer().getScoreboardManager().getMainScoreboard();
			final String[] horseTypes = new String[]{"horse", "donkey", "mule", "llama"};
			final String[] statTypes = new String[]{"speed", "jump", "health"};
			int typeI = 0, statI = 0;
			@Override public void run(){
				if(typeI > 3){
					if(typeI == 4){board.getObjective("trader_llama-h").setDisplaySlot(DisplaySlot.SIDEBAR); typeI = 5;}
					else{board.getObjective("skeleton_horse-j").setDisplaySlot(DisplaySlot.SIDEBAR); typeI = 0;}
					return;
				}
				board.getObjective(horseTypes[typeI]+"-"+statTypes[statI]).setDisplaySlot(DisplaySlot.SIDEBAR);
				if((statI = ++statI % 3) == 0) typeI = ++typeI % 6;
			}
		}.runTaskTimer(pl, 20*5, 20*5);*/
	}

	private void updateHorseScoreboard(AbstractHorse horse, String name){
		pl.getLogger().fine("Adding horse scoareboard statistics for '"+name+"'");
		final Scoreboard board = pl.getServer().getScoreboardManager().getMainScoreboard();
		switch(horse.getType()){
			case HORSE:
			case DONKEY:
			case MULE:
			case LLAMA:
				final String horseType = horse.getType().name().toLowerCase();
				board.getObjective(horseType+"-speed").getScore(name).setScore((int)(100*HorseUtils.getNormalSpeed(horse)));
				board.getObjective(horseType+"-jump").getScore(name).setScore((int)(100*HorseUtils.getNormalJump(horse)));
				board.getObjective(horseType+"-health").getScore(name).setScore(HorseUtils.getNormalMaxHealth(horse));
				return;
			case TRADER_LLAMA:
				board.getObjective("trader_llama-h").getScore(name).setScore(HorseUtils.getNormalMaxHealth(horse));
				return;
			case SKELETON_HORSE:
				board.getObjective("skeleton_horse-j").getScore(name).setScore((int)(100*HorseUtils.getNormalJump(horse)));
				return;
			default:
		}
	}

	private void renameHorseScoreboard(String oldName, String newName){
		pl.getLogger().fine("Updating horse scoareboard statistics from '"+oldName+"' to '"+newName+"'");
		final HashMap<Objective, Integer> oldScores = new HashMap<>();
		final HashMap<Objective, Integer> horseScores = new HashMap<>();
		final Scoreboard board = pl.getServer().getScoreboardManager().getMainScoreboard();
		for(String objectiveName : horseObjectives){
			final Objective objective = board.getObjective(objectiveName);
			final Score score = objective.getScore(oldName);
			if(!score.isScoreSet()) continue;
			if(horseObjectives.contains(objective.getName())) horseScores.put(objective, score.getScore());
			else oldScores.put(objective, score.getScore());
		}
		board.resetScores(oldName);
		for(Entry<Objective, Integer> entry : oldScores.entrySet()){
			entry.getKey().getScore(oldName).setScore(entry.getValue());
		}
		if(newName != null)
		for(Entry<Objective, Integer> entry : horseScores.entrySet()){
			entry.getKey().getScore(newName).setScore(entry.getValue());
		}
	}

	@EventHandler public void onHorseClaim(HorseClaimEvent evt){
		if(evt.getEntity() instanceof AbstractHorse){
			updateHorseScoreboard((AbstractHorse) evt.getEntity(), evt.getClaimName());
		}
	}
	@EventHandler public void onHorseRename(HorseRenameEvent evt){
		renameHorseScoreboard(evt.getOldFullName(), evt.getNewFullName());
	}
	@EventHandler public void onHorseDeath(HorseDeathEvent evt){
		renameHorseScoreboard(evt.getEntity().getCustomName(), null);
	}
}
