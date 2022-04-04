package Eventials.economy.commands;

import org.bukkit.ChatColor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import Eventials.economy.ServerEconomy;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.hooks.EssEcoHook;
import net.evmodder.EvLib.extras.TextUtils;

public class CommandServerBal extends EvCommand{
	final ServerEconomy economy;
	final JavaPlugin plugin;
	final String curSymbol;

	public CommandServerBal(JavaPlugin pl, ServerEconomy eco, boolean enabled){
		super(pl, enabled);
		plugin = pl;
		economy = eco;
		curSymbol = TextUtils.translateAlternateColorCodes('&', pl.getConfig().getString("currency-symbol", "&2L"));
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
		if(args.length == 0) return null;
		args[0] = args[0].toLowerCase();
		final List<String> tabCompletes = new ArrayList<>();
		if(args.length == 1){
			if(sender.hasPermission("eventials.serverbal.transfer") && "send".startsWith(args[0]))
				tabCompletes.add("send");
			if(sender.hasPermission("eventials.serverbal.modify") && "add".startsWith(args[0]))
				tabCompletes.add("add");
			if(sender.hasPermission("eventials.serverbal.modify") && "charge".startsWith(args[0]))
				tabCompletes.add("charge");
			if(sender.hasPermission("eventials.serverbal.tax") && "tax".startsWith(args[0]))
				tabCompletes.add("tax");
			if(args[0].isEmpty()) tabCompletes.add("");//TODO: (for /serverbal view) necessary?
		}
		else if(args.length == 2){
			//if(args[0].equals("send")){//auto complete player names
			//if(args[0].equals("add")){//do nothing
			if(args[0].equals("charge") && sender.hasPermission("eventials.serverbal.modify")){
				String bal = ""+economy.getServerBal();
				if(bal.startsWith(args[1])) tabCompletes.add(bal);
			}
			else if(args[0].equals("tax") && sender.hasPermission("eventials.serverbal.tax")){
				args[1] = args[1].toLowerCase();
				for(Player p : plugin.getServer().getOnlinePlayers()){
					if(p.getName().toLowerCase().startsWith(args[1])
							|| p.getDisplayName().toLowerCase().startsWith(args[1])) tabCompletes.add(p.getName());
				}
				if("all".startsWith(args[1])) tabCompletes.add("all");
			}
		}
		else if(args.length == 3){
			if(args[0].equals("send") && sender.hasPermission("eventials.serverbal.transfer")){
				String bal = ""+economy.getServerBal();
				if(bal.startsWith(args[1])) tabCompletes.add(bal);
			}
		}
		return tabCompletes;
	}

	@SuppressWarnings("deprecation") @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/serverbal [send/add/charge/tax] [amt] [player]

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
				sender.sendMessage(ChatColor.GRAY+"Transferred "+ChatColor.YELLOW+amt.abs()
					+ChatColor.DARK_GREEN+curSymbol+ChatColor.GRAY+" from "+
					(amt.compareTo(BigDecimal.ZERO) == 1 ? "Server to "+p.getName() : p.getName()+" to Server"));
				if(!sender.getName().equals(p.getName()) && p.isOnline()){
					p.getPlayer().sendMessage(""+ChatColor.GRAY+ChatColor.BOLD+"Notice: "+
							ChatColor.YELLOW+amt.abs()+ChatColor.DARK_GREEN+curSymbol+
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
		else if(args[0].equals("tax")){
			if(sender instanceof Player && !sender.hasPermission("eventials.serverbal.tax")){
				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command");
				return true;
			}
			if(args.length == 2){
				sender.sendMessage(ChatColor.RED+"Too few arguments!"+ChatColor.GRAY+'\n'+command.getUsage());
				return true;
			}
			double percent;
			boolean percentFormat = args[2].contains("%");
			if(percentFormat) args[2] = args[2].replace("%", "");
			String target = args[1];
			try{percent = Double.parseDouble(args[2]);}
			catch(NumberFormatException ex){
				percentFormat = args[1].contains("%");
				if(percentFormat) args[1] = args[1].replace("%", "");
				try{percent = Double.parseDouble(args[1]);}
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
				if(target.toLowerCase().replace("@a", "all").replace("@", "").equals("all")) p = null;
				else{
					sender.sendMessage(ChatColor.RED+"Unknown player: "+target);
					return true;
				}
			}
			if(percent == 0) sender.sendMessage(ChatColor.RED+"Invalid tax rate");
			if(percentFormat) percent /= 100D;
			if(percent < -1D || percent > 1D){
				sender.sendMessage(ChatColor.RED+"Invalid tax rate; Please pick a number between [-1,1]");
			}
			if(p != null){
				int taxAmt = (int)(EssEcoHook.getBalance(p)*(percent > 0 ? percent : (1D/percent)-1D));
				if(economy.playerToServer(p.getUniqueId(), taxAmt)){
					sender.sendMessage(ChatColor.GRAY+"Transferred "+ChatColor.YELLOW+Math.abs(taxAmt)
						+ChatColor.DARK_GREEN+curSymbol+ChatColor.GRAY+" from "+
						(taxAmt < 0 ? "Server to "+p.getName() : p.getName()+" to Server"));
				}
				else{
					sender.sendMessage(ChatColor.RED+"Unable to complete taxation of "+p.getName()+
							" for amount: "+taxAmt);
				}
			}
			else{
				int numTaxed = 0;
				long amtTaxed = 0;
				for(OfflinePlayer p2 : plugin.getServer().getOfflinePlayers()){
					int taxAmt = (int)(EssEcoHook.getBalance(p2)*(percent > 0 ? percent : (1D/percent)-1D));
					if(economy.playerToServer(p2.getUniqueId(), taxAmt)){
						String taxMsg = new StringBuilder("").append(ChatColor.GRAY).append("Transferred ")
								.append(ChatColor.YELLOW).append(Math.abs(taxAmt))
								.append(ChatColor.DARK_GREEN).append(curSymbol)
								.append(ChatColor.GRAY).append(" from ")
								.append(taxAmt < 0 ? "Server to "+p2.getName() : p2.getName()+" to Server")
								.toString();
						++numTaxed;
						amtTaxed += taxAmt;
						plugin.getLogger().info(taxMsg);
						if(sender instanceof Player) sender.sendMessage(taxMsg);
					}
				}
				ChatColor cc = numTaxed > 0 ? ChatColor.GREEN : ChatColor.RED;
				String taxMsg = new StringBuilder("").append(cc).append("Successfully taxed ")
						.append(ChatColor.YELLOW).append(amtTaxed).append(ChatColor.DARK_GREEN).append(curSymbol)
						.append(cc).append(" from ").append(ChatColor.GRAY).append(numTaxed).append(cc).append(" players")
						.toString();
				plugin.getLogger().info(taxMsg);
				if(sender instanceof Player) sender.sendMessage(taxMsg);
			}
		}
		else{
			sender.sendMessage(ChatColor.RED+"Unknown arguments"+ChatColor.GRAY+'\n'+command.getUsage());
		}
		return true;
	}
}