package Eventials.mailbox;

import java.io.File;
import java.util.HashMap;
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
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.mailbox.MailboxClient.MailListener;
import Eventials.splitworlds.SplitWorlds;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;

public class CommandMailbox extends EvCommand implements MailListener{
	final EvPlugin plugin;
	final MailboxClient mailFetcher;
	final HashMap<UUID, UUID> mailReaders;
	final HashMap<UUID, ItemStack[]> currentlyOpen;

	public CommandMailbox(EvPlugin pl, MailboxClient mailboxHook){
		super(pl, true);
		plugin = pl;
		mailFetcher = mailboxHook;
		mailReaders = new HashMap<UUID, UUID>();
		currentlyOpen = new HashMap<UUID, ItemStack[]>();
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override @SuppressWarnings("deprecation")
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
		mailReaders.put(targetPlayer.getUniqueId(), player.getUniqueId());
		mailFetcher.loadMailbox(targetPlayer.getUniqueId(), this, true);
		return true;
	}

	private void openMailbox(UUID targetUUID, File mailboxFile, String message){
		UUID viewerUUID = mailReaders.get(targetUUID);
		Player player = plugin.getServer().getPlayer(viewerUUID);
		if(player == null){
			mailFetcher.saveMailbox(targetUUID, mailboxFile, CommandMailbox.this, true);
			plugin.getLogger().info("Mailbox fetched, but player is not longer online: "+viewerUUID);
			OfflinePlayer offP = plugin.getServer().getOfflinePlayer(viewerUUID);
			if(offP == null || !offP.hasPlayedBefore()) plugin.getLogger().severe("Unknown player in mailbox load: "+offP);
			return;
		}

		String metadata;
		int idx = message.indexOf('|');
		if(idx != -1){metadata = message.substring(0, idx); message = message.substring(idx+1);}
		else metadata = message;
		boolean failed = metadata.startsWith("failed");
		if(failed) metadata = metadata.substring(6);
		boolean locked = metadata.startsWith("locked");
		if(locked) metadata = metadata.substring(6);


		if(locked){
			player.sendMessage(ChatColor.RED+"Unable to fetch mailbox - It might be currently open elsewhere");
			return;
		}
		if(mailboxFile == null || failed){
			player.sendMessage(ChatColor.RED+"Failure when attempting to fetch mailbox");
			return;
		}

		// Save my current profile
		plugin.getLogger().info("[DEBUG] Saving current profile for "+player.getName());
		if(!SplitWorlds.saveCurrentProfile(player)){
			player.sendMessage(ChatColor.RED+"Encounter error while saving your current inventory!");
			mailFetcher.saveMailbox(targetUUID, mailboxFile, this, true);
			return;
		}
		GameMode gm = player.getGameMode();

		// Load the target's profile data
		if(!SplitWorlds.loadProfile(player, mailboxFile)){
			player.sendMessage("An error occurred reading the mailbox file");
			mailFetcher.saveMailbox(targetUUID, mailboxFile, this, true);
			return;
		}
		ItemStack[] contents = player.getEnderChest().getContents();
		currentlyOpen.put(viewerUUID, contents);

		// Reload my profile
		SplitWorlds.loadCurrentProfile(player);
		player.setGameMode(gm);

		// Create and display an inventory using the ItemStack[]
		final String invName = "> Global Mailbox Service";
		Inventory targetInv = plugin.getServer().createInventory(player, InventoryType.ENDER_CHEST, invName);
		targetInv.setContents(contents);
		player.openInventory(targetInv);
		plugin.getLogger().info(player.getName()+" has opened their mailbox");
		player.sendMessage(ChatColor.GREEN+"Mailbox opened");

		//TODO: Look to write back (save mailbox) every few seconds

		// Listener to write back to disk the inventory being viewed once it is closed
		plugin.getServer().getPluginManager().registerEvents(new Listener(){
			@EventHandler public void inventoryCloseEvent(InventoryCloseEvent evt){
				if(!evt.getPlayer().getUniqueId().equals(viewerUUID) ||
						evt.getInventory().getType() != InventoryType.ENDER_CHEST) return;

				currentlyOpen.put(viewerUUID, evt.getInventory().getContents());
				plugin.getLogger().info("Updating mailbox: "+targetUUID);

				SplitWorlds.saveCurrentProfile(player); // Update changes in my inv
				SplitWorlds.loadProfile(player, mailboxFile);
				player.getEnderChest().setContents(evt.getInventory().getContents());
				SplitWorlds.saveProfile(player, mailboxFile); // Update changes in mailbox inv
				SplitWorlds.loadCurrentProfile(player);

				mailFetcher.saveMailbox(targetUUID, mailboxFile, CommandMailbox.this, true);
				HandlerList.unregisterAll(this);
			}
			@EventHandler public void inventoryClickEvent(InventoryInteractEvent evt){
				if(!evt.getWhoClicked().getUniqueId().equals(viewerUUID) ||
						evt.getInventory().getType() != InventoryType.ENDER_CHEST) return;
				currentlyOpen.put(viewerUUID, evt.getInventory().getContents());
			}
		}, plugin);
	}

	@Override public void playerMailboxLoaded(UUID targetUUID, File mailboxFile, String message){
		new BukkitRunnable(){@Override public void run(){
			openMailbox(targetUUID, mailboxFile, message);
		}}.runTask(plugin);
	}

	@Override public void playerMailboxSaved(UUID targetUUID, String message){
		UUID viewerUUID = mailReaders.remove(targetUUID);
		if(viewerUUID == null) plugin.getLogger().severe("Unknown viewer: "+viewerUUID);
		Player player = plugin.getServer().getPlayer(viewerUUID);

		if(message.startsWith("failed")){
			plugin.getLogger().warning("Failed to save mailbox: "+targetUUID);
			ItemStack[] contents = currentlyOpen.remove(viewerUUID);
			if(contents == null) plugin.getLogger().severe("Unable to locate mailbox items");
			if(player != null) for(ItemStack overflowItem : player.getInventory().addItem(contents).values()){
				player.getWorld().dropItem(player.getLocation(), overflowItem);
			}
			else{
				plugin.getLogger().severe("Dropping mailbox items at world spawnpoint");
				World world = plugin.getServer().getWorld(SplitWorlds.getDefaultWorld());
				if(world == null) {
					plugin.getLogger().severe("Can't find a default world :l");
					world = plugin.getServer().getWorlds().get(0);
				}
				for(ItemStack item : contents) world.dropItem(world.getSpawnLocation(), item);
			}
		}
		else{
			plugin.getLogger().info("[DEBUG] Mailbox saved successfully: "+targetUUID);
			player.sendMessage(ChatColor.GOLD+"Mailbox saved");
		}
	}

	public void closeAllMailboxes(){
		
	}
}