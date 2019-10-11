package Eventials.custombows;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import Eventials.custombows.CustomBows.BowType;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;

public class CommandMakeBow extends EvCommand{
	CustomBows bowManager;

	public CommandMakeBow(EvPlugin p, CustomBows bows){
		super(p);
		bowManager = bows;
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String Label, String[] args){
		if(args.length == 1 && sender instanceof Player){
			final List<String> tabCompletes = new ArrayList<String>();
			args[0] = args[0].toLowerCase();
			for(BowType bow : BowType.values()){
				if(bow.name().toLowerCase().startsWith(args[0])) tabCompletes.add(bow.name().toLowerCase());
			}
			return tabCompletes;
		}
		return null;
	}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/makebow <type>
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
			return true;
		}
		BowType type;

		if(args.length < 1){
			sender.sendMessage(ChatColor.RED+"Too few arguments!\n"+ChatColor.GRAY+command.getUsage());
			return true;
		}
		else{
			try{type = BowType.valueOf(args[0].toUpperCase());}
			catch(IllegalArgumentException ex){
				sender.sendMessage(ChatColor.RED+"Invalid bow type\n"+ChatColor.GRAY+command.getUsage());
				return true;
			}
		}
		((Player)sender).getInventory().setItemInMainHand(bowManager.makeBow(type));
		return true;
	}
}
