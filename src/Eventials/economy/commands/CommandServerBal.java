package Eventials.economy.commands;

import org.bukkit.ChatColor;
import java.math.BigDecimal;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import EvLib.CommandBase2;
import Eventials.economy.ServerEconomy;

public class CommandServerBal extends CommandBase2{
	ServerEconomy economy;
	JavaPlugin plugin;

	public CommandServerBal(JavaPlugin pl, ServerEconomy eco, boolean enabled){
		super(pl, enabled);
		plugin = pl;
		economy = eco;
	}

	@SuppressWarnings("deprecation") @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/serverbal [send/add/charge] [amt]

		if(args.length == 0 || (args[0]=args[0].toLowerCase()).equals("view")){
			BigDecimal bal = economy.getServerBal();
			sender.sendMessage(ChatColor.GRAY+"Server's balance: "
					+ (bal.compareTo(BigDecimal.ZERO) > 0 ? ChatColor.GREEN : ChatColor.RED) + bal);
		}
		else if(args.length == 1){
			sender.sendMessage(ChatColor.RED+"Too few arguments!"+ChatColor.GRAY+'\n'+command.getUsage());
		}
		else if(args.length > 3){
			sender.sendMessage(ChatColor.RED+"Too many arguments!"+ChatColor.GRAY+'\n'+command.getUsage());
		}
		else if(args[0].equals("send") || args[0].equals("pay") || args[0].equals("transfer")){
			if(!sender.hasPermission("eventials.serverbal.transfer")){
				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command");
				return true;
			}
			if(args.length == 2){
				sender.sendMessage(ChatColor.RED+"Too few arguments!"+ChatColor.GRAY+'\n'+command.getUsage());
				return true;
			}
			BigDecimal amt;
			String target = args[1];
			try{amt = new BigDecimal(args[2]);}
			catch(NumberFormatException ex){
				try{amt = new BigDecimal(args[1]);}
				catch(NumberFormatException ex2){
					sender.sendMessage(ChatColor.RED+"Invalid numeric amount"+ChatColor.GRAY+'\n'+command.getUsage());
					return true;
				}
				target = args[2];
			}
			OfflinePlayer p = plugin.getServer().getOfflinePlayer(target);
			if(p == null || !p.hasPlayedBefore())
				try{p = plugin.getServer().getOfflinePlayer(UUID.fromString(target));}
				catch(IllegalArgumentException ex){}
			if(p == null || !p.hasPlayedBefore()){
				sender.sendMessage(ChatColor.RED+"Unknown player: "+target);
				return true;
			}
			if(amt == BigDecimal.ZERO){
				sender.sendMessage(ChatColor.RED+"Please use a non-zero number amount");
				return true;
			}
			if(economy.serverToPlayer(p.getUniqueId(), amt)){
				sender.sendMessage(ChatColor.GRAY+"Transferred "+ChatColor.YELLOW+amt.abs()+ChatColor.DARK_GREEN+'L'+
					ChatColor.GRAY+" from "+
					(amt.compareTo(BigDecimal.ZERO) == 1 ? "Server to "+p.getName() : p.getName()+" to Server"));
				if(!sender.getName().equals(p.getName()) && p.isOnline()){
					p.getPlayer().sendMessage(""+ChatColor.GRAY+ChatColor.BOLD+"Notice: "+
							ChatColor.YELLOW+amt.abs()+ChatColor.DARK_GREEN+'L'+
							ChatColor.GRAY+" was transferred "+
							(amt.compareTo(BigDecimal.ZERO) == 1 ? "to you from" : "from you to")+
							ChatColor.GOLD+" Server Bank");
				}
			}
			else sender.sendMessage(ChatColor.RED+"Unable to complete transfer");
		}
		else if(args[0].equals("add")){
			if(sender instanceof Player && !sender.hasPermission("eventials.serverbal.modify")){
				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command");
				return true;
			}
			BigDecimal amt;
			try{amt = new BigDecimal(args[1]);}
			catch(NumberFormatException ex){
				sender.sendMessage(ChatColor.RED+"Invalid numeric amount"+ChatColor.GRAY+'\n'+command.getUsage());
				return true;
			}
			if(amt == BigDecimal.ZERO){
				sender.sendMessage(ChatColor.RED+"Please use a non-zero number amount");
			}
			else if(economy.payServer(amt)){
				if(amt.compareTo(BigDecimal.ZERO) > 0)
					sender.sendMessage(ChatColor.GRAY+"Added "
							+ChatColor.YELLOW+amt+ChatColor.GRAY+" to Server's balance");
				else sender.sendMessage(ChatColor.GRAY+"Took "
							+ChatColor.YELLOW+amt.abs()+ChatColor.GRAY+" from Server's balance");
			}
			else sender.sendMessage(ChatColor.RED+"Unable to complete transaction");
		}
		else if(args[0].equals("take") || args[0].equals("charge") || args[0].equals("subtract")){
			args[0] = "add";
			args[1] = (args[1].charAt(0) == '-' ? args[1].substring(1) : '-'+args[1]);
			return onCommand(sender, command, label, args);
		}
		else{
			sender.sendMessage(ChatColor.RED+"Unknown arguments"+ChatColor.GRAY+'\n'+command.getUsage());
		}
		return true;
	}
}