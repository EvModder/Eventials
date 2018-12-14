package Eventials.custombows;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import EvLib.CommandBase2;
import EvLib.EvPlugin;
import Eventials.custombows.CustomBows.BowType;

public class CommandMakeBow extends CommandBase2{
	CustomBows bowManager;

	public CommandMakeBow(EvPlugin p, CustomBows bows){
		super(p);
		bowManager = bows;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
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
