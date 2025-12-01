package Eventials.economy.commands;

import org.bukkit.ChatColor;
import java.math.BigDecimal;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import Eventials.economy.ServerEconomy;
import net.evmodder.EvLib.bukkit.EvCommand;

public class CommandDonateServer extends EvCommand{
	ServerEconomy economy;

	public CommandDonateServer(JavaPlugin pl, ServerEconomy eco, boolean enabled){
		super(pl, enabled);
		economy = eco;
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/donateserver [amt]

		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
		}
		else if(args.length == 0){
			sender.sendMessage(ChatColor.RED+"Too few arguments!"+ChatColor.GRAY+'\n'+command.getUsage());
		}
		else if(args.length > 1){
			sender.sendMessage(ChatColor.RED+"Too many arguments!"+ChatColor.GRAY+'\n'+command.getUsage());
		}
		else{
			BigDecimal amt;
			try{amt = new BigDecimal(args[0]);}
			catch(NumberFormatException ex){
				sender.sendMessage(ChatColor.RED+"Invalid numeric amount"+ChatColor.GRAY+'\n'+command.getUsage());
				return true;
			}
			if(amt.compareTo(BigDecimal.ZERO) != 1){
				sender.sendMessage(ChatColor.RED+"Please enter a positive, non-zero number amount");
				return true;
			}
			if(economy.playerToServer(((Player)sender).getUniqueId(), amt)){
				economy.addDonatedAmount(((Player)sender).getUniqueId(), amt.longValue());
				sender.sendMessage(ChatColor.GREEN+"Successfully donated "+ChatColor.YELLOW+amt+ChatColor.DARK_GREEN+'L'
						+ChatColor.GREEN+" to the server!");
			}
			else sender.sendMessage(ChatColor.RED+"Invalid/Insufficient funds!");
		}
		return true;
	}
}