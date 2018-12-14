package Eventials.economy.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import EvLib.CommandBase2;
import Eventials.economy.Economy;
import Eventials.economy.ServerEconomy;

public class CommandDonateTop extends CommandBase2{
	ServerEconomy economy;

	public CommandDonateTop(JavaPlugin pl, ServerEconomy eco){
		super(pl);
		economy = eco;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/donateserver [amt]

		int page = -1;
		if(args.length != 0){
			if(StringUtils.isNumeric(args[0])) page = Integer.parseInt(args[0]);
		}
		Economy.getEconomy().showDonateTop(sender, page);
		return true;
	}
}