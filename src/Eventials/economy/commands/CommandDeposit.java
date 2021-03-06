package Eventials.economy.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import Eventials.economy.EvEconomy;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.hooks.EssEcoHook;

public class CommandDeposit extends EvCommand{
	final EvEconomy economy;
	final String curSymbol;

	public CommandDeposit(EvPlugin pl, EvEconomy eco, boolean enabled){
		super(pl);
		if(!enabled) pl.getCommand("Deposit").setExecutor(new CommandExecutor(){
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
				sender.sendMessage(ChatColor.RED+"This command is currently unavailable");
				return true;
			}
		});
		economy = eco;
		curSymbol = TextUtils.translateAlternateColorCodes('&', pl.getConfig().getString("currency-symbol", "&2L"));
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@SuppressWarnings("deprecation") @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/deposit <amount>
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
			return true;
		}
		int deposit;
		
		if(args.length < 1){
			deposit = economy.getCurrency().getMaxStackSize()*4*9;
//			sender.sendMessage(ChatColor.RED+"Too few arguments!"+ChatColor.GRAY+"\n"+command.getUsage());
//			return true;
		}
		else{
			try{deposit = Integer.parseInt(args[0]);}
			catch(NumberFormatException ex){
				sender.sendMessage(ChatColor.RED+"Invalid deposit amount"+ChatColor.GRAY+'\n'+command.getUsage());
				return true;
			}
		}

		Player p = (Player) sender;
		int deposited = 0;
		ItemStack[] contents = p.getInventory().getContents();
		for(ItemStack item : contents){
			if(item != null && item.getType() == economy.getCurrency()){
				if(item.getAmount() <= deposit-deposited){
					deposited += item.getAmount();
					item.setType(Material.AIR);
				}
				else{
					item.setAmount(item.getAmount()-(deposit-deposited));
					deposited = deposit;
				}
				if(deposited == deposit) break;
			}
		}
		if(EssEcoHook.giveMoney(p, deposited)){
			p.getInventory().setContents(contents);
			p.sendMessage(new StringBuilder().append(ChatColor.GREEN).append("You deposited ")
					.append(ChatColor.YELLOW).append(deposited).append(curSymbol).toString());
			economy.addGlobalBal(deposited);
		}
		else sender.sendMessage(ChatColor.RED+"Failed to make deposit!");
		return true;
	}
}