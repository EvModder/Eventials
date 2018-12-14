package Eventials.voter;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.vexsoftware.votifier.model.VotifierEvent;
import Eventials.Eventials;

public class EvVoteListener implements Listener{
	public static final String prefix = ChatColor.YELLOW+"["+ChatColor.AQUA+"AC"+ChatColor.YELLOW+" "
			+ChatColor.STRIKETHROUGH+'-'+ChatColor.YELLOW+"> "+ChatColor.RED
			+"Me"+ChatColor.YELLOW+"]"+ChatColor.GRAY+" ";
	public static final String prefix2= ChatColor.AQUA+"["+ChatColor.GRAY+"AC"+ChatColor.AQUA+"]"+ChatColor.WHITE+" ";
	private Eventials plugin;
	private EvVoter voteManager;

	public EvVoteListener(){
		plugin = Eventials.getPlugin();
		voteManager = EvVoter.getVoteManager();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	// Voting event from com.vexsoftware.votifier.model.VotifierEvent
	@EventHandler public void vote(VotifierEvent evt){
		if(evt.getVote().getUsername() == null || evt.getVote().getUsername().isEmpty()) {
			plugin.getLogger().warning("No username given when voting from "+evt.getVote().getServiceName());
			return;
		}
		@SuppressWarnings("deprecation")
		OfflinePlayer voter = plugin.getServer().getOfflinePlayer(evt.getVote().getUsername());
		if(voter == null){
			plugin.getLogger().warning("Unknown Voting Player: " + evt.getVote().getUsername());
			return;
		}
		String voteSite = evt.getVote().getServiceName();
		if(evt.getVote().getAddress() == null || evt.getVote().getAddress().isEmpty()) {
			plugin.getLogger().info("An address was not given when voting from "+voteSite);
		}

		plugin.getLogger().fine("Player "+voter.getName()+" voted for the server on "+voteSite);

		int streak = voteManager.addVoteAndGetStreak(voter);

		if(voter.isOnline()){
			voteManager.rewardPlayer(voter.getPlayer(), streak);
			voter.getPlayer().sendMessage(prefix+"Thanks for voting for us on "+voteSite+'!');

			for(Player p : plugin.getServer().getOnlinePlayers()){
				if(!voter.getName().equals(p.getName())) {
					p.sendMessage(prefix2+evt.getVote().getUsername()+" voted for us on "+voteSite+'.');
				}
			}
		}
		else{
			plugin.getServer().broadcastMessage(prefix2+voter.getName()+" voted for us on "+voteSite+'.');
		}
	}

//	@Override public void voteMade(Vote vote){
//		plugin.getLogger().info("test msg from EvVoter voteMade()");// never seems to happen
//	}
}