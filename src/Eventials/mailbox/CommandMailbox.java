package Eventials.mailbox;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.economy.EvEconomy;
import Eventials.mailbox.MailboxClient.MailListener;
import Eventials.splitworlds.SplitWorlds;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.util.Pair;

public class CommandMailbox extends EvCommand implements MailListener{
	final EvPlugin plugin;
	final MailboxClient mailFetcher;
	final HashMap<UUID, UUID> viewerByTarget, targetByViewer;
	final HashMap<UUID, File> fileByTarget;
	final HashMap<UUID, Inventory> mailboxByViewer;
	final NamespacedKey MAIL_ITEM_FLAG;
	final HashMap<Material, Pair<Double, Double>> mailFees;
	final double DEFAULT_ADD_FEE, DEFAULT_REMOVE_FEE;
	final boolean CONTAINER_ITEMS_FEE;
	//give @p minecraft:golden_boots{CustomModelData:1, PublicBukkitValues:{"Eventials:mailable": 42}} 1

	void loadMailFeesFile(){
		mailFees.clear();
		InputStream defaultFees = plugin.getClass().getResourceAsStream("/mail_fees.csv");
		String[] fees = FileIO.loadFile("mail_fees.csv", defaultFees).split("\n");
		for(int i=0; i<fees.length; ++i){
			String[] data = fees[i].split(",", -1);// limit=-1 prevents split() from removing trailing empty strings
			if(data.length != 3){
				plugin.getLogger().warning("Invalid line in 'mail_fees.csv': ["+fees[i]+"], len="+data.length);
				continue;
			}
			Material type;
			try{type = Material.getMaterial(data[0].toUpperCase());}
			catch(IllegalArgumentException ex){type = null;}
			if(type == null){
				if(i != 0) plugin.getLogger().warning("Unknown item type: "+data[0]+" (please use exact material names)");
				continue;
			}
			Double sendCost, receiveCost;
			try{
				if(data[1].isEmpty()) sendCost = null;
				else sendCost = Double.parseDouble(data[1]);
				if(data[2].isEmpty()) receiveCost = null;
				else receiveCost = Double.parseDouble(data[2]);
			}
			catch(NumberFormatException ex){
				plugin.getLogger().warning("Invalid number cost in line: "+fees[i]);
				continue;
			}
			mailFees.put(type, new Pair<Double, Double>(sendCost, receiveCost));
		}
	}

	public CommandMailbox(EvPlugin pl, MailboxClient mailboxHook){
		super(pl, true);
		plugin = pl;
		mailFetcher = mailboxHook;
		viewerByTarget = new HashMap<UUID, UUID>();
		targetByViewer = new HashMap<UUID, UUID>();
		fileByTarget = new HashMap<UUID, File>();
		mailboxByViewer = new HashMap<UUID, Inventory>();
		MAIL_ITEM_FLAG = new NamespacedKey(plugin, "mailable");
		mailFees = new HashMap<Material, Pair<Double, Double>>();
		DEFAULT_ADD_FEE = plugin.getConfig().getDouble("default-sending-fee", -1D);
		DEFAULT_REMOVE_FEE = plugin.getConfig().getDouble("default-receiving-fee", -1D);
		CONTAINER_ITEMS_FEE = plugin.getConfig().getBoolean("container-fee-includes-contents", true);
		loadMailFeesFile();
	}

	public Double costToAdd(ItemStack item){
		if(item == null || item.getType() == Material.AIR) return 0D;
		double fee = 0D;
		if(CONTAINER_ITEMS_FEE && item.getItemMeta() instanceof BlockStateMeta &&
				((BlockStateMeta)item.getItemMeta()).getBlockState() instanceof InventoryHolder){
			for(ItemStack subItem : ((InventoryHolder)((BlockStateMeta)item.getItemMeta()).getBlockState())
					.getInventory().getContents()){
				Double incr = costToAdd(subItem);
				if(incr == null){if(DEFAULT_ADD_FEE < 0) return null; else incr = DEFAULT_ADD_FEE;}
				fee += incr;
			}
		}
		if(item.getItemMeta().getPersistentDataContainer().has(MAIL_ITEM_FLAG, PersistentDataType.BYTE)) return fee;
		Pair<Double, Double> sendCost = mailFees.get(item.getType());
		return sendCost == null ? (DEFAULT_ADD_FEE < 0 ? null : DEFAULT_ADD_FEE + fee) : sendCost.a + fee;
	}
	public Double costToRemove(ItemStack item){
		if(item == null || item.getType() == Material.AIR) return 0D;
		double fee = 0D;
		if(CONTAINER_ITEMS_FEE && item.getItemMeta() instanceof BlockStateMeta &&
				((BlockStateMeta)item.getItemMeta()).getBlockState() instanceof InventoryHolder){
			for(ItemStack subItem : ((InventoryHolder)((BlockStateMeta)item.getItemMeta()).getBlockState())
					.getInventory().getContents()){
				Double incr = costToRemove(subItem);
				if(incr == null){if(DEFAULT_ADD_FEE < 0) return null; else incr = DEFAULT_ADD_FEE;}
				fee += incr;
			}
		}
		if(item.getItemMeta().getPersistentDataContainer().has(MAIL_ITEM_FLAG, PersistentDataType.BYTE)) return fee;
		Pair<Double, Double> receiveCost = mailFees.get(item.getType());
		return receiveCost == null ? (DEFAULT_REMOVE_FEE < 0 ? null : DEFAULT_REMOVE_FEE + fee) : receiveCost.b + fee;
	}

	public boolean addToMailbox(ItemStack item, UUID sender){
		return costToAdd(item) != null && EvEconomy.getEconomy()
				.playerToServer(sender, mailFees.get(item.getType()).a);
	}
	public boolean takeFromMailbox(ItemStack item, UUID receiver){
		return costToRemove(item) != null && EvEconomy.getEconomy()
				.playerToServer(receiver, mailFees.get(item.getType()).b);
	}

	public void closeAllMailboxes(){
/*		// Called in a shutdown scenario, when we don't have time (or can't) save to the MailHost.
		// Any mailboxes open at this time must, unfortunately, have their contents dropped.
		World dropWorld = plugin.getServer().getWorlds().get(0);
		try(InputStream is = MailboxHoster.class.getResourceAsStream("/empty_inv_playerdata.dat")){
			Files.copy(is, Paths.get("./temp_empty_inv.dat"), StandardCopyOption.REPLACE_EXISTING);
		}
		catch(IOException e){System.err.println("Failed to load empty_inv resource");}
		File tempEmptyInvFile = new File("./temp_empty_inv.dat");

		for(Entry<UUID, Inventory> openMailbox : mailboxByViewer.entrySet()){
			UUID viewerUUID = openMailbox.getKey();
			plugin.getLogger().warning("Forcibly closing mailbox; was being viewed by "+viewerUUID);
			Player player = plugin.getServer().getPlayer(viewerUUID);
			if(player == null){
				// Just drop the items on the ground :(
				for(ItemStack item : openMailbox.getValue().getContents())
					if(item != null) dropWorld.dropItem(dropWorld.getSpawnLocation(), item);
			}
			else{
				// Drop the items at the player
				for(ItemStack item : openMailbox.getValue().getContents()){
					if(item != null) for(ItemStack item2 : player.getInventory().addItem(item).values())
						player.getWorld().dropItem(player.getLocation(), item2);
				}
			}
			mailFetcher.saveMailbox(targetByViewer.get(viewerUUID), tempEmptyInvFile, CommandMailbox.this, true);
		}
		mailboxByViewer.clear();
		//fileByTarget.clear();
		tempEmptyInvFile.delete();*/
		for(UUID viewerUUID : mailboxByViewer.keySet()){
			Player player = plugin.getServer().getPlayer(viewerUUID);
			if(player == null) plugin.getLogger().severe("Unable to save mailbox -- invalid player: "+viewerUUID);
			else player.closeInventory();
		}
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
		if(mailFetcher.bridge.isClosed()){
			sender.sendMessage(ChatColor.RED+"Mail Service is currently unavailable");
			return true;
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
		viewerByTarget.put(targetPlayer.getUniqueId(), player.getUniqueId());
		targetByViewer.put(player.getUniqueId(), targetPlayer.getUniqueId());
		mailFetcher.loadMailbox(targetPlayer.getUniqueId(), this, true);
		return true;
	}

	private void openMailbox(UUID targetUUID, File mailboxFile, String message){
		UUID viewerUUID = viewerByTarget.get(targetUUID);
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
		GameMode gm = player.getGameMode();
		boolean isFlying = player.isFlying();

		// Save my current profile
		plugin.getLogger().info("[DEBUG] Saving current profile for "+player.getName());
		if(!SplitWorlds.saveCurrentProfile(player)){
			player.sendMessage(ChatColor.RED+"Encounter error while saving your current inventory!");
			mailFetcher.saveMailbox(targetUUID, mailboxFile, this, true);
			return;
		}

		plugin.getLogger().info("[DEBUG] Loading mail file: "+mailboxFile.getName());
		// Load the target's profile data
		if(!SplitWorlds.loadProfile(player, mailboxFile)){
			player.sendMessage("An error occurred reading the mailbox file");
			mailFetcher.saveMailbox(targetUUID, mailboxFile, this, true);
			return;
		}
		ItemStack[] contents = player.getEnderChest().getContents();

		// Reload my profile
		SplitWorlds.loadCurrentProfile(player);
		player.setGameMode(gm);
		player.setFlying(isFlying);
		plugin.getLogger().info("[DEBUG] set gamemode: "+gm);

		// Create and display an inventory using the ItemStack[]
		final String invName = "> Global Mailbox Service";
		Inventory targetInv = plugin.getServer().createInventory(player, InventoryType.ENDER_CHEST, invName);
		targetInv.setContents(contents);
		mailboxByViewer.put(viewerUUID, targetInv);
		fileByTarget.put(targetUUID, mailboxFile);
		player.openInventory(targetInv);
		plugin.getLogger().info(player.getName()+" has opened their mailbox");
		player.sendMessage(ChatColor.GREEN+"Mailbox opened");

		//TODO: Write back (save mailbox) every few seconds

		// Listener to write back to disk the inventory being viewed once it is closed
		plugin.getServer().getPluginManager().registerEvents(new MailboxInventoryListener(viewerUUID), plugin);
	}

	private void closeMailbox(Player player, boolean saveChanges){
		UUID targetUUID = targetByViewer.get(player.getUniqueId());
		File mailboxFile = fileByTarget.get(targetUUID);
		plugin.getLogger().info("Updating mailbox: "+targetUUID);
		if(saveChanges){
			GameMode gm = player.getGameMode();
			boolean isFlying = player.isFlying();
			SplitWorlds.saveCurrentProfile(player); // Update changes in my inv
			SplitWorlds.loadProfile(player, mailboxFile);
			player.getEnderChest().setContents(mailboxByViewer.get(player.getUniqueId()).getContents());
			SplitWorlds.saveProfile(player, mailboxFile); // Update changes in mailbox inv
			SplitWorlds.loadCurrentProfile(player);
			player.setGameMode(gm);
			player.setFlying(isFlying);
		}
		mailFetcher.saveMailbox(targetUUID, mailboxFile, CommandMailbox.this, true);
	}

	@Override public void playerMailboxLoaded(UUID targetUUID, File mailboxFile, String message){
		new BukkitRunnable(){@Override public void run(){openMailbox(targetUUID, mailboxFile, message);}}.runTask(plugin);
	}

	@Override public void playerMailboxSaved(UUID targetUUID, String message){
		UUID viewerUUID = viewerByTarget.remove(targetUUID);
		if(viewerUUID == null) plugin.getLogger().severe("Unknown viewer: "+viewerUUID);
		targetByViewer.remove(viewerUUID);
		fileByTarget.remove(targetUUID);
		ItemStack[] contents = mailboxByViewer.remove(viewerUUID).getContents();
		Player player = plugin.getServer().getPlayer(viewerUUID);

		if(message.startsWith("failed")){
			plugin.getLogger().warning("Failed to save mailbox: "+targetUUID);
			
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

	class MailboxInventoryListener implements Listener{
		final UUID viewerUUID;
		final HashMap<ItemStack, Integer> itemsToAdd, itemsToRemove;//ItemStack(amount=1) -> amount
		boolean cursorItemIsFromMailbox = false;
		double currentCost = 0D;
		MailboxInventoryListener(UUID viewer){
			viewerUUID = viewer;
			itemsToAdd = new HashMap<ItemStack, Integer>();
			itemsToRemove = new HashMap<ItemStack, Integer>();
		}

		void chargeFeesAndCloseMailbox(Player player){
			if(currentCost > 0 && !EvEconomy.getEconomy().playerToServer(viewerUUID, currentCost)){
				double missingItemsCost = 0D;
				player.sendMessage(ChatColor.DARK_RED+"Error: "+ChatColor.RED+"Unable to afford mailbox interaction");
				for(Entry<ItemStack, Integer> itemAndAmt : itemsToRemove.entrySet()){
					double costToRemove = costToRemove(itemAndAmt.getKey());
					for(int i=0; i<itemAndAmt.getValue(); ++i)
						if(!player.getInventory().removeItem(itemAndAmt.getKey()).isEmpty())
							missingItemsCost += costToRemove;
				}
				for(Entry<ItemStack, Integer> itemAndAmt : itemsToAdd.entrySet()){
					ItemStack item64 = itemAndAmt.getKey(); item64.setAmount(64);
					for(int i=0; i<itemAndAmt.getValue()/64; ++i)
						for(ItemStack leftover : player.getInventory().addItem(item64).values())
							player.getWorld().dropItem(player.getLocation(), leftover);
					int rem = itemAndAmt.getValue()%64;
					if(rem > 0){
						ItemStack itemRem = itemAndAmt.getKey(); itemRem.setAmount(rem);
						for(ItemStack leftover : player.getInventory().addItem(itemRem).values())
							player.getWorld().dropItem(player.getLocation(), leftover);
					}
				}
				plugin.getLogger().severe("Unable to afford mailbox interaction!");
				plugin.getLogger().warning("Player: "+player.getName());
				plugin.getLogger().warning("Lost item cost/Fees bypassed: "+missingItemsCost);
				if(missingItemsCost > 0 && EvEconomy.getEconomy().playerToServer(viewerUUID, missingItemsCost)){
					plugin.getLogger().warning("Managed to charge remainder fees to player balance");
				}
				closeMailbox(player, false);
			}
			closeMailbox(player, true);
		}

		@EventHandler public void playerQuitEvent(PlayerQuitEvent evt){
			if(evt.getPlayer().getUniqueId().equals(viewerUUID)){
				HandlerList.unregisterAll(this);
				chargeFeesAndCloseMailbox(evt.getPlayer());
			}
		}
		@EventHandler public void inventoryCloseEvent(InventoryCloseEvent evt){
			if(evt.getPlayer().getUniqueId().equals(viewerUUID) &&
					evt.getInventory().equals(mailboxByViewer.get(viewerUUID))){
				HandlerList.unregisterAll(this);
				chargeFeesAndCloseMailbox((Player)evt.getPlayer());
			}
		}
		@EventHandler(priority = EventPriority.HIGHEST)
		public void itemDragEvent(InventoryDragEvent evt){
			if(evt.isCancelled() || evt.getWhoClicked().getUniqueId() != viewerUUID ||
					!evt.getInventory().equals(mailboxByViewer.get(viewerUUID))) return;
			plugin.getLogger().info("-------------------------- Drag evt:");
			plugin.getLogger().info("Current inv: "+evt.getInventory().getType());
			plugin.getLogger().info("Cursor item: "+(evt.getCursor() == null ? "null" : evt.getCursor().getType()));
			plugin.getLogger().info("Old cursor item: "+(evt.getOldCursor() == null ? "null" : evt.getOldCursor().getType()));
			plugin.getLogger().info("Inventory slots: "+
					evt.getInventorySlots().stream().map(i -> i.toString()).collect(Collectors.joining(",")));
			plugin.getLogger().info("Raw slots: "+
					evt.getRawSlots().stream().map(i -> i.toString()).collect(Collectors.joining(",")));
			plugin.getLogger().info("New items: "+
					evt.getNewItems().values().stream().map(i -> i.getType().toString()).collect(Collectors.joining(",")));
			
			ItemStack singleAmtItem = evt.getOldCursor(); singleAmtItem.setAmount(1);
			Double costToAdd = costToAdd(singleAmtItem);
			Double costToRemove = costToRemove(singleAmtItem);
			double costDelta = 0D;
			HashMap<ItemStack, Integer> itemsToAddDelta = new HashMap<ItemStack, Integer>();
			HashMap<ItemStack, Integer> itemsToRemoveDelta = new HashMap<ItemStack, Integer>();
			for(int slot : evt.getNewItems().keySet()){
				if(slot < 27){/*putIn*/
					if(!cursorItemIsFromMailbox){
						int amtOfItemToMail = evt.getNewItems().get(slot).getAmount();
						int amtPendingRemove = itemsToRemove.getOrDefault(singleAmtItem, 0);
						if(amtPendingRemove >= amtOfItemToMail) costDelta -= costToRemove*amtOfItemToMail;
						else{
							amtOfItemToMail -= amtPendingRemove;
							if(costToAdd == null){
								evt.getWhoClicked().sendMessage(ChatColor.RED+"That item is not currently eligible for the GMS");
								evt.setCancelled(true);
								return;
							}
							costDelta += costToAdd*amtOfItemToMail;
						}
						itemsToAddDelta.put(singleAmtItem, amtOfItemToMail);
					}
				}
				else if(cursorItemIsFromMailbox){/*takenOut*/
					int amtOfItemToWithdraw = evt.getNewItems().get(slot).getAmount();
					int amtPendingToMail = itemsToAdd.getOrDefault(singleAmtItem, 0);
					if(amtPendingToMail >= amtOfItemToWithdraw) costDelta -= costToAdd*amtOfItemToWithdraw;
					else{
						amtOfItemToWithdraw -= amtPendingToMail;
						if(costToRemove == null){
							evt.getWhoClicked().sendMessage(ChatColor.RED+"That item cannot be withdrawn from the GMS on this world");
							evt.setCancelled(true);
							return;
						}
						costDelta += costToRemove*amtOfItemToWithdraw;
					}
					itemsToRemoveDelta.put(singleAmtItem, amtOfItemToWithdraw);
				}
			}
			// Check if they can afford this action
			if(costDelta > 0 && EvEconomy.getEconomy().playerHasAtLeast(viewerUUID, currentCost + costDelta)){
				evt.getWhoClicked().sendMessage(ChatColor.RED+"You do not have sufficient funds to complete this action");
				evt.setCancelled(true);
			}
			else{
				for(Entry<ItemStack, Integer> itemAndAmt : itemsToAddDelta.entrySet()){
					Integer amtToRemove = itemsToRemove.get(itemAndAmt.getKey());
					if(amtToRemove == null) itemsToAdd.put(itemAndAmt.getKey(),
							itemsToAdd.getOrDefault(itemAndAmt.getKey(), 0) + itemAndAmt.getValue());
					else{
						if(itemAndAmt.getValue() > amtToRemove){
							itemsToAdd.put(itemAndAmt.getKey(), itemAndAmt.getValue() - amtToRemove);
							itemsToRemove.remove(itemAndAmt.getKey());
						}
						if(itemAndAmt.getValue() == amtToRemove){
							itemsToRemove.remove(itemAndAmt.getKey());
						}
						if(itemAndAmt.getValue() < amtToRemove){
							itemsToRemove.put(itemAndAmt.getKey(), amtToRemove - itemAndAmt.getValue());
						}
					}
				}
				for(Entry<ItemStack, Integer> itemAndAmt : itemsToRemoveDelta.entrySet()){
					Integer amtToAdd = itemsToAdd.get(itemAndAmt.getKey());
					if(amtToAdd == null) itemsToRemove.put(itemAndAmt.getKey(),
							itemsToRemove.getOrDefault(itemAndAmt.getKey(), 0) + itemAndAmt.getValue());
					else{
						if(itemAndAmt.getValue() > amtToAdd){
							itemsToRemove.put(itemAndAmt.getKey(), itemAndAmt.getValue() - amtToAdd);
							itemsToAdd.remove(itemAndAmt.getKey());
						}
						if(itemAndAmt.getValue() == amtToAdd){
							itemsToAdd.remove(itemAndAmt.getKey());
						}
						if(itemAndAmt.getValue() < amtToAdd){
							itemsToAdd.put(itemAndAmt.getKey(), amtToAdd - itemAndAmt.getValue());
						}
					}
				}
				if(evt.getCursor() == null || evt.getCursor().getType() == Material.AIR) cursorItemIsFromMailbox = false;
				currentCost += costDelta;
			}
		}
		@EventHandler(priority = EventPriority.HIGHEST)
		public void itemClickEvent(InventoryClickEvent evt){
			if(evt.isCancelled() || evt.getWhoClicked().getUniqueId() != viewerUUID ||
					!evt.getInventory().equals(mailboxByViewer.get(viewerUUID))) return;
			plugin.getLogger().info("-------------------------- Click evt:");
			plugin.getLogger().info("Clicked inv: "+
					(evt.getClickedInventory() == null ? "null" : evt.getClickedInventory().getType()));
			plugin.getLogger().info("Regular inv: "+evt.getInventory().getType());
			plugin.getLogger().info("Click action: "+evt.getAction());
			plugin.getLogger().info("Click type: "+evt.getClick());
			plugin.getLogger().info("Current item: "+(evt.getCurrentItem() == null ? "null" : evt.getCurrentItem().getType()));
			plugin.getLogger().info("Cursor item: "+(evt.getCursor() == null ? "null" : evt.getCursor().getType()));
			plugin.getLogger().info("Slot type: "+evt.getSlotType());
			plugin.getLogger().info("Slot: "+evt.getSlot());
			plugin.getLogger().info("Raw slot: "+evt.getRawSlot());
			plugin.getLogger().info("Hotbar button: "+evt.getHotbarButton());
			
			boolean inMailbox = evt.getRawSlot() < 27;
			ItemStack putIn = null, takenOut = null;
			boolean setFromMailboxFlag = false, fromMailboxFlagValue = false, tempVarDeleteMe = false;
			switch(evt.getAction()){
				case COLLECT_TO_CURSOR:
					if(evt.getCursor() != null)
						plugin.getLogger().warning("Expected cursor to be null! pls debug CommandMailbox");
					if(inMailbox){
						setFromMailboxFlag = true;
						fromMailboxFlagValue = true;
					}
					break;
				case DROP_ALL_CURSOR:
				case DROP_ONE_CURSOR:
					if(cursorItemIsFromMailbox) takenOut = evt.getCursor();
					break;
				case DROP_ONE_SLOT:
				case DROP_ALL_SLOT:
					if(inMailbox) takenOut = evt.getCurrentItem();
					break;
				case HOTBAR_SWAP:
				case HOTBAR_MOVE_AND_READD:
					if(inMailbox){
						takenOut = evt.getCurrentItem();
						putIn = evt.getWhoClicked().getInventory().getItem(evt.getHotbarButton());
					}
					break;
				case MOVE_TO_OTHER_INVENTORY:
					tempVarDeleteMe = true;
					if(inMailbox) takenOut = evt.getCurrentItem();
					else putIn = evt.getCurrentItem();
					break;
				case PLACE_ALL:
					if(inMailbox) putIn = evt.getCursor();
					else if(cursorItemIsFromMailbox) takenOut = evt.getCursor();
					setFromMailboxFlag = true;
					fromMailboxFlagValue = false;
					break;
				case PLACE_ONE:
				case PLACE_SOME:
					if(inMailbox) putIn = evt.getCursor();
					else if(cursorItemIsFromMailbox) takenOut = evt.getCursor();
					if(evt.getCursor().getAmount() == 1){
						setFromMailboxFlag = true;
						fromMailboxFlagValue = false;
					}
					break;
				case PICKUP_ALL:
				case PICKUP_ONE:
				case PICKUP_SOME:
				case PICKUP_HALF:
					if(inMailbox){
						setFromMailboxFlag = true;
						fromMailboxFlagValue = true;
					}
					break;
				case SWAP_WITH_CURSOR:
					if(inMailbox){
						putIn = evt.getCursor();
						setFromMailboxFlag = true;
						fromMailboxFlagValue = true;
					}
					else{
						if(cursorItemIsFromMailbox) takenOut = evt.getCursor();
					}
					break;
				case CLONE_STACK:
				case NOTHING:
				case UNKNOWN:
				default:
					break;
			}
			plugin.getLogger().info("=>");
			double costDelta = 0D;
			int putInAmt = 1, takenOutAmt = 1;
			if(putIn != null){
				plugin.getLogger().info("Put in: "+putIn.getType());
				putInAmt = putIn.getAmount();
				putIn.setAmount(1);
				Double costToAdd = costToAdd(putIn);
				Double costToRemove = costToRemove(putIn);
				int amtPendingRemove = itemsToRemove.getOrDefault(putIn, 0);
				if(amtPendingRemove >= putInAmt) costDelta -= costToRemove*putInAmt;
				else{
					if(costToAdd == null){
						plugin.getLogger().info("[DEBUG] item not mailable: "+putIn.getType());
						//**************************************************************
						if(evt.getWhoClicked().isOp() && tempVarDeleteMe){
							evt.getWhoClicked().sendMessage(ChatColor.GREEN+"Adding 'mailable' tag to item: "+putIn.getType());
							ItemMeta meta = putIn.getItemMeta();
							meta.getPersistentDataContainer().set(MAIL_ITEM_FLAG, PersistentDataType.BYTE, (byte)1);
							putIn.setItemMeta(meta); evt.setCurrentItem(putIn);
							return;
						}//**************************************************************
						evt.getWhoClicked().sendMessage(ChatColor.RED+"That item is not currently eligible for the GMS");
						evt.setCancelled(true);
						return;
					}
					costDelta += costToAdd*putInAmt;
				}
			}
			if(takenOut != null){
				plugin.getLogger().info("Taken out: "+takenOut.getType());
				takenOutAmt = takenOut.getAmount();
				takenOut.setAmount(1);
				Double costToAdd = costToAdd(takenOut);
				Double costToRemove = costToRemove(takenOut);
				int amtPendingAdd = itemsToAdd.getOrDefault(takenOut, 0);
				if(amtPendingAdd >= takenOutAmt) costDelta -= costToAdd*takenOutAmt;
				else{
					if(costToRemove == null){
						plugin.getLogger().info("[DEBUG] item cannot by taken from mailbox: "+takenOut.getType());
						evt.getWhoClicked().sendMessage(ChatColor.RED+"That item cannot be withdrawn from the GMS on this world");
						evt.setCancelled(true);
						return;
					}
					costDelta += costToRemove*takenOutAmt;
				}
			}
			// Check if they can afford this action
			if(costDelta > 0 && EvEconomy.getEconomy().playerHasAtLeast(viewerUUID, currentCost + costDelta)){
				evt.getWhoClicked().sendMessage(ChatColor.RED+"You do not have sufficient funds to complete this action");
				evt.setCancelled(true);
			}
			else{
				if(putIn != null){
					Integer amtToRemove = itemsToRemove.get(putIn);
					if(amtToRemove == null) itemsToAdd.put(putIn, itemsToAdd.getOrDefault(putIn, 0) + putInAmt);
					else{
						if(putInAmt > amtToRemove){
							itemsToAdd.put(putIn, putInAmt - amtToRemove);
							itemsToRemove.remove(putIn);
						}
						if(putInAmt == amtToRemove){
							itemsToRemove.remove(putIn);
						}
						if(putInAmt < amtToRemove){
							itemsToRemove.put(putIn, amtToRemove - putInAmt);
						}
					}
				}
				if(takenOut != null){
					Integer amtToAdd = itemsToAdd.get(takenOut);
					if(amtToAdd == null) itemsToRemove.put(takenOut, itemsToRemove.getOrDefault(takenOut, 0) + takenOutAmt);
					else{
						if(takenOutAmt > amtToAdd){
							itemsToRemove.put(takenOut, takenOutAmt - amtToAdd);
							itemsToAdd.remove(takenOut);
						}
						if(takenOutAmt == amtToAdd){
							itemsToAdd.remove(takenOut);
						}
						if(takenOutAmt < amtToAdd){
							itemsToAdd.put(takenOut, amtToAdd - takenOutAmt);
						}
					}
				}
				if(setFromMailboxFlag) cursorItemIsFromMailbox = fromMailboxFlagValue;
				currentCost += costDelta;
			}
		}
	}
}