package Eventials.splitworlds;

import java.util.List;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import Eventials.splitworlds.SplitWorldUtils.PlayerState;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;

public class CommandEnderchest extends EvCommand{
	private EvPlugin pl;

	public CommandEnderchest(EvPlugin p){
		super(p, true);
		pl = p;
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override @SuppressWarnings( "deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/echest [world] [player]
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
				Player onlineTarget = pl.getServer().getPlayer(args[0]);
				targetPlayer = onlineTarget;
				if(targetPlayer == null) targetPlayer = pl.getServer().getOfflinePlayer(args[0]);
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore()){
					try{targetPlayer = pl.getServer().getOfflinePlayer(UUID.fromString(args[0]));}
					catch(IllegalArgumentException ex){}
				}
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore()){
					if(sender.hasPermission("eventials.echest.others")){
						if(onlineTarget == null || !SplitWorlds.inSharedInvGroup(
								onlineTarget.getWorld().getName(), player.getWorld().getName()))
							sender.sendMessage(ChatColor.RED+"Unable to find world or player matching: "+args[0]);
						else player.openInventory(targetPlayer.getPlayer().getEnderChest());
					}
					else sender.sendMessage(ChatColor.RED+"Unable to find world: "+args[0]);
					return false;
				}
				else if(sender.hasPermission("eventials.echest.others") == false){
					sender.sendMessage(ChatColor.RED+"You do not have permission to view others' enderchests ");
					return true;
				}
				if(targetPlayer.isOnline()) targetWorld = targetPlayer.getPlayer().getWorld().getName();
			}
		}
		else{
			World w = pl.getServer().getWorld(args[0]);
			if(w != null){
				targetWorld = w.getName();
				targetPlayer = pl.getServer().getPlayer(args[1]);
				if(targetPlayer == null) targetPlayer = pl.getServer().getOfflinePlayer(args[1]);
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore())
					targetPlayer = pl.getServer().getOfflinePlayer(UUID.fromString(args[1]));
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore()){
					if(sender.hasPermission("eventials.echest.others"))
						sender.sendMessage(ChatColor.RED+"Unable to find player: "+args[1]);
					else sender.sendMessage(ChatColor.RED+"Too many arguments");
					return false;
				}
				else if(sender.hasPermission("eventials.echest.others") == false){
					sender.sendMessage(ChatColor.RED+"You do not have permission to view others' enderchests ");
					return true;
				}
			}
			else{
				targetPlayer = pl.getServer().getOfflinePlayer(args[0]);
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore())
					targetPlayer= pl.getServer().getOfflinePlayer(UUID.fromString(args[0]));
				if(targetPlayer == null || !targetPlayer.hasPlayedBefore()){
					if(sender.hasPermission("eventials.echest.others"))
						sender.sendMessage(ChatColor.RED+"Unable to find world or player matching: "+args[0]);
					//else sender.sendMessage(ChatColor.RED+"Unable to find world: "+args[0]);
					else sender.sendMessage(ChatColor.RED+"Too many arguments");
					return false;
				}
				else if(sender.hasPermission("eventials.echest.others") == false){
					sender.sendMessage(ChatColor.RED+"You do not have permission to view others' enderchests ");
					return true;
				}
				w = pl.getServer().getWorld(args[1]);
				if(w == null){
					sender.sendMessage(ChatColor.RED+"Could not find world '"+args[1]+"' for enderchest:"+args[0]);
					return false;
				}
				targetWorld = w.getName();
			}
		}
		//----- Argument Parsing (end) ---------------------------------------------------------------------//

		//TODO: permissions for OfflinePlayer
		//if(targetPlayer.hasPermission("eventials.inventory.universal")) targetWorld = splitWorlds.DEFAULT_WORLD;

		if(sender.getName().equals(targetPlayer.getName()) && (
				SplitWorlds.inSharedInvGroup(targetWorld, player.getWorld().getName()) ||
				sender.hasPermission("eventials.inventory.universal"))){
			player.openInventory(((Player)sender).getEnderChest());
			return true;
		}

		if(targetPlayer.isOnline() &&
				SplitWorlds.inSharedInvGroup(targetWorld, targetPlayer.getPlayer().getWorld().getName())){
			player.openInventory(targetPlayer.getPlayer().getEnderChest());
			return true;
		}

		// Save my current profile
		if(!SplitWorlds.saveCurrentProfile(player)){
			sender.sendMessage(ChatColor.RED+"Error occurred while saving your current inventory!");
			return true;
		}

		// Load the target's profile data
		PlayerState myState = SplitWorldUtils.getPlayerState(player);
		if(!SplitWorlds.loadProfile(player, targetPlayer.getUniqueId(), targetWorld, true, true,
				!player.getUniqueId().equals(targetPlayer.getUniqueId()))){
			sender.sendMessage("Unable to find data files for "+targetPlayer.getName()+" in world "+targetWorld);
			return true;
		}
		ItemStack[] contents = player.getEnderChest().getContents();

		// Reload my profile
		PlayerState targetState = SplitWorldUtils.getPlayerState(player);
		SplitWorlds.loadCurrentProfile(player);
		SplitWorldUtils.loadPlayerState(player, myState);

		// Create and display an inventory using the enderchest ItemStack[]
		final String invName = "> "+targetPlayer.getName()+" - "+SplitWorlds.getInvGroup(targetWorld);
		Inventory targetInv = pl.getServer().createInventory(player, InventoryType.ENDER_CHEST, invName);
		targetInv.setContents(contents);
		player.openInventory(targetInv);
		pl.getLogger().info(player.getName()+" viewing enderchest of: "+targetPlayer.getName());

		// Listener to write back to disk the inventory being viewed once it is closed
		final String fTargetWorld = targetWorld;
		final OfflinePlayer fTargetPlayer = targetPlayer;
		pl.getServer().getPluginManager().registerEvents(new Listener(){
			final UUID snooper = player.getUniqueId();
			@EventHandler public void inventoryCloseEvent(InventoryCloseEvent evt){
				if(!evt.getPlayer().getUniqueId().equals(snooper) ||
						evt.getInventory().getType() != InventoryType.ENDER_CHEST/*TODO: broke in 1.14 ||
						!invName.equals(evt.getInventory().getTitle())*/) return;

				pl.getLogger().info("Updating inventory: "+fTargetWorld+" > "+fTargetPlayer.getName());

				SplitWorlds.saveCurrentProfile(player);// Any changes I made in my own inv
				SplitWorlds.loadProfile(player, fTargetPlayer.getUniqueId(), fTargetWorld, true, true, !player.getUniqueId().equals(fTargetPlayer.getUniqueId()));
				player.getEnderChest().setContents(evt.getInventory().getContents());
				SplitWorldUtils.loadPlayerState(player, targetState);
				SplitWorlds.saveProfile(player, fTargetPlayer.getUniqueId(), fTargetWorld, true, true, true);
				SplitWorlds.loadCurrentProfile(player);
				SplitWorldUtils.loadPlayerState(player, myState);
				HandlerList.unregisterAll(this);
			}
			// Listener in case they log on while their echest is being edited
			@EventHandler public void onPlayerJoin(PlayerJoinEvent evt){
				if(!evt.getPlayer().getUniqueId().equals(fTargetPlayer.getUniqueId()) ||
						!SplitWorlds.inSharedInvGroup(evt.getPlayer().getWorld().getName(), fTargetWorld)) return;

				//TODO: handle player teleporting to a shared inv world same as logging in on it
				HandlerList.unregisterAll(this);
				evt.getPlayer().getEnderChest().setContents(player.getOpenInventory().getTopInventory().getContents());
				player.closeInventory();
				player.openInventory(evt.getPlayer().getEnderChest());
			}
		}, pl);
		return true;
	}
}