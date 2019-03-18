package Eventials.splitworlds;

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import EvLib.CommandBase2;
import EvLib.EvPlugin;

public class CommandInvsee extends CommandBase2{
	private EvPlugin pl;
	private SplitWorlds splitWorlds;

	public CommandInvsee(EvPlugin p, SplitWorlds sw){
		super(p, true);
		pl = p;
		splitWorlds = sw;
	}

	@Override @SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/invsee [world] [player]
		//----- Argument Parsing ---------------------------------------------------------------------------//
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		if(args.length > 2){
			sender.sendMessage(ChatColor.RED+"Too many arguments");
			return false;
		}
		Player player = (Player)sender;
		String targetWorld = player.getWorld().getName();
		OfflinePlayer targetPlayer = player;
		if(args.length == 0){/* defaults */}
		else if(args.length == 1){
			World w = pl.getServer().getWorld(args[0]);
			if(w != null) targetWorld = w.getName();
			else{
				targetPlayer = pl.getServer().getOfflinePlayer(args[0]);
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore())
					targetPlayer = pl.getServer().getOfflinePlayer(UUID.fromString(args[0]));
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore()){
					if(sender.hasPermission("eventials.invsee.others"))
						sender.sendMessage(ChatColor.RED+"Unable to find world or player matching: "+args[0]);
					else sender.sendMessage(ChatColor.RED+"Unable to find world: "+args[0]);
					return false;
				}
				else if(sender.hasPermission("eventials.invsee.others") == false){
					sender.sendMessage(ChatColor.RED+"You do not have permission to view others' inventories ");
					return true;
				}
				if(targetPlayer.isOnline()) targetWorld = targetPlayer.getPlayer().getWorld().getName();
			}
		}
		else{
			World w = pl.getServer().getWorld(args[0]);
			if(w != null){
				targetWorld = w.getName();
				targetPlayer = pl.getServer().getOfflinePlayer(args[1]);
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore())
					targetPlayer = pl.getServer().getOfflinePlayer(UUID.fromString(args[1]));
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore()){
					if(sender.hasPermission("eventials.invsee.others"))
						sender.sendMessage(ChatColor.RED+"Unable to find player: "+args[1]);
					else sender.sendMessage(ChatColor.RED+"Too many arguments");
					return false;
				}
				else if(sender.hasPermission("eventials.invsee.others") == false){
					sender.sendMessage(ChatColor.RED+"You do not have permission to view others' inventories ");
					return true;
				}
			}
			else{
				targetPlayer = pl.getServer().getOfflinePlayer(args[0]);
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore())
					targetPlayer= pl.getServer().getOfflinePlayer(UUID.fromString(args[0]));
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore()){
					if(sender.hasPermission("eventials.invsee.others"))
						sender.sendMessage(ChatColor.RED+"Unable to find world or player matching: "+args[0]);
					//else sender.sendMessage(ChatColor.RED+"Unable to find world: "+args[0]);
					else sender.sendMessage(ChatColor.RED+"Too many arguments");
					return false;
				}
				else if(sender.hasPermission("eventials.invsee.others") == false){
					sender.sendMessage(ChatColor.RED+"You do not have permission to view others' inventories ");
					return true;
				}
				w = pl.getServer().getWorld(args[1]);
				if(w == null){
					sender.sendMessage(ChatColor.RED+"Could not find world '"+args[1]+"' for inventory:"+args[0]);
					return false;
				}
				targetWorld = w.getName();
			}
		}
		//----- Argument Parsing (end) ---------------------------------------------------------------------//

		//TODO: permissions for OfflinePlayer
		//if(targetPlayer.hasPermission("eventials.inventory.universal")) targetWorld = splitWorlds.DEFAULT_WORLD;

		if(sender.getName().equals(targetPlayer.getName()) && (
				splitWorlds.inSharedInvGroup(targetWorld, player.getWorld().getName()) ||
				sender.hasPermission("eventials.inventory.universal"))){
			sender.sendMessage(ChatColor.RED+"Your "+targetWorld+" inventory is already open");
			return true;
		}

		if(targetPlayer.isOnline() &&
				splitWorlds.inSharedInvGroup(targetWorld, targetPlayer.getPlayer().getWorld().getName())){
			player.openInventory(targetPlayer.getPlayer().getInventory());
			return true;
		}

		// Save my current profile
		if(!splitWorlds.forceSaveProfile(player)){
			sender.sendMessage(ChatColor.RED+"Encounter error while saving your current inventory!");
			return true;
		}

		// Load the target's profile data
		if(!splitWorlds.loadProfile(player, targetPlayer.getUniqueId(), targetWorld, true)){
			sender.sendMessage("Unable to find data files for "+targetPlayer.getName()+" in world "+targetWorld);
			return true;
		}
		ItemStack[] contents = player.getInventory().getContents();

		// Reload my profile
		splitWorlds.forceLoadProfile(player);

		// Create and display an inventory using the ItemStack[]
		final String invName = "> "+targetPlayer.getName()+" - "+splitWorlds.getInvGroup(targetWorld);
		Inventory targetInv = pl.getServer().createInventory(player, InventoryType.PLAYER, invName);
		targetInv.setContents(contents);
		player.openInventory(targetInv);
		pl.getLogger().info(player.getName()+" viewing inventory of: "+targetPlayer.getName());

		// Listener to write back to disk the inventory being viewed once it is closed
		final String fTargetWorld = targetWorld;
		final OfflinePlayer fTargetPlayer = targetPlayer;
		pl.getServer().getPluginManager().registerEvents(new Listener(){
			final UUID snooper = player.getUniqueId();
			@EventHandler public void inventoryCloseEvent(InventoryCloseEvent evt){
				if(!evt.getPlayer().getUniqueId().equals(snooper) ||
						evt.getInventory().getType() != InventoryType.PLAYER ||
						!invName.equals(evt.getInventory().getTitle())) return;

				pl.getLogger().info("Updating inventory: "+fTargetWorld+" > "+fTargetPlayer.getName());

				splitWorlds.forceSaveProfile(player);// Any changes I made in my own inv
				splitWorlds.loadProfile(player, fTargetPlayer.getUniqueId(), fTargetWorld, true);
				player.getInventory().setContents(evt.getInventory().getContents());
				splitWorlds.saveProfile(player, fTargetPlayer.getUniqueId(), fTargetWorld, true);
				splitWorlds.forceLoadProfile(player);

				HandlerList.unregisterAll(this);
			}
		}, pl);
		return true;
	}
}