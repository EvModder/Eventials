package Eventials.commands;

import java.util.Collection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import EvLib.CommandBase2;
import EvLib.EvPlugin;

public class CommandDebug extends CommandBase2{

	public CommandDebug(EvPlugin p) {
		super(p);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		if(args.length == 0) listDebugCommands(sender);
		return true;
	}

	Collection<String> enabledDebugMsgs(Player p){
		return null;
	}

	void listDebugCommands(CommandSender sender){
		
	}
}