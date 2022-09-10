package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import Eventials.Eventials;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.extras.ReflectionUtils;
import net.evmodder.EvLib.extras.ReflectionUtils.RefClass;
import net.evmodder.EvLib.extras.ReflectionUtils.RefMethod;

public class CommandStat extends EvCommand{
	private final Eventials pl;
	private final ScoreboardManager boardManager;
	private final HashMap<Objective, Scoreboard> perObjScoreboards;
	private final HashMap<UUID, BukkitRunnable> activeTempDisplays;
	private final Object nmsMainBoard;
	private final RefMethod methodGetObjective, methodGetScore;
	@SuppressWarnings("rawtypes")
	private Set playerScores;

	public CommandStat(Eventials p){
		super(p); pl = p;
		boardManager = p.getServer().getScoreboardManager();
		perObjScoreboards = new HashMap<>();
		activeTempDisplays = new HashMap<>();
		RefMethod methodGetHandle = ReflectionUtils.getRefClass("{cb}.scoreboard.CraftScoreboard").getMethod("getHandle");
		RefClass classScoreboard = ReflectionUtils.getRefClass("{nm}.world.scores.Scoreboard");
		methodGetObjective = classScoreboard.findMethod(/*isStatic=*/false,
				ReflectionUtils.getRefClass("{nm}.world.scores.ScoreboardObjective"), String.class);
		methodGetScore = ReflectionUtils.getRefClass("{nm}.world.scores.ScoreboardScore").findMethod(/*isStatic=*/false, int.class);
		nmsMainBoard = methodGetHandle.of(boardManager.getMainScoreboard()).call();
		for(Field f : classScoreboard.getRealClass().getDeclaredFields()){
			if(!f.getType().equals(Map.class)) continue;
			f.setAccessible(true);
			try{
				@SuppressWarnings("rawtypes")
				Map map = (Map)f.get(nmsMainBoard);
				if(map.isEmpty()) continue;
//				System.out.println("map is not empty");
				Object value = map.values().iterator().next();
				if(value instanceof Map == false) continue;
//				System.out.println("map value is a map");
				@SuppressWarnings("rawtypes")
				Map subMap = (Map)value;
				if(subMap.isEmpty()) continue;
//				System.out.println("map value is not empty");
				if(!ReflectionUtils.getRefClass("{nm}.world.scores.ScoreboardObjective").getRealClass()
						.equals(subMap.keySet().iterator().next().getClass())) continue;
//				System.out.println("map value key is ScoreboardObjective");
				if(!ReflectionUtils.getRefClass("{nm}.world.scores.ScoreboardScore").getRealClass()
						.equals(subMap.values().iterator().next().getClass())) continue;
//				System.out.println("map value value is ScoreboardScore");
				playerScores = map.entrySet();
			}
			catch(IllegalArgumentException | IllegalAccessException e){e.printStackTrace();}
		}
	}

	private static List<String> statNames = null;
	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){
		if(args.length != 1) return ImmutableList.of(); //TODO: 2nd arg for display time in seconds (or toggle?)
		if(args[0].startsWith("zstats-")/* || args[0].startsWith("istats-")*/) args[0] = args[0].substring(7);
		if(statNames == null){
			statNames = boardManager.getMainScoreboard().getObjectives().stream()
					.map(obj -> {
						String objName = obj.getName();
						if(objName.startsWith("zstats-")/* || objName.startsWith("istats-")*/) objName = objName.substring(7);
						return objName;
					})
					.toList();
		}
		// TODO: Prefix tree would be more optimal
		int sharedPrefixLen = args[0].length();
		while(true){
			HashMap<String, String> tabCompletes = new HashMap<String, String>();
			for(String statName : statNames){
				if(!statName.startsWith(args[0])) continue;
				final int currPartEnd = statName.indexOf('_', sharedPrefixLen + 1);
//				pl.getLogger().info("currPartEnd: "+currPartEnd);
				final String tabComplete = statName.substring(0, currPartEnd == -1 ? statName.length() : currPartEnd+1);
//				pl.getLogger().info("obj: "+statName+", tabc: "+tabComplete);
				if(tabCompletes.put(tabComplete, statName) != null) tabCompletes.put(tabComplete, tabComplete);
			}
			if(tabCompletes.size() != 1 || !tabCompletes.values().iterator().next().endsWith("_")) return tabCompletes.values().stream().toList();
			sharedPrefixLen += tabCompletes.values().iterator().next().length();
		}
	}

	private boolean showObjective(Player player, Objective obj, long seconds){
		Scoreboard sb = perObjScoreboards.get(obj);
		if(sb == null){
			perObjScoreboards.put(obj, sb = boardManager.getNewScoreboard());
			sb.registerNewObjective(obj.getName(), obj.getCriteria(), obj.getDisplayName());
			Objective objCopy = sb.getObjective(obj.getName());
			Object nmsObj = methodGetObjective.of(nmsMainBoard).call(obj.getName());
			//System.out.println("hmmmm: "+playerScores.size());
			for(Object entry : playerScores){
				@SuppressWarnings("rawtypes") String entryName = (String)((java.util.Map.Entry)entry).getKey();
				@SuppressWarnings("rawtypes") Map nmsObjToScore = (Map)((java.util.Map.Entry)entry).getValue();
				Object nmsScore = nmsObjToScore.get(nmsObj);
				if(nmsScore != null){
					objCopy.getScore(entryName).setScore((int)methodGetScore.of(nmsScore).call());
//					System.out.println("added score for "+entryName+": "+(int)methodGetScore.of(nmsScore).call());
				}
			}
			//TODO: copy all scores from main into this new sb/obj
			//TODO: also copy tab-display and under-name-display obj to new sb (TODO: just online players? join/quit listener?)
			objCopy.setDisplaySlot(DisplaySlot.SIDEBAR);
			pl.getLogger().info("all scores of caller on sb: "+sb.getScores(player.getName()).size());
		}

		player.setScoreboard(perObjScoreboards.get(obj));
		final UUID uuid = player.getUniqueId();
		final BukkitRunnable oldTask = activeTempDisplays.get(uuid);
		if(oldTask != null) oldTask.cancel();
		final BukkitRunnable newTask = new BukkitRunnable(){@Override public void run(){
			Player player = pl.getServer().getPlayer(uuid);
			if(player != null) player.setScoreboard(boardManager.getMainScoreboard());
			activeTempDisplays.remove(uuid);
		}};
		newTask.runTaskLater(pl, seconds*20L);
		activeTempDisplays.put(uuid, newTask);
		return true;
	}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		if(args.length == 0){
			// Clear currently displayed statistic
			final BukkitRunnable oldTask = activeTempDisplays.remove(((Player)sender).getUniqueId());
			if(oldTask != null){
				oldTask.cancel();
				((Player)sender).setScoreboard(boardManager.getMainScoreboard());
				return true;
			}
			else{
				return false;
			}
		}
		Objective obj = boardManager.getMainScoreboard().getObjective(args[0]);
		if(obj == null) obj = boardManager.getMainScoreboard().getObjective("zstats-"+args[0]);
		if(obj == null) obj = boardManager.getMainScoreboard().getObjective("istats-"+args[0]);
		if(obj == null){
			sender.sendMessage(ChatColor.RED+"Unknown statistic: "+args[0]);
			return true;
		}
		final long seconds = args.length > 1 && args[1].matches("\\d+") ? Long.parseLong(args[1]) : 8;
		showObjective((Player)sender, obj, seconds);
		return true;
	}
}