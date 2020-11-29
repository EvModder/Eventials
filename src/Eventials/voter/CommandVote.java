package Eventials.voter;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import Eventials.Eventials;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.TellrawUtils.ActionComponent;
import net.evmodder.EvLib.extras.TellrawUtils.ClickEvent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.extras.TellrawUtils.TellrawBlob;
import net.evmodder.EvLib.extras.TextUtils;

public class CommandVote extends EvCommand{
	final EvVoter voteManager;
	final String websiteLink, tellrawStringLinks;

	public CommandVote(EvPlugin pl, EvVoter v){this(pl, v, true);}
	public CommandVote(EvPlugin pl, EvVoter v, boolean enabled){
		super(pl, enabled);
		voteManager = EvVoter.getVoteManager();
		List<String> confLinks = pl.getConfig().getStringList("vote-links");
		websiteLink = pl.getConfig().getString("vote-site-page", null);
		final String[] links, hyper;
		if(confLinks == null/* || confLinks.isEmpty()*/){// What if they don't want voting links?
			links = new String[]{
				"https://www.planetminecraft.com/",
				"http://minecraft-mp.com/",
				"http://minecraftservers.org/",
				"http://minecraft-server-list.com/",
				"https://minecraftservers.biz/",
				"https://www.minecraft-index.com/"
			};
			hyper = new String[]{
				""+ChatColor.AQUA+ChatColor.UNDERLINE+" PMC", ""+ChatColor.AQUA+ChatColor.UNDERLINE+" MMP",
				""+ChatColor.AQUA+ChatColor.UNDERLINE+" MC Servers", ""+ChatColor.AQUA+ChatColor.UNDERLINE+" MCSL",
				""+ChatColor.AQUA+ChatColor.UNDERLINE+" MC biz", ""+ChatColor.AQUA+ChatColor.UNDERLINE+" MC Index"
			};
		}
		else{
			links = new String[confLinks.size()];
			hyper = new String[confLinks.size()];
			int i=0;
			for(String link : confLinks){
				int idx = link.indexOf(',');
				if(idx == -1){
					hyper[i] = ChatColor.AQUA+" Site";
					links[i] = link;
				}
				else{
					hyper[i] = ChatColor.AQUA+" "+link.substring(0, idx);
					links[i] = link.substring(idx+1).trim();
				}
				++i;
			}
		}
		TellrawBlob blob = new TellrawBlob();
		for(int i=0; i<links.length; ++i){
			if(i == 0) blob.addComponent(TextUtils.translateAlternateColorCodes('&', "&eVoting Links:  &d1&7."));
			else blob.addComponent("  "+ChatColor.LIGHT_PURPLE+(i+1)+ChatColor.GRAY+".");
			blob.addComponent(new ActionComponent(hyper[i], ClickEvent.OPEN_URL, links[i]));
		}
		tellrawStringLinks = blob.toString();
	}

	@Override
	public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/vote
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
		}
		Player player = (Player)sender;
		if(websiteLink != null && !websiteLink.isEmpty()){
			Eventials.getPlugin().sendTellraw(player, new TellrawBlob(
					new RawTextComponent(ChatColor.YELLOW+"Voting"),
					new ActionComponent(ChatColor.AQUA+" website page", ClickEvent.OPEN_URL, websiteLink))
					.toString());
		}
		if(tellrawStringLinks.length() > 2) Eventials.getPlugin().sendTellraw(player, tellrawStringLinks);

		int votes = voteManager.getTotalVotes(player.getUniqueId());
		if(votes > 0){
			int streak = voteManager.getStreak(player.getUniqueId());
			player.sendMessage(ChatColor.GRAY+"Total votes: "+ChatColor.GOLD+votes);
			ChatColor c = (streak > 0 ? streak > voteManager.streakMax ? ChatColor.GREEN : ChatColor.AQUA : ChatColor.YELLOW);
			player.sendMessage(ChatColor.GRAY+"Your voting streak: "+c+streak);
			if(streak > 0){
				long now = System.currentTimeMillis();
				long lastVote = voteManager.getLastVote(player.getUniqueId());
				long timeSinceVote = now - lastVote;
				long timeLeft = (voteManager.dayInMillis + voteManager.graceInMillis) - timeSinceVote;
				player.sendMessage(ChatColor.GRAY+"Time until streak is lost: "
						+TextUtils.formatTime(timeLeft, false, ChatColor.GOLD, ChatColor.GRAY));
	
				if(timeSinceVote < voteManager.dayInMillis){
					long lastStreakVote = voteManager.getLastStreakVote(player.getUniqueId());
					long timeUntilIncr = voteManager.dayInMillis - (now - lastStreakVote);
					player.sendMessage(ChatColor.GRAY+"Time until streak can be increased: "
							+TextUtils.formatTime(timeUntilIncr, false, ChatColor.GOLD, ChatColor.GRAY));;
				}
			}
		}
		return true;
	}
}