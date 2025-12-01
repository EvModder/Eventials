package Eventials.commands;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.google.common.collect.ImmutableList;
import net.evmodder.EvLib.bukkit.EvCommand;
import net.evmodder.EvLib.bukkit.EvPlugin;
import net.evmodder.EvLib.bukkit.TellrawUtils;
import net.evmodder.EvLib.bukkit.TellrawUtils.Component;

public class CommandEvTellraw extends EvCommand{
	public CommandEvTellraw(EvPlugin p){super(p);}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return ImmutableList.of();}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		String tellraw = String.join(" ", args);
		if(tellraw.isEmpty()) return false;
		Component comp = TellrawUtils.parseComponentFromString(tellraw);
		sender.sendMessage("toString(): "+comp.toString());
		sender.sendMessage("toPlainText(): "+comp.toPlainText());
		return true;
	}
}