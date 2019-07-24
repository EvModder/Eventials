package EventAndMisc;

import java.util.HashSet;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import Eventials.Eventials;
import net.evmodder.EvLib.EvUtils;

public class HC_ScoreboardUpdater implements Listener{
	final HashSet<String> included;
	final Scoreboard emptyBoard;
	final Eventials pl;

	public HC_ScoreboardUpdater(){
		pl = Eventials.getPlugin();
		included = new HashSet<String>();
		included.addAll(pl.getConfig().getStringList("advancements-included"));
		emptyBoard = pl.getServer().getScoreboardManager().getNewScoreboard();
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}

	boolean isPaidAdvancement(Advancement adv){
		int i = adv.getKey().getKey().indexOf('/');
		return adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) && i != -1
				&& included.contains(adv.getKey().getKey().substring(0, i));
	}

	static String getAdvancementTeamName(int numAdvancements){
		StringBuilder builder = new StringBuilder("Adv_");
		int i = 0;
		while(i<numAdvancements/26){builder.append('a'); ++i;}
		char ch = 'z'; ch -= numAdvancements%26;
		builder.append(ch); ++i;
		while(i<12){builder.append('z'); ++i;}
		return builder.toString();
	}

	void addObjectiveAndTeam(Player player, int numAdvancements){
		player.getScoreboard().getObjective("advancements").getScore(player.getName()).setScore(numAdvancements);

		String oldTeamName = getAdvancementTeamName(numAdvancements-1);
		Team oldTeam = player.getScoreboard().getTeam(oldTeamName);
		if(oldTeam != null) oldTeam.removeEntry(player.getName());
		String newTeamName = getAdvancementTeamName(numAdvancements);
		Team newTeam = player.getScoreboard().getTeam(newTeamName);
		if(newTeam == null) newTeam = player.getScoreboard().registerNewTeam(newTeamName);
		newTeam.addEntry(player.getName());
	}

	@EventHandler
	public void onAchievementGet(PlayerAdvancementDoneEvent evt){
		if(!isPaidAdvancement(evt.getAdvancement())) return;

		int advancements = EvUtils.getVanillaAdvancements(evt.getPlayer(), included).size();
		addObjectiveAndTeam(evt.getPlayer(), advancements);
	}

	@EventHandler
	public void onLevelUp(PlayerLevelChangeEvent evt){
		Scoreboard board = pl.getServer().getScoreboardManager().getMainScoreboard();
		evt.getPlayer().setScoreboard(board);
		final UUID uuid = evt.getPlayer().getUniqueId();
		new BukkitRunnable(){@Override public void run(){
			Player player = pl.getServer().getPlayer(uuid);
			if(player != null) player.setScoreboard(emptyBoard);
		}}.runTaskLater(pl, 20*10);
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt){
		Scoreboard board = pl.getServer().getScoreboardManager().getMainScoreboard();
		evt.getPlayer().setScoreboard(board);
		final UUID uuid = evt.getPlayer().getUniqueId();
		new BukkitRunnable(){@Override public void run(){
			Player player = pl.getServer().getPlayer(uuid);
			if(player != null) player.setScoreboard(emptyBoard);
		}}.runTaskLater(pl, 20*10);
	}
}