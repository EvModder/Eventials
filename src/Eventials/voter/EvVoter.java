package Eventials.voter;

import Eventials.Eventials;
import Eventials.economy.EvEconomy;
import net.evmodder.EvLib.hooks.EssEcoHook;
import net.evmodder.EvLib.FileIO;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EvVoter implements Listener{
	private static EvVoter voter; public static EvVoter getVoteManager(){return voter;}

	private EvEconomy eco;
	private Eventials plugin;
//	private List<UUID> fireworkWaits = new ArrayList<UUID>();
	private YamlConfiguration voters;
	final int streakOp, streakAmount, streakMax;
	final double playerCash, serverCash;
	final boolean serverPays, trackGlobalBal;
	final long hrInMillis = 3600000, dayInMillis = 24*hrInMillis, graceInMillis;

	public EvVoter(Eventials pl){
		voter = this;
		plugin = pl;
		eco = EvEconomy.getEconomy();
		voters = FileIO.loadYaml("voters.yml", "");

		boolean votifierInstalled = plugin.getServer().getPluginManager().getPlugin("Votifier") != null;
		if(votifierInstalled){
			new EvVoteListener();
		}
		else{
			plugin.getLogger().warning("Votifier is not installed on this server!");
		}

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		new CommandVote(plugin, this/*, votifierInstalled*/);

		streakOp = plugin.getConfig().getInt("vote-streak-op", 0);
		streakAmount = plugin.getConfig().getInt("vote-streak-amount", 0);
		streakMax = plugin.getConfig().getInt("vote-streak-max-days", 0);
		graceInMillis = plugin.getConfig().getInt("vote-streak-grace-hours", 24)*hrInMillis;
		playerCash = plugin.getConfig().getDouble("player-cash-reward", 0);
		serverCash = plugin.getConfig().getDouble("server-cash-reward", 0);
		serverPays = plugin.getConfig().getBoolean("server-pays-player-cash-reward", true);
		trackGlobalBal = plugin.getConfig().getBoolean("track-global-balance", true);
	}

	public void onDisable(){
		FileIO.saveYaml("voters.yml", voters);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent evt){
		String uuid = evt.getPlayer().getUniqueId().toString();
		ConfigurationSection offlineVotes = voters.getConfigurationSection(uuid+".offline");
		if(offlineVotes == null) return;
		for(String streakVote : offlineVotes.getKeys(false)){
			if(!StringUtils.isNumeric(streakVote)) continue;
			int streak = Integer.parseInt(streakVote), votes = offlineVotes.getInt(streakVote);
			for(int i=0; i<votes; ++i) rewardPlayer(evt.getPlayer(), streak);
		}
		voters.set(uuid+".offline", null);
	}

/*	@EventHandler
	public void onChangeItemHeld(PlayerItemHeldEvent evt){
		while(fireworkWaits.remove(evt.getPlayer().getUniqueId())){
			Rewards.giveFirework(evt.getPlayer());
		}
	}*/

	public int getStreak(OfflinePlayer voter){
		String uuid = voter.getUniqueId().toString();
		if(!voters.isConfigurationSection(uuid)) return 0;

		// Hasn't voted in >X hours, streak is 0
		long timeSinceVote = System.currentTimeMillis() - voters.getLong(uuid+".streak-last", 0);
		if(timeSinceVote > dayInMillis + graceInMillis) voters.set(uuid+".streak", 0);
		return voters.getInt(uuid+".streak", 0);
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
		// Can only get streak bonus once per day.
		else streak = 0;
		if(voter.isOnline()) rewardPlayer(voter.getPlayer(), streak);
		else{
			if(!voters.isConfigurationSection(uuid+".offline")) voters.createSection(uuid+".offline");
			voters.set(uuid+".offline."+streak, voters.getInt(uuid+".offline."+streak, 0) + 1);
		}
	}

	public long lastVote(UUID uuid){
		return voters.getLong(uuid.toString()+".last", 0);
	}
	public long lastStreakVote(UUID uuid){
		return voters.getLong(uuid.toString()+".streak-last", 0);
	}

	@SuppressWarnings("deprecation")
	public void rewardPlayer(Player p, int streak){
		Rewards.give(p);
//		fireworkWaits.add(p.getUniqueId());
		Rewards.giveFirework(p);

		//voteMoney() section
		streak = Math.min(streak, streakMax);
		eco.payServer(serverCash);
		eco.addGlobalBal(serverCash);

		double amount = playerCash;
		if(streakOp == 0) amount += streakAmount*streak;
		else if(streakOp == 1) amount *= streakAmount*streak;

		if(serverPays){
			eco.serverToPlayer(p.getUniqueId(), amount);
		}
		else if(EssEcoHook.giveMoney(p, amount) && trackGlobalBal){
			eco.addGlobalBal(amount);
		}
	}
}