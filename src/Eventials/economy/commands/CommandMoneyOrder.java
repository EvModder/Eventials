package Eventials.economy.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import Eventials.economy.EvEconomy;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.hooks.EssEcoHook;
import org.bukkit.ChatColor;

public class CommandMoneyOrder extends EvCommand implements Listener{
	final EvEconomy economy;
	final String curSymbol;
	final int MAX_MO, MIN_MO, TAX_MO;

	public CommandMoneyOrder(EvPlugin pl, EvEconomy eco, boolean enabled){
		super(pl, enabled);
		if(enabled) pl.getServer().getPluginManager().registerEvents(this, pl);
		economy = eco;
		curSymbol = TextUtils.translateAlternateColorCodes('&', pl.getConfig().getString("currency-symbol", "&2L"));
		MAX_MO = pl.getConfig().getInt("moneyorder-max");
		MIN_MO = pl.getConfig().getInt("moneyorder-min");
		TAX_MO = pl.getConfig().getInt("moneyorder-tax-percent");
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String Label, String[] args){
		if(args.length == 1 && sender instanceof Player){
			final List<String> tabCompletes = new ArrayList<>();
			args[0] = args[0].toLowerCase();
			if((""+MIN_MO).startsWith(args[0])) tabCompletes.add(""+MIN_MO);
			int bal = (int) EssEcoHook.getBalance((Player)sender);
			if(MIN_MO < bal && bal < MAX_MO) if((""+bal).startsWith(args[0])) tabCompletes.add(""+bal);
			if((""+MAX_MO).startsWith(args[0])) tabCompletes.add(""+MAX_MO);
			return tabCompletes;
		}
		return null;
	}


	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		Player p = (Player) sender;

		if(args.length != 1){
			double amount = EvEconomy.getMoneyOrderValue(p.getInventory().getItemInMainHand());
			if(args.length == 0 && amount != 0){
				if(!economy.serverToPlayer(p.getUniqueId(), amount)){
					p.sendMessage(ChatColor.RED+"Error in economy system--the server appears to be out of money!");
				}
				else{
					if(p.getInventory().getItemInMainHand().getAmount() == 1) p.getInventory().setItemInMainHand(null);
					else p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount()-1);
					p.sendMessage(ChatColor.GREEN+"Cashed in a MO for: "+curSymbol+amount);
				}
			}
			else{
				p.sendMessage(ChatColor.RED+"Too few/many arguments");
				return false;
			}
		}
		else{
			try{
				int amount = Integer.parseInt(args[0]);
				if(amount < MIN_MO || amount > MAX_MO){
					p.sendMessage(ChatColor.RED+"Invalid money amount\n"+ChatColor.GRAY
							+" (must be integer between "+MIN_MO+" and "+MAX_MO+", "+TAX_MO+"% tax included)");
					return false;
				}
				if(!economy.playerToServer(p.getUniqueId(), amount*(1 + TAX_MO/100.0))){
					p.sendMessage(ChatColor.RED+"Error in economy system or not enough money!!");
					return true;
				}
				ItemStack mo = new ItemStack(Material.PAPER);
				ItemMeta meta = mo.getItemMeta();
				meta.setDisplayName(ChatColor.GREEN+"Money Order");
				meta.setLore(Arrays.asList(ChatColor.YELLOW+"Stored balance: "+ChatColor.AQUA+"$"+amount));
				mo.setItemMeta(meta);
				if(p.getInventory().addItem(mo).isEmpty() == false) p.getWorld().dropItem(p.getLocation(), mo);
				p.sendMessage(ChatColor.GREEN+"Withdrew a MO of: "+curSymbol+ChatColor.AQUA+((double)amount)
								+" ("+TAX_MO+"% tax amounting to "+curSymbol+ChatColor.YELLOW
								+(amount*(TAX_MO/100.0))+ChatColor.GREEN+")");
			}
			catch(NumberFormatException ex){
				p.sendMessage(ChatColor.RED+"Invalid money amount (must be a whole number)");
				return false;
			}
		}
		return true;
	}

	@EventHandler
	public void onPlayerInteractBlock(PlayerInteractEvent evt){
		if(EvEconomy.getMoneyOrderValue(evt.getPlayer().getInventory().getItemInMainHand()) != 0){
			double amount = EvEconomy.getMoneyOrderValue(evt.getItem());
			EvEconomy eco = EvEconomy.getEconomy();
			eco.serverToPlayer(evt.getPlayer().getUniqueId(), amount);

			int numMOs = evt.getPlayer().getInventory().getItemInMainHand().getAmount();
			if(numMOs == 1) evt.getPlayer().getInventory().setItemInMainHand(null);
			else evt.getPlayer().getInventory().getItemInMainHand().setAmount(numMOs-1);

			evt.getPlayer().sendMessage(ChatColor.GREEN+"Cashed in a MO for: "+eco+amount);
		}
	}
}