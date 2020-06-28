package _SpecificAndMisc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.extras.TellrawUtils.ActionComponent;
import net.evmodder.EvLib.extras.TellrawUtils.HoverEvent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.extras.TellrawUtils.TellrawBlob;
import net.evmodder.EvLib.util.Pair;
import net.evmodder.Renewable.Renewable;
import net.evmodder.Renewable.RenewableAPI;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;

public class AC_New implements Listener{
	private final HashSet<Pair<Integer,Integer>> modChunks;
	private final Eventials pl;
	private final RenewableAPI renewableAPI;
	final boolean fancyPl;

	public AC_New(){
		pl = Eventials.getPlugin();
		renewableAPI = ((Renewable)pl.getServer().getPluginManager().getPlugin("Renewable")).getAPI();
		fancyPl = pl.getConfig().getBoolean("fancy-pl", true);
		pl.getServer().getPluginManager().registerEvents(this, pl);
		modChunks = new HashSet<Pair<Integer, Integer>>();
		loadModChunks();
	}

	void loadModChunks(){
		modChunks.clear();
		for(String chunk : FileIO.loadFile("leafdecaychunks.txt", "").split(" ")){
			int i = chunk.indexOf(',');
			if(i != -1){
				try{
					int x = Integer.parseInt(chunk.substring(0, i)), y = Integer.parseInt(chunk.substring(i+1));
					modChunks.add(new Pair<Integer, Integer>(x, y));
				}
				catch(NumberFormatException ex){}
			}
		}
	}
	void saveModChunks(){
		StringBuilder builder = new StringBuilder();
		for(Pair<Integer, Integer> chunk : modChunks)
			builder.append(chunk.a).append(',').append(chunk.b).append(' ');
		FileIO.saveFile("leafdecaychunks.txt", builder.toString());
		updateFile = false;
	}

	private boolean updateFile = false;
	public void addModChunk(Chunk c){
		if(modChunks.add(new Pair<Integer, Integer>(c.getX(), c.getZ()))){
			if(!updateFile){
				updateFile = true;
				new BukkitRunnable(){
					@Override public void run(){
						saveModChunks();
					}
				}.runTaskLater(pl, 20*60*10);//every 10 minutes
			}
		}
	}

	@EventHandler
	public void onLeafDecay(LeavesDecayEvent evt){
		if(!evt.isCancelled()){
			int x = evt.getBlock().getChunk().getX(), z = evt.getBlock().getChunk().getZ();
			if(!modChunks.contains(new Pair<Integer, Integer>(x, z))) evt.setCancelled(true);
		}
	}

	@EventHandler
	public void onTreeGrow(StructureGrowEvent evt){
		if(isWoodTree(evt.getSpecies())){
			addModChunk(evt.getBlocks().iterator().next().getChunk());
		}
	}

	@EventHandler
	public void onVinePlant(BlockPlaceEvent evt){
		if(evt.getBlockPlaced().getType() == Material.VINE){
			addModChunk(evt.getBlockPlaced().getChunk());
		}
	}

	public boolean isWoodTree(TreeType tree){
		switch(tree){
			case CHORUS_PLANT:
			case BROWN_MUSHROOM:
			case RED_MUSHROOM:
				return false;
			default:
				return true;
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent evt){
		FPlayer fplayer = FPlayers.i.get(evt.getPlayer());
		if(!fplayer.hasFaction()) evt.setFormat(ChatColor.GRAY+"%s"+ChatColor.RESET+" %s");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerBreakBlock(BlockBreakEvent evt){
		if(evt.isCancelled() &&
				Math.abs(evt.getBlock().getLocation().getBlockX()) < 240 &&
				Math.abs(evt.getBlock().getLocation().getBlockZ()) < 240){
			evt.getPlayer().sendMessage(ChatColor.GRAY+"> "+ChatColor.AQUA+"Travel out "
				+ChatColor.GRAY+"240"+ChatColor.AQUA+" blocks to edit terrain "+ChatColor.GRAY+":)");
		}
	}

	ChatColor enableTest(String pluginName){
		Plugin plugin = Eventials.getPlugin().getServer().getPluginManager().getPlugin(pluginName);
		return (plugin != null && plugin.isEnabled()) ? ChatColor.GREEN : ChatColor.RED;
	}
	void showFancyPlugins(Player player){
		TellrawBlob blob = new TellrawBlob(
				new RawTextComponent("Plugins: "),
				new ActionComponent(enableTest("OpenTerrainGenerator")+"OTG", HoverEvent.SHOW_TEXT, "Open Terrain Generator (custom terrain)"),
				new RawTextComponent("§r, "),
				new ActionComponent(enableTest("Renewable")+"Renewable", HoverEvent.SHOW_TEXT, "Prevents unrenewable items from being destroyed"),
				new RawTextComponent("§r, "),
				new ActionComponent(enableTest("Essentials")+"Essentials", HoverEvent.SHOW_TEXT, "Collection of useful tools and commands"),
				new RawTextComponent("§r, "),
				new ActionComponent(enableTest("Eventials")+"Eventials", HoverEvent.SHOW_TEXT, "Package of custom-built features and tweaks"),
				new RawTextComponent("§r, "),
				new ActionComponent(enableTest("DropHeads")+"DropHeads", HoverEvent.SHOW_TEXT, "Provides a chance to get heads from mobs/players"),
				new RawTextComponent("§r,\n"),
				new ActionComponent(enableTest("Factions")+"Factions", HoverEvent.SHOW_TEXT, "Protect your land and build communities"),
				new RawTextComponent("§r, "),
				new ActionComponent(enableTest("HorseOwners")+"HorseRanks", HoverEvent.SHOW_TEXT, "Claim, name, and view stats for horses"),
				new RawTextComponent("§r, "),
				new ActionComponent(enableTest("ChatManager")+"ChatTweaks", HoverEvent.SHOW_TEXT, "Keeps chat pg13 + Color/Format for chat & signs"),
				new RawTextComponent("§r, "),
				new ActionComponent(enableTest("EnchantBook")+"EnchantBook", HoverEvent.SHOW_TEXT, "Color item names in anvils, looting on axes, etc!"),
				new RawTextComponent("§r, "),
				new ActionComponent("More", HoverEvent.SHOW_TEXT,
						enableTest("WorldEdit")+"WorldEdit§r, "+
						enableTest("WorldGuard")+"WorldGuard§r, "+
						enableTest("PluginLoader")+"PluginLoader§r, "+
						enableTest("EssentialsSpawn")+"EssentialsSpawn§r, "+
						enableTest("Votifier")+"Votifier§r, "+
						"§aEvAntiCheat§r, "+
						enableTest("BungeeTabListPlus")+"TabList+§r, "+
						enableTest("PermissionsBukkit")+"PermissionsBukkit§r."+
						"\n§7§oHover over a plugin to see more details!"
				)
			);
		Eventials.getPlugin().sendTellraw(player, blob.toString());
	}
	@EventHandler
	public void onPreCommand(PlayerCommandPreprocessEvent evt){
		String command = evt.getMessage().toLowerCase();
		int space = command.indexOf(' ');
		if(space > 0) command = command.substring(0, space);

		if(command.contains("sethome") && !evt.getPlayer().hasPermission("essentials.sethome")){
			evt.getPlayer().sendMessage(ChatColor.AQUA+"/sethome"+ChatColor.WHITE+" isn't enabled.");
			evt.getPlayer().sendMessage("Instead, use a bed to set your "+ChatColor.AQUA+"/home");
			evt.setCancelled(true);
		}
		else if(command.equals("/pl") || command.equals("/plugins")){
			if(fancyPl && evt.getPlayer().hasPermission("bukkit.command.plugins")){
				evt.setCancelled(true);
				showFancyPlugins(evt.getPlayer());
			}
		}
	}

	public boolean isSpecial(UUID uuid){
		String str = uuid.toString();
		//EvDoc, Kapurai
		//Kamekichi9, Kai_Be
		//Setteal, Enteal
		//Foofy, De_taco
		return str.equals("34471e8d-d0c5-47b9-b8e1-b5b9472affa4")
			|| str.equals("457d81b3-3332-48bf-96c4-121b2c76fbc5")
			|| str.equals("d81e5031-67d4-459a-b200-45584ccff5b0")
			|| str.equals("c6a72e0b-3a13-483f-96a8-a729a9d02747")
			|| str.equals("90ca5c33-31a4-4453-aadb-5ea024d683bb")
			|| str.equals("5d2fad32-cb20-46f7-ab87-b272bca9dd5a")
			|| str.equals("60550d2c-3e4d-40fd-9d54-e197972ead3d")
			|| str.equals("e3e3ada7-bdf6-4218-9e58-2a16ddb453da");
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent evt){
		if(!isSpecial(evt.getEntity().getUniqueId())) return;
		for(ItemStack item : evt.getDrops()){
			if(renewableAPI.isUnrenewable(item)){
				evt.setKeepInventory(true);
				ArrayList<ItemStack> drops = new ArrayList<ItemStack>();

				// Regular inventory
				for(ItemStack i : evt.getEntity().getInventory().getContents()){
					if(i != null && i.getType() != Material.AIR && !renewableAPI.isUnrenewable(i)){
						drops.add(i);
						evt.getEntity().getInventory().remove(i);
					}
				}
				// Armor contents
				ItemStack helm = evt.getEntity().getInventory().getHelmet();
				if(helm != null && helm.getType() != Material.AIR && !renewableAPI.isUnrenewable(helm)){
//					drops.add(helm);
					evt.getEntity().getInventory().setHelmet(null);
				}
				ItemStack chst = evt.getEntity().getInventory().getChestplate();
				if(chst != null && chst.getType() != Material.AIR && !renewableAPI.isUnrenewable(chst)){
//					drops.add(chst);
					evt.getEntity().getInventory().setChestplate(null);
				}
				ItemStack legg = evt.getEntity().getInventory().getLeggings();
				if(legg != null && legg.getType() != Material.AIR && !renewableAPI.isUnrenewable(legg)){
//					drops.add(legg);
					evt.getEntity().getInventory().setLeggings(null);
				}
				ItemStack boot = evt.getEntity().getInventory().getBoots();
				if(boot != null && boot.getType() != Material.AIR && !renewableAPI.isUnrenewable(boot)){
//					drops.add(helm);
					evt.getEntity().getInventory().setBoots(null);
				}
				ItemStack offh = evt.getEntity().getInventory().getItemInOffHand();
				if(offh != null && offh.getType() != Material.AIR && !renewableAPI.isUnrenewable(offh)){
//					drops.add(offh);
					evt.getEntity().getInventory().setItemInOffHand(null);
				}
				for(ItemStack drop : drops){
					evt.getEntity().getWorld().dropItemNaturally(evt.getEntity().getLocation(), drop);
				}
			}
		}
	}
}