package Eventials.economy.commands;

import java.math.BigDecimal;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import Eventials.economy.ServerEconomy;
import net.evmodder.EvLib2.CommandBase;

public class CommandGlobalBal extends CommandBase{
	ServerEconomy economy;

	public CommandGlobalBal(JavaPlugin pl, ServerEconomy serverEconomy, boolean enabled){
		super(pl, enabled);
		economy = serverEconomy;
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/globalbal

		BigDecimal bal = economy.getGlobalBal();
		sender.sendMessage(ChatColor.GRAY+"Global balance: "
				+ (bal.compareTo(BigDecimal.ZERO) > 0 ? ChatColor.GREEN : ChatColor.RED) + bal);
		return true;
	}
}