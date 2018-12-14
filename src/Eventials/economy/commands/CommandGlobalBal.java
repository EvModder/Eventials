package Eventials.economy.commands;

import java.math.BigDecimal;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import EvLib.CommandBase2;
import Eventials.economy.ServerEconomy;

public class CommandGlobalBal extends CommandBase2{
	ServerEconomy economy;

	public CommandGlobalBal(JavaPlugin pl, ServerEconomy serverEconomy, boolean enabled){
		super(pl, enabled);
		economy = serverEconomy;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/globalbal

		BigDecimal bal = economy.getGlobalBal();
		sender.sendMessage(ChatColor.GRAY+"Global balance: "
				+ (bal.compareTo(BigDecimal.ZERO) > 0 ? ChatColor.GREEN : ChatColor.RED) + bal);
		return true;
	}
}