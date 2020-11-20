package Eventials.voter;

import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.vexsoftware.votifier.model.VotifierEvent;
import Eventials.Eventials;

public class EvVoteListener implements Listener{
	private Eventials plugin;
	private EvVoter voteManager;
	final List<String> voteTriggeredCommands;

	public EvVoteListener(){
		plugin = Eventials.getPlugin();
		voteManager = EvVoter.getVoteManager();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		voteTriggeredCommands =plugin.getConfig().getStringList("vote-triggered-commands");
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
		plugin.getLogger().fine("Player "+voter.getName()+" voted for the server on "+voteSite);
		if(evt.getVote().getAddress() == null || evt.getVote().getAddress().isEmpty()) {
			plugin.getLogger().info("An address was not given when voting from "+voteSite);
		}

		voteManager.applyVote(voter);

		for(String cmd : voteTriggeredCommands){
			cmd = cmd.replace("%uuid%", voter.getUniqueId().toString()).replace("%name%", voter.getName())
					.replace("%display_name%", voter.isOnline() ? voter.getPlayer().getDisplayName() : voter.getName())
					.replace("%votes%", ""+voteManager.getTotalVotes(voter.getUniqueId()))
					.replace("%streak%", ""+voteManager.getStreak(voter.getUniqueId()))
					.replace("%site_name%", voteSite)
					.replace("%site_url%", evt.getVote().getAddress()) // TODO: check if this is the site or the player's ip lol
			;
			plugin.runCommand(cmd);
		}
	}

//	@Override public void voteMade(Vote vote){
//		plugin.getLogger().info("test msg from EvVoter voteMade()");// never seems to happen
//	}
}