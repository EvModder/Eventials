package Eventials.voter;

import Eventials.Eventials;
import net.evmodder.EvLib.FileIO;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EvVoter implements Listener{
	private static EvVoter voter; public static EvVoter getVoteManager(){return voter;}
	private Eventials plugin;
	private YamlConfiguration voters;
	final int streakOp, streakMax;
	final double cashReward, streakAmount;
	final boolean trackGlobalBal;
	final long hrInMillis = 3600000, dayInMillis = 24*hrInMillis, graceInMillis;
	final List<String> onlineVoteRewardCommands;
	private boolean anyEvent = false;

	public EvVoter(Eventials pl){
		voter = this;
		plugin = pl;
		voters = FileIO.loadYaml("voters.yml", "");

		boolean votifierInstalled = plugin.getServer().getPluginManager().getPlugin("Votifier") != null;
		if(votifierInstalled) new EvVoteListener();
		else plugin.getLogger().warning("Votifier is not installed on this server!");

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		new CommandVote(plugin, this/*, votifierInstalled*/);

		streakOp = plugin.getConfig().getInt("vote-streak-op", 0);
		streakAmount = plugin.getConfig().getDouble("vote-streak-amount", 0D);
		streakMax = plugin.getConfig().getInt("vote-streak-max-days", 0);
		graceInMillis = plugin.getConfig().getInt("vote-streak-grace-hours", 24)*hrInMillis;
		cashReward = plugin.getConfig().getDouble("player-cash-reward", 0);
		trackGlobalBal = plugin.getConfig().getBoolean("track-global-balance", true);
		onlineVoteRewardCommands = plugin.getConfig().getStringList("vote-triggered-commands-once-online");
	}

	public void onDisable(){
		if(anyEvent) FileIO.saveYaml("voters.yml", voters);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent evt){
		String uuid = evt.getPlayer().getUniqueId().toString();
		ConfigurationSection offlineVotes = voters.getConfigurationSection(uuid+".offline");
		if(offlineVotes == null) return;
		for(String streakVote : offlineVotes.getKeys(false)){
			try{
				int streak = Integer.parseInt(streakVote), votes = offlineVotes.getInt(streakVote);
				for(int i=0; i<votes; ++i) rewardOnlinePlayer(evt.getPlayer(), streak);
			}catch(NumberFormatException ex){};
		}
		voters.set(uuid+".offline", null);
		anyEvent = true;
	}

	void applyVote(OfflinePlayer voter){
		String uuid = voter.getUniqueId().toString();
		if(!voters.isConfigurationSection(uuid)) voters.createSection(uuid);
		voters.set(uuid+".name", voter.getName());
		voters.set(uuid+".total", voters.getInt(uuid+".total", 0) + 1);

		long now = System.currentTimeMillis();
		long lastVote = voters.getLong(uuid+".last", 0);
		long lastVoteOfStreak = voters.getLong(uuid+".last-streak", 0);
		int streak = voters.getInt(uuid+".streak", 0);
		voters.set(uuid+".last", now);

		// Hasn't voted in >X hours, reset streak
		if(now - lastVote > dayInMillis + graceInMillis){
			voters.set(uuid+".streak", streak=1);
			voters.set(uuid+".streak-last", now);
		}
		// Only increment streak if >24 hours since last streak vote
		else if(now - lastVoteOfStreak > dayInMillis){
			voters.set(uuid+".streak", ++streak);
			voters.set(uuid+".streak-last", now);
		}
		// Can only get streak bonus once per day
		else streak = 0;
		if(voter.isOnline()) rewardOnlinePlayer(voter.getPlayer(), streak);
		else{
			if(!voters.isConfigurationSection(uuid+".offline")) voters.createSection(uuid+".offline");
			voters.set(uuid+".offline."+streak, voters.getInt(uuid+".offline."+streak, 0) + 1);
		}
		anyEvent = true;
	}

	public long getLastVote(UUID uuid){
		return voters.getLong(uuid.toString()+".last", 0);
	}
	public long getLastStreakVote(UUID uuid){
		return voters.getLong(uuid.toString()+".streak-last", 0);
	}
	public int getTotalVotes(UUID uuid){
		return voters.getInt(uuid.toString()+".total", 0);
	}
	public int getStreak(UUID uuid){
		if(!voters.isConfigurationSection(uuid.toString())) return 0;

		// Hasn't voted in >X hours, streak is 0
		long timeSinceVote = System.currentTimeMillis() - voters.getLong(uuid.toString()+".streak-last", 0);
		if(timeSinceVote > dayInMillis + graceInMillis){voters.set(uuid.toString()+".streak", 0); anyEvent=true;}
		return voters.getInt(uuid+".streak", 0);
	}

	public void rewardOnlinePlayer(Player p, int streak){
//		Rewards.give(p);
//		Rewards.giveFirework(p);
		
		int limitedStreak = Math.min(streak, streakMax);
		double payout = cashReward;
		if(streakOp == 0) payout += streakAmount*limitedStreak;
		else if(streakOp == 1) payout *= streakAmount*limitedStreak;

		for(String cmd : onlineVoteRewardCommands){
			cmd = cmd.replace("%uuid%", p.getUniqueId().toString())
					.replace("%name%", p.getName()).replace("%display_name%", p.getDisplayName())
					.replace("%votes%", ""+getTotalVotes(p.getUniqueId())).replace("%streak%", ""+streak)
					.replace("%reward_amount%", ""+payout).replace("%reward_amount_int%", ""+((int)payout));
			plugin.runCommand(cmd);
		}
	}
}