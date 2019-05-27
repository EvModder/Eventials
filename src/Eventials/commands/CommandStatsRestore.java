package Eventials.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import Eventials.Eventials;
import Eventials.Extras;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.TextUtils;
import net.md_5.bungee.api.ChatColor;

public class CommandStatsRestore extends EvCommand {
	public CommandStatsRestore(EvPlugin p){
		super(p);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(args.length == 0) return false;//TODO: Use this from now on, not sendMessage("too few args");

		@SuppressWarnings("deprecation")
		OfflinePlayer target = Eventials.getPlugin().getServer().getOfflinePlayer(args[0]);
		if(target == null) target = Eventials.getPlugin().getServer().getOfflinePlayer(UUID.fromString(args[0]));

		if(target == null){
			sender.sendMessage(ChatColor.RED+"Player \""+args[0]+"\" not found");
			return true;
		}
		if(target.isOnline()){
			sender.sendMessage(ChatColor.RED+"Cannot recover stats for an online player!");
			return true;
		}
		if(target.hasPlayedBefore()){
			if(sender instanceof Player && (args.length == 1 || !args[1].equals("confirm"))){
				//-----------------------------------------------------------
				TextUtils.sendModifiedText(""+ChatColor.RED+ChatColor.BOLD+"Warning:"+
						ChatColor.GRAY+" This will overwrite existing stats!\n"+ChatColor.RED+"[",
						ChatColor.GOLD+" Confirm ", TextUtils.TextAction.RUN_CMD,
						"/clearstats "+target.getName()+" confirm",
						ChatColor.RED+"]", (Player)sender);
				//-----------------------------------------------------------
				return true;
			}
		}
		if(Extras.undeletePlayer(target))
			sender.sendMessage(ChatColor.GREEN+"Restored data files for: "+ChatColor.GRAY+target.getName());
		else
			sender.sendMessage(ChatColor.RED+"Unable to locate deleted data files for: "+ChatColor.GRAY+target.getName());
		return true;
	}
}
