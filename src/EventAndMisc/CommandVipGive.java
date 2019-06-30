package EventAndMisc;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import Eventials.Eventials;

public class CommandVipGive implements CommandExecutor{
	
	@SuppressWarnings("deprecation") @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(args.length < 1) return false;
		Eventials pl = Eventials.getPlugin();
		OfflinePlayer p;
		try{p = pl.getServer().getOfflinePlayer(UUID.fromString(args[0]));}
		catch(IllegalArgumentException ex){p = pl.getServer().getOfflinePlayer(args[0]);}
		
		if(p == null || p.hasPlayedBefore() == false){
			sender.sendMessage("Invalid player uuid/name");
			return false;
		}
		
		for(String worldName : new String[]{"VictoryHills", "Creative", "Events"}){
			File groups = new File("./plugins/GroupManager/worlds/"+worldName.toLowerCase()+"/groups.yml");
			if(groups.exists() == false) continue;
			/*File users = new File("./plugins/GroupManager/worlds/"+worldName.toLowerCase()+"/users.yml");
			
			if(groups.exists() && users.exists())
			try{
				if(WorldDataHolder.load(worldName, groups, users).getUser(args[0]).getGroupName().equalsIgnoreCase("Default")){
					pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), "manuadd "+p.getName()+" Vip "+worldName);
					pl.getLogger().info("Added Vip rank to "+p.getName()+" for world: "+worldName);
					continue;
				}
			}catch(FileNotFoundException e){}catch(IOException e){}*/
			// gets here if there is an exception or if the player is not in he group "Default"
			pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), "manuaddsub "+p.getName()+" Vip");
			pl.getLogger().info("Added Vip subgroup to "+p.getName()+" to for world: "+worldName);
		}
		
		//------------------------------------------------- Give Vip Items -------------------------------------------------
		if(p.isOnline()){
			Player onlineP = p.getPlayer();
			if(onlineP.getGameMode() != GameMode.CREATIVE){
				giveVipItems(onlineP.getInventory());
			}
			else giveVipItems(onlineP.getEnderChest());
		}
		else{
			Block block = pl.getServer().getWorlds().get(0).getBlockAt(0, 200, 0);
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
			giveVipItems(chest.getInventory());
		}
		return true;
	}
	
	private void giveVipItems(Inventory inv){
		inv.addItem(new ItemStack(Material.COW_SPAWN_EGG, 2));
		inv.addItem(new ItemStack(Material.SHEEP_SPAWN_EGG, 2));
		inv.addItem(new ItemStack(Material.PIG_SPAWN_EGG, 2));
		inv.addItem(new ItemStack(Material.HORSE_SPAWN_EGG, 2));
		inv.addItem(new ItemStack(Material.TURTLE_SPAWN_EGG, 2));
		inv.addItem(new ItemStack(Material.RABBIT_SPAWN_EGG, 2));
		ItemStack thx = new ItemStack(Material.FEATHER);
		ItemMeta meta = thx.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD+"Thank you for donating!");
		meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
		thx.setItemMeta(meta);
		inv.addItem(thx);
	}
}