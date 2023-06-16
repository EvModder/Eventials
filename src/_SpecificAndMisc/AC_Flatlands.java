package _SpecificAndMisc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import Eventials.Eventials;
import Eventials.economy.EvEconomy;
import Evil_Code_EvKits.EvKits;
import net.evmodder.EvLib.extras.NBTTagUtils;
import net.evmodder.EvLib.extras.NBTTagUtils.RefNBTTagCompound;
import net.evmodder.EvLib.extras.NBTTagUtils.RefNBTTagList;

public class AC_Flatlands implements Listener {
	Eventials pl;
	Random rand;

	final String[] blockedPastWalls = new String[]{
			"/f c",
			"/f claim",
			"/faction claim",
			"/faction c",
	//		"/sethome",
			"/setwarp",
	};
	final String[] quickWarps = new String[]{
			"/creative,/cr",
			"/racetrack",
			"/marketplace,/market",
			"/adminshop",
			"/downtown,/dt",
			"/parkour",
			"/suggestions,/suggest",
			"/freebuild,/fb"
	};

	public AC_Flatlands(){
		rand = new Random();
		pl = Eventials.getPlugin();
		pl.getServer().getPluginManager().registerEvents(this, pl);
		pl.getCommand("vipgive").setExecutor(new CommandVipGive());
		pl.getCommand("viptake").setExecutor(new CommandVipTake());
	}

	public static double getMoneyOrderValue(ItemStack mo){
		if(mo != null && mo.getType() == Material.PAPER && mo.hasItemMeta()
				&& mo.getItemMeta().hasLore() && mo.getItemMeta().getLore().get(0).contains("Stored balance:"))
		{
			return Double.parseDouble(ChatColor.stripColor(mo.getItemMeta().getLore().get(0).split(":")[1].replace("$", "").trim()));
		}
		else return 0;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTp(PlayerTeleportEvent evt){
		if(evt.isCancelled() || evt.getCause() == TeleportCause.ENDER_PEARL) return;
		
		String toWorldName = evt.getTo().getWorld().getName(), fromWorldName = evt.getFrom().getWorld().getName();
		Player p = evt.getPlayer();
		
		if((toWorldName.equals("Events") || fromWorldName.equals("Events")) && !p.isOp()){
			
			boolean fromInEvents = fromWorldName.equals("Events");
			boolean toInEvents = toWorldName.equals("Events");
			
			boolean fromInArena = false, toInArena = false;
			EvKits evKitPVP = (EvKits) pl.getServer().getPluginManager().getPlugin("EvKitPvP");
			if(evKitPVP != null && evKitPVP.isEnabled()){
				fromInArena = evKitPVP.isInArena(evt.getFrom(), false) != null;
				toInArena = evKitPVP.isInArena(evt.getTo(), false) != null;
			}
			
			if((!toInEvents && fromInArena) || (!fromInEvents && toInArena) || (fromInArena && toInArena));/* All good */
			
			if(fromWorldName.equals("VictoryHills")
					&& evt.getFrom().getBlockX() == -9 && evt.getFrom().getBlockZ() == 4 && evt.getFrom().getBlockY() == 148){
				if(EvEconomy.getEconomy().playerToServer(p.getUniqueId(), 500) == false){
					evt.setCancelled(true);
					p.sendMessage("�cYou do not have enough money to afford this portal");
				}
			}
			else{
				evt.setCancelled(true);
				p.sendMessage("�c-\n�4Trans-world teleportaion is not allowed to/from here.\n�c-");
			}
		}
	}

	@EventHandler
	public void onEggDrop(PlayerDropItemEvent evt){
		if(evt.getItemDrop().getItemStack().getType() == Material.EGG
				&& evt.getItemDrop().getItemStack().hasItemMeta() == false
				&& evt.getItemDrop().getLocation().getBlockX() > 9000
				&& evt.getItemDrop().getLocation().getBlockZ() > 9000)
		{
			ItemMeta meta = evt.getItemDrop().getItemStack().getItemMeta();
			meta.setLore(Arrays.asList("player"));
			evt.getItemDrop().getItemStack().setItemMeta(meta);
		}
	}
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent evt){
		if(evt.getEntity().getItemStack().getType() == Material.EGG
				&& evt.getEntity().getLocation().getBlockX() > 9000
				&& evt.getEntity().getLocation().getBlockZ() > 9000)
		{
			if(evt.getEntity().getItemStack().hasItemMeta()){
				if(evt.getEntity().getItemStack().getItemMeta().hasLore()
					&& evt.getEntity().getItemStack().getItemMeta().getLore().get(0).equals("player"))
				{
					evt.getEntity().setItemStack(new ItemStack(Material.EGG, evt.getEntity().getItemStack().getAmount()));
				}
				return;
			}
			for(Entity e : evt.getEntity().getNearbyEntities(3, 3, 3)){
				if(e instanceof Chicken && ((Chicken)e).getCustomName() != null
						&& ChatColor.stripColor(((Chicken)e).getCustomName().toLowerCase()).contains("prisoner")){
					evt.getEntity().setItemStack(
					rand.nextInt(4) != 0 ? (rand.nextInt(8) == 0 ? (rand.nextInt(2) == 0 ? (rand.nextInt(7) == 0 ? (rand.nextInt(16) == 0 ?
					makeOpEgg() :
					new ItemStack(Material.ENCHANTED_GOLDEN_APPLE)) :
					new ItemStack(Material.GOLD_BLOCK)) :
					new ItemStack(Material.GOLDEN_APPLE)) :
					new ItemStack(Material.GOLD_INGOT)) :
					new ItemStack(Material.EGG));
					return;
				}
			}
		}
	}
	private ItemStack makeOpEgg(){
		RefNBTTagCompound tag = new RefNBTTagCompound();
		RefNBTTagList attributeModifiers = new RefNBTTagList();
		//----------------------- Attack attribute -----------------------
		RefNBTTagCompound attribute = new RefNBTTagCompound();
//		atributeModifiers.setString("Slot", "mainhand");
		attribute.setString("AttributeName", "generic.attackDamage");
		attribute.setString("Name", "generic.attackDamage");
		attribute.setInt("Amount", 20);
		attribute.setInt("Operation", 0);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		attributeModifiers.add(attribute);
		tag.set("AttributeModifiers", attributeModifiers);
		//----------------------------------------------------------------
		ItemStack item = new ItemStack(Material.EGG);
		item = NBTTagUtils.setTag(item, tag);

		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
		meta.setLore(Arrays.asList(ChatColor.GOLD+"Egg of Power"));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		return item;
	}

	@EventHandler
	public void entityPortalEvent(EntityPortalEvent evt){
		if(evt.getFrom().getWorld().getName().equals("Creative")){
			evt.setCancelled(true);
			if(evt.getEntityType() == EntityType.DROPPED_ITEM){
				for(Entity e : evt.getEntity().getNearbyEntities(5, 10, 5)){
					if(e instanceof Player){
						((Player)e).setGameMode(GameMode.SURVIVAL);
						((Player)e).setHealth(.5);// punish them for their insolence
					}
				}
				evt.getEntity().remove();
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreCommand(PlayerCommandPreprocessEvent evt){
		if(evt.isAsynchronous() || evt.isCancelled())return;

		String message = evt.getMessage().toLowerCase();
		String command = message.split(" ")[0].replace("-", "");
//		pl.getLogger().info(evt.getPlayer().getName()+' '+evt.getMessage());//TODO: Use this?

		int px = evt.getPlayer().getLocation().getBlockX(), pz = evt.getPlayer().getLocation().getBlockZ();

		if(evt.getPlayer().getWorld().getName().equals("VictoryHills")
				&& (px > 10000 || pz > 10000) && evt.getPlayer().isOp() == false){
			for(String bcmd : blockedPastWalls){
				if(command.startsWith(bcmd)){
					evt.setCancelled(true);
					evt.getPlayer().sendMessage(ChatColor.RED+"You can not use that command here");
				}
			}
		}
		else if(command.equals("/cleanworld") && message.contains(" ") && evt.getPlayer().isOp()){
			evt.setCancelled(true);

			final String wName = message.split(" ")[1];
			final UUID pUUID = evt.getPlayer().getUniqueId();

			evt.getPlayer().sendMessage(ChatColor.RED+"Staring world-cleanup");

			new Thread(){@Override public void run(){
				File folder = new File("./"+wName+"/region/");
				File[] listOfFiles = folder.listFiles();

				for(int i = 0; i < listOfFiles.length; ++i){
					if(listOfFiles[i].isFile()){
						String fname = listOfFiles[i].getName();
						int x = 0, z = 0;
						try{
							x = Math.abs(Integer.parseInt(fname.replace(".", ",").split(",")[1]));
							z = Math.abs(Integer.parseInt(fname.replace(".", ",").split(",")[2]));
						}catch(ArrayIndexOutOfBoundsException ex){}

						if((x > 3 || z > 3) && (x != 0)){
							//has to be less then 10k if it's in VictoryHills to delete it
							if(!wName.equalsIgnoreCase("VictoryHills") || (x > 1 && z < 1)){
								//
								double fileSize = (double)listOfFiles[i].length()/1024;
								if(fileSize < 1200) listOfFiles[i].delete();
							}
						}
					}
				}
				Player p = pl.getServer().getPlayer(pUUID);
				if(p != null) p.sendMessage(ChatColor.GREEN+"World Cleaned!");
			}}.start();
		}
		else if(!message.contains(" ")){
			for(String warp : quickWarps){
				for(String alias : warp.split(",")){
					if(message.equals(alias)){
						evt.setMessage("/warp "+warp.split(",")[0].replaceFirst("/", ""));
						return;
					}
				}
			}
		}
	}

	List<int[]> chunksToClean = new ArrayList<>();
	List<String> cleanWorldNames = new ArrayList<>();
	@EventHandler public void onChunkUnload(ChunkUnloadEvent evt){
		if(evt.getChunk() == null || chunksToClean.contains(new int[]{evt.getChunk().getX(), evt.getChunk().getZ()})) return;
		
		cleanWorldNames.add(evt.getWorld().getName());
		chunksToClean.add(new int[]{evt.getChunk().getX(), evt.getChunk().getZ()});
		
		//TODO: fix & re-enable
		/*
//		evm.getLogger().info("butchering!!");
		new BukkitRunnable(){
			@Override public void run(){
				int[] coords = chunksToClean.get(0);
				World w = pl.getServer().getWorld(cleanWorldNames.get(0));
				if(w == null) return;
				Chunk chunk = w.getChunkAt(coords[0], coords[1]);
				if(chunk.isLoaded() == false && chunk.load()){
					int killed = ButcherUtils.clearEntitiesByChunk(chunk);
					if(killed > 0) pl.getLogger().info("Killed in chunk unload: "+killed);
					chunk.unload();
					chunksToClean.remove(0);//these remove()s must come after the .unload()
					cleanWorldNames.remove(0);
				}
			}
		}.runTaskLater(pl, 1200);//1 minute*/
	}
}