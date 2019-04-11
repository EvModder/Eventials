package Eventials.economy.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import EvLib.CommandBase2;
import EvLib.EvPlugin;
import EvLib.UsefulUtils;
import EvLib.VaultHook;
import Eventials.economy.Economy;
import Extras.Text;

public class CommandWithdraw extends CommandBase2{
	final Economy economy;
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

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String Label, String[] args){
		if(args.length == 1 && sender instanceof Player){
			int bal = (int) VaultHook.getBalance((Player)sender);
			if(bal > 0){
				final List<String> tabCompletes = new ArrayList<String>();
				args[0] = args[0].toLowerCase();
				if("64".startsWith(args[0])) tabCompletes.add("64");
				if((""+bal).startsWith(args[0])) tabCompletes.add(""+bal);
				String spaceAvail = ""+UsefulUtils.maxCapacity(((Player)sender).getInventory(), economy.getCurrency());
				if(spaceAvail.startsWith(args[0])) tabCompletes.add(spaceAvail);
				return tabCompletes;
			}
		}
		return null;
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
			withdraw = (int) VaultHook.getBalance(p);
		}

		if(withdraw > 0 && VaultHook.chargeFee(p, withdraw)){
			int MAX_STACK_SIZE = economy.getCurrency().getMaxStackSize();

			ItemStack[] items = new ItemStack[withdraw/MAX_STACK_SIZE+1];
			items[0] = new ItemStack(economy.getCurrency(), MAX_STACK_SIZE);

			for(int i=1; i<items.length; ++i) items[i] = items[0].clone();
			items[0].setAmount(withdraw % MAX_STACK_SIZE);

			for(ItemStack leftover : p.getInventory().addItem(items).values())
				if(leftover != null && leftover.getType() != Material.AIR)
					p.getWorld().dropItem(p.getLocation(), leftover);

			p.sendMessage(new StringBuilder().append(ChatColor.GREEN).append("You withdrew ")
					.append(ChatColor.YELLOW).append(withdraw).append(curSymbol).toString());
			economy.addGlobalBal(-withdraw);
		}
		else sender.sendMessage(ChatColor.RED+"Failed to make withdrawal!");
		return true;
	}
}