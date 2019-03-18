package Eventials.economy.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import EvLib.CommandBase2;
import EvLib.EvPlugin;
import EvLib.VaultHook;
import Eventials.economy.Economy;
import Extras.Text;

public class CommandWithdraw extends CommandBase2{
	Economy economy;
	final String curSymbol;

	public CommandWithdraw(EvPlugin pl, Economy eco, boolean enabled){
		super(pl);
		if(!enabled) pl.getCommand("Withdraw").setExecutor(new CommandExecutor(){
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
				sender.sendMessage(ChatColor.RED+"This command is currently unavailable");
				return true;
			}
		});
		economy = eco;
		curSymbol = Text.translateAlternateColorCodes('&', pl.getConfig().getString("currency-symbol", "&2L"));
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/withdraw <amount>
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
			return true;
		}
		int withdraw;
		
		if(args.length < 1){
			withdraw = economy.getCurrency().getMaxStackSize();
//			sender.sendMessage(ChatColor.RED+"Too few arguments!"+ChatColor.GRAY+'\n'+command.getUsage());
//			return true;
		}
		else{
			try{withdraw = Integer.parseInt(args[0]);}
			catch(NumberFormatException ex){
				sender.sendMessage(ChatColor.RED+"Invalid withdrawal amount"+ChatColor.GRAY+'\n'+command.getUsage());
				return true;
			}
		}

		Player p = (Player) sender;
		if(!VaultHook.hasAtLeast(p, withdraw)){
			try{withdraw = (int) VaultHook.getBalance(p);}
			catch(Exception e){sender.sendMessage(ChatColor.RED+"Failed to make withdrawal!"); return true;}
		}

		if(withdraw > 0 && VaultHook.chargeFee(p, withdraw)){
			ItemStack item = new ItemStack(economy.getCurrency(), economy.getCurrency().getMaxStackSize());

			ItemStack[] items = new ItemStack[withdraw/64 +1];
			int i=0;
			for(; i<withdraw/64; ++i) items[i] = item.clone();
			item.setAmount(withdraw % economy.getCurrency().getMaxStackSize());
			items[i] = item;

			for(ItemStack leftover : p.getInventory().addItem(items).values())
				p.getWorld().dropItem(p.getLocation(), leftover);

			p.sendMessage(new StringBuilder().append(ChatColor.GREEN).append("You withdrew ")
					.append(ChatColor.YELLOW).append(withdraw).append(curSymbol).toString());
			economy.addGlobalBal(-withdraw);
		}
		else sender.sendMessage(ChatColor.RED+"Failed to make withdrawal!");
		return true;
	}
}