package Eventials.voter;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.EvUtils;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.extras.TextUtils.TextAction;

public class CommandVote extends EvCommand{
	final String[] links;
	final String website;
	final String[] hyper;
	final String[] nonHyper;
	final TextAction[] clickResult;
	final EvVoter voteManager;
	long hrs48 = 172800000;//TODO: move this constant into the config

	public CommandVote(EvPlugin pl, EvVoter v){this(pl, v, true);}
	public CommandVote(EvPlugin pl, EvVoter v, boolean enabled){
		super(pl, enabled);
		voteManager = EvVoter.getVoteManager();
		List<String> confLinks = pl.getConfig().getStringList("vote-links");
		website = pl.getConfig().getString("vote-site-page", "http://www.altcraft.net/voting");
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
				ChatColor.AQUA+" PMC", ChatColor.AQUA+" MMP", ChatColor.AQUA+" MC Servers",
				ChatColor.AQUA+" MCSL", ChatColor.AQUA+" MC biz", ChatColor.AQUA+" MC Index"
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

		nonHyper = new String[links.length];
		clickResult = new TextAction[links.length];
		if(links.length != 0){
			for(int i=0; i<links.length;){
				clickResult[i] = TextAction.LINK;
				nonHyper[i] = TextUtils.translateAlternateColorCodes('&', " &d"+(++i)+"&7.");
			}
			nonHyper[0] = TextUtils.translateAlternateColorCodes('&', "&eVoting Links:\\n&d1&7.");
		}
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/vote
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
		}
		if(website != null && !website.isEmpty())
			TextUtils.sendModifiedText(
					new String[]{ChatColor.YELLOW+"Voting"},
					new String[]{ChatColor.AQUA+" website page"},
					new TextAction[]{TextAction.LINK},
					new String[]{website}, "", (Player)sender);
		if(links.length > 0)
			TextUtils.sendModifiedText(nonHyper, hyper, clickResult, links, "", (Player)sender);

		int s = voteManager.getStreak((Player)sender);
		ChatColor c = (s > 0 ? s > voteManager.streakMax ? ChatColor.GREEN : ChatColor.AQUA : ChatColor.YELLOW);
		sender.sendMessage(ChatColor.GRAY+"Your voting streak: "+c+s);
		if(s > 0){
			long time = hrs48 + voteManager.getLastVoted(((Player)sender).getUniqueId()) - System.currentTimeMillis();
			sender.sendMessage(ChatColor.GRAY+"Time until streak is lost: "
					+EvUtils.formatTime(time, ChatColor.GOLD, ChatColor.GRAY));
		}
		return true;
	}
}