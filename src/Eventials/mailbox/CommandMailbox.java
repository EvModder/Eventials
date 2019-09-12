package Eventials.mailbox;

import java.io.File;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
import Eventials.mailbox.MailboxFetcher.MailListener;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;

public class CommandMailbox extends EvCommand implements MailListener{
	final EvPlugin plugin;
	final MailboxFetcher mailFetcher;

	public CommandMailbox(EvPlugin pl, MailboxFetcher mailboxHook){
		super(pl, true);
		plugin = pl;
		mailFetcher = mailboxHook;
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override @SuppressWarnings({ "deprecation", "static-access" })
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/mailbox [player]
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		if(args.length > 1 || (args.length == 1 && !sender.hasPermission("eventials.mailbox.others"))){
			sender.sendMessage(ChatColor.RED+"Too many arguments");
			return false;
		}
		Player player = (Player)sender;
		OfflinePlayer targetPlayer = player;
		if(args.length == 1){
			targetPlayer = plugin.getServer().getPlayer(args[0]);
			if(targetPlayer == null) targetPlayer = plugin.getServer().getOfflinePlayer(args[0]);
			if(targetPlayer == null || !targetPlayer.hasPlayedBefore()){
				try{targetPlayer = plugin.getServer().getOfflinePlayer(UUID.fromString(args[0]));}
				catch(IllegalArgumentException ex){}
			}
			if(targetPlayer == null || !targetPlayer.hasPlayedBefore()){
				sender.sendMessage(ChatColor.RED+"Unable to find player: "+args[0]);
				return false;
			}
		}

		sender.sendMessage(ChatColor.GOLD+"Please wait, loading mailbox...");
		mailFetcher.loadMailbox(targetPlayer.getUniqueId(), this, true);
	}

	@Override public void playerMailboxLoaded(UUID playerUUID, File mailbox, String message){
		// TODO Auto-generated method stub
		// Save my current profile
		if(!splitWorlds.saveCurrentProfile(player)){
			sender.sendMessage(ChatColor.RED+"Encounter error while saving your current inventory!");
			return true;
		}
		GameMode gm = player.getGameMode();

		// Load the target's profile data
		if(!splitWorlds.loadProfile(player, targetPlayer.getUniqueId(), targetWorld, true, false)){
			sender.sendMessage("Unable to find data files for "+targetPlayer.getName()+" in world "+targetWorld);
			return true;
		}
		ItemStack[] contents = player.getInventory().getContents();

		// Reload my profile
		splitWorlds.loadCurrentProfile(player);
		player.setGameMode(gm); // In case I'm in creative and they're not and I don't want to fall out of the sky

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
						evt.getInventory().getType() != InventoryType.PLAYER /*||
						broke in 1.14: !invName.equals(evt.getInventory().getTitle())*/) return;

				pl.getLogger().info("Updating inventory: "+fTargetWorld+" > "+fTargetPlayer.getName());

				splitWorlds.saveCurrentProfile(player);// Any changes I made in my own inv
				splitWorlds.loadProfile(player, fTargetPlayer.getUniqueId(), fTargetWorld, true, false);
				player.getInventory().setContents(evt.getInventory().getContents());
				splitWorlds.saveProfile(player, fTargetPlayer.getUniqueId(), fTargetWorld, true, false);
				splitWorlds.loadCurrentProfile(player);

				HandlerList.unregisterAll(this);
			}
		}, pl);
		return true;
	}

	@Override public void playerMailboxSaved(UUID playerUUID, String message){
		// TODO Auto-generated method stub
		
	}
}