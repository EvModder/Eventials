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
import Eventials.economy.EvEconomy;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.EvUtils;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.hooks.EssEcoHook;

public class CommandWithdraw extends EvCommand{
	final EvEconomy economy;
	final String curSymbol;

	public CommandWithdraw(EvPlugin pl, EvEconomy eco, boolean enabled){
		super(pl);
		if(!enabled) pl.getCommand("withdraw").setExecutor(new CommandExecutor(){
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
				sender.sendMessage(ChatColor.RED+"This command is currently unavailable");
				return true;
			}
		});
		economy = eco;
		curSymbol = TextUtils.translateAlternateColorCodes('&', pl.getConfig().getString("currency-symbol", "&2L"));
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
		if(args.length == 1 && sender instanceof Player && label.equalsIgnoreCase(cmd.getName())){
			int bal = (int) EssEcoHook.getBalance((Player)sender);
			if(bal > 0){
				final List<String> tabCompletes = new ArrayList<String>();
				args[0] = args[0].toLowerCase();
				if(bal >= 64 && "64".startsWith(args[0])) tabCompletes.add("64");
				if((""+bal).startsWith(args[0])) tabCompletes.add(""+bal);
				int spaceAvail = EvUtils.maxCapacity(((Player)sender).getInventory(), economy.getCurrency());
				if(bal >= spaceAvail && (""+spaceAvail).startsWith(args[0])) tabCompletes.add(""+spaceAvail);
				return tabCompletes;
			}
		}
		return null;
	}

	@Override @SuppressWarnings("deprecation")
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
		if(!EssEcoHook.hasAtLeast(p, withdraw)){
			withdraw = (int) EssEcoHook.getBalance(p);
		}

		if(withdraw > 0 && EssEcoHook.chargeFee(p, withdraw)){
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