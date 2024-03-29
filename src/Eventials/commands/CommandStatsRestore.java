package Eventials.commands;

import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.google.common.collect.ImmutableList;
import Eventials.Eventials;
import Eventials.Extras;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.extras.TellrawUtils.TextClickAction;
import net.evmodder.EvLib.extras.TellrawUtils.ClickEvent;
import net.evmodder.EvLib.extras.TellrawUtils.ListComponent;

public class CommandStatsRestore extends EvCommand {
	public CommandStatsRestore(EvPlugin p){
		super(p);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){
		return args.length <= 1 ? null : ImmutableList.of();
	}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(args.length == 0) return false;

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
				ListComponent blob = new ListComponent();
				blob.addComponent(""+ChatColor.RED+ChatColor.BOLD+"Warning:"+ChatColor.GRAY+" This will overwrite existing stats!\n"+ChatColor.RED+"[");
				blob.addComponent(new RawTextComponent(ChatColor.GOLD+" Confirm ",
						new TextClickAction(ClickEvent.RUN_COMMAND, "/statsrestore "+target.getName()+" confirm")));
				blob.addComponent(ChatColor.RED+"]");
				Eventials.getPlugin().sendTellraw(sender.getName(), blob.toString());
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
