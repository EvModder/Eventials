package Eventials.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import Eventials.Eventials;
import net.evmodder.EvLib.CommandBase;
import org.bukkit.ChatColor;

public class CommandVipGive extends CommandBase {
	Eventials plugin;

	public CommandVipGive(Eventials pl) {
		super(pl);
		plugin = pl;
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@SuppressWarnings("deprecation") @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(args.length == 0) return false;

		OfflinePlayer p;
		try{p = plugin.getServer().getOfflinePlayer(UUID.fromString(args[0]));}
		catch(IllegalArgumentException ex){p = plugin.getServer().getOfflinePlayer(args[0]);}
		
		if(p == null || !p.hasPlayedBefore()){
			sender.sendMessage("Invalid player name/uuid");
			return true;
		}

		Eventials plugin = Eventials.getPlugin();
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
				"permissions player addgroup "+p.getUniqueId()+" vip");

		Inventory inv;
		if(p.isOnline()) inv = p.getPlayer().getInventory();
		else{
			Block block = plugin.getServer().getWorlds().get(0).getBlockAt(0, 200, 0);
			if(block.getType() != Material.CHEST) block.setType(Material.CHEST);
			Chest chest = (Chest) block.getState();

			// "nametag" for whose items these are
			ItemStack donationslip = new ItemStack(Material.PAPER);
			ItemMeta meta = donationslip.getItemMeta();
			meta.setDisplayName(ChatColor.GRAY+args[0]);
			ArrayList<String> lore = new ArrayList<String>(); lore.add(ChatColor.BLUE+"> 32 diamonds");
			meta.setLore(lore);
			donationslip.setItemMeta(meta);
			
			chest.getInventory().addItem(donationslip);
			inv = chest.getInventory();
		}
		inv.addItem(new ItemStack(Material.COW_SPAWN_EGG, 2));
		inv.addItem(new ItemStack(Material.SHEEP_SPAWN_EGG, 2));
		inv.addItem(new ItemStack(Material.PIG_SPAWN_EGG, 2));
		inv.addItem(new ItemStack(Material.HORSE_SPAWN_EGG, 2));
		inv.addItem(new ItemStack(Material.TURTLE_SPAWN_EGG, 2));
		inv.addItem(new ItemStack(Material.RABBIT_SPAWN_EGG, 2));
		ItemStack thx = new ItemStack(Material.FEATHER);
		ItemMeta meta = thx.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD+"Thank you for helping the server!");
		meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
		thx.setItemMeta(meta);
		inv.addItem(thx);
		return true;
	}
}