package Eventials.voter;

import Eventials.Eventials;
import Eventials.economy.Economy;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.VaultHook;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EvVoter implements Listener{
	private static EvVoter voter; public static EvVoter getVoteManager(){return voter;}

	private Economy eco;
	private Eventials plugin;
//	private List<UUID> fireworkWaits = new ArrayList<UUID>();
	private YamlConfiguration voters;
	final int streakOp, streakAmount, streakMax, graceHours;
	final double playerCash, serverCash;
	final boolean serverPays, trackGlobalBal;
	final long hrsInMillis = 3600000;

	public EvVoter(Eventials pl){
		voter = this;
		plugin = pl;
		eco = Economy.getEconomy();
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
		graceHours = plugin.getConfig().getInt("vote-streak-grace-hours", 24);
		playerCash = plugin.getConfig().getDouble("player-cash-reward", 0);
		serverCash = plugin.getConfig().getDouble("server-cash-reward", 0);
		serverPays = plugin.getConfig().getBoolean("server-pays-player-cash-reward", true);
		trackGlobalBal = plugin.getConfig().getBoolean("track-global-balance", true);
	}

	public void onDisable(){
		try{voters.save("./plugins/EvFolder/voters.yml");}
		catch(IOException e){e.printStackTrace();}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent evt){
		String uuid = evt.getPlayer().getUniqueId().toString();
		if(voters.contains(uuid)){
			long now = new GregorianCalendar().getTimeInMillis();
			long lastVote = voters.getLong(uuid+".last", 0);
			int offlineDays = (int)((now - lastVote)/(24*hrsInMillis));
			int streak = voters.getInt(uuid+".streak");
			int offlineVotes = voters.getInt(uuid+".offline");
			voters.set(uuid+".offline", 0);
			for(int i=0; i<offlineDays && i<offlineVotes; ++i) rewardPlayer(evt.getPlayer(), streak-i);
			for(int i=offlineDays; i<offlineVotes; ++i) rewardPlayer(evt.getPlayer(), 0);
		}
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

		// Gone for X+ hours, streak is 0
		long timeSinceVote = new GregorianCalendar().getTimeInMillis() - voters.getLong(uuid+".last", 0);
		if(timeSinceVote > graceHours*hrsInMillis) voters.set(uuid+".streak", 0);
		return voters.getInt(uuid+".streak", 0);
	}

	int addVoteAndGetStreak(OfflinePlayer voter){
		String uuid = voter.getUniqueId().toString();
		if(!voters.isConfigurationSection(uuid)) voters.createSection(uuid);
		voters.set(uuid+".name", voter.getName());
		if(!voter.isOnline()){
			voters.set(uuid+".offline", voters.getInt(voter.getUniqueId()+".offline", 0) + 1);
		}
		voters.set(uuid+".total", voters.getInt(uuid+".total", 0) + 1);

		long now = new GregorianCalendar().getTimeInMillis();
		long lastVote = voters.getLong(uuid+".last", 0);
		// Gone for X hours, reset streak
		if(now - lastVote > graceHours*hrsInMillis) voters.set(uuid+".streak", 1);
		// Voted within last X hours, don't apply streak (or update lastVote)
		else if(now - lastVote < 24*hrsInMillis) return 0;
		//Voted within last X hours but not last 24 hours. Increment streak
		else voters.set(uuid+".streak", (voters.getInt(uuid+".streak", 0) + 1));
		voters.set(uuid+".last", now);

		return voters.getInt(uuid+".streak", 0);
	}

	public long getLastVoted(UUID uuid){
		return voters.getLong(uuid.toString()+".last", 0);
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
		else if(VaultHook.giveMoney(p, amount) && trackGlobalBal){
			eco.addGlobalBal(amount);
		}
	}
}