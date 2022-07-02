package Eventials.economy.commands;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import Eventials.economy.ServerEconomy;
import net.evmodder.EvLib.EvCommand;

public class CommandBaltop extends EvCommand{
	private final ServerEconomy economy;

	public CommandBaltop(JavaPlugin pl, ServerEconomy serverEconomy, boolean enabled){
		super(pl, enabled);
		economy = serverEconomy;
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/baltop [page]
		int page = -1;
		if(args.length > 0){
			try{page = Integer.parseInt(args[0]);}
			catch(NumberFormatException ex){}
		}
		economy.showBaltop(sender, page);
		return true;
	}
}