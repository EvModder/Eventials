package Eventials.commands;

import java.util.Collection;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.evmodder.EvLib.CommandBase;
import net.evmodder.EvLib.EvPlugin;

public class CommandDebug extends CommandBase{
	public CommandDebug(EvPlugin p){super(p);}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

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