package Extras;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.earth2me.essentials.User;
import com.earth2me.essentials.api.UserDoesNotExistException;
import EvLib.UsefulUtils;
import EvLib.VaultHook;
import Eventials.Eventials;
import Eventials.economy.Economy;
import Extras.Text.TextAction;
import net.ess3.api.IEssentials;

public class Extras {
	//used for the fancy /help
	static Map<String, String[]> pluginCommands;//map<command name, String[]{permission, description}>
	public static void loadFancyHelp(Plugin p){
		//load commands from all plugins
		pluginCommands = new HashMap<String, String[]>();
		for(Plugin plugin : p.getServer().getPluginManager().getPlugins()){
			if(plugin.getDescription().getCommands() == null) continue;

			for(String cmdName : plugin.getDescription().getCommands().keySet()){
				PluginCommand cmd = plugin.getServer().getPluginCommand(cmdName);
				if(cmd != null) pluginCommands.put(cmdName, new String[]{
					(cmd.getPermission() != null ? cmd.getPermission() : plugin.getName().toLowerCase()+'.'+cmdName),
					cmd.getDescription()
				});
			}
		}
	}

	public static void runPlayerDelete(){
		Eventials pl = Eventials.getPlugin();
		int cutoffDays = pl.getConfig().getInt("inactive-days-until-player-delete", 200);
//		int ifHasAdvs = pl.getConfig().getInt("keep-if-has-x-advancements", 50);
		double START_BAL = pl.getConfig().getDouble("starting-balance");
		boolean ifAboveStartingBal = pl.getConfig().getBoolean("keep-if-above-starting-balance", true);
		boolean ifLongtime = pl.getConfig().getBoolean("keep-if-time-played-outweighs-time-inactive", true);
		boolean ifWhitelisted = pl.getConfig().getBoolean("keep-if-whitelisted", true);
		boolean ifOp = pl.getConfig().getBoolean("keep-if-op", true);

		long now = new GregorianCalendar().getTimeInMillis();
		for(OfflinePlayer p : pl.getServer().getOfflinePlayers()){
			long timeInactive = now - p.getLastPlayed();
			double userBal;
			try{userBal = VaultHook.getBalance(p);}catch(UserDoesNotExistException e){userBal=0;}
			if(timeInactive/(24*60*60*1000) < cutoffDays
					|| (ifLongtime && timeInactive > (p.getLastPlayed()-p.getFirstPlayed()))
					|| (ifOp && p.isOp()) || (ifWhitelisted && p.isWhitelisted())
					|| (ifAboveStartingBal && userBal > START_BAL)
				/*	|| UsefulUtils.getVanillaAdvancements(p).length > ifHasAdvs*/)//TODO: Make this work!!
			{
				continue;
			}
			pl.getServer().broadcastMessage(p.getName()+" has been offline for "+cutoffDays+"+ days. Clearing stats");
			deletePlayer(p);
		}
	}

	public static boolean undeletePlayer(OfflinePlayer player){
		Eventials pl = Eventials.getPlugin();
		String playerName = player.getName();
		UUID uuid = player.getUniqueId();
		
		StringBuilder returnMsg = new StringBuilder(ChatColor.GRAY+
				"\n-----------------------------------------------------\n");
		File delFolder = new File("./plugins/EvFolder/DELETED/"+playerName);
		if(!delFolder.exists()) return false;
		
		// Restore world data
		File file;
		for(World world : pl.getServer().getWorlds()){
			file = new File(delFolder.getPath()+'/'+world.getName()+"-playerdata-UUID.dat");
			if(file.renameTo(new File("./"+world.getName()+"/playerdata/"+uuid+".dat"))){
				returnMsg.append(ChatColor.GRAY).append("Restored ").append(playerName)
						.append("'s playerdata for world ").append(ChatColor.YELLOW)
						.append(world.getName()).append('\n');
			}
			else returnMsg.append(ChatColor.RED).append("Failed to restore ").append(playerName)
						.append("'s playerdata for world ").append(ChatColor.YELLOW)
						.append(world.getName()).append('\n');

			file = new File(delFolder.getPath()+'/'+world.getName()+"-stats-UUID.json");
			if(file.renameTo(new File("./"+world.getName()+"/stats/"+uuid+".json"))){
				returnMsg.append(ChatColor.GRAY).append("Restored ").append(playerName)
						.append("'s world-stats data\n");
			}

			file = new File(delFolder.getPath()+'/'+world.getName()+"-advancements-UUID.json");
			if(file.renameTo(new File("./"+world.getName()+"/advancements/"+uuid+".json"))){
				returnMsg.append(ChatColor.GRAY).append("Restored ").append(playerName)
						.append("'s world-advancements data\n");
			}
		}

		// Restore Essentials data
		file = new File(delFolder.getPath()+'/'+uuid+".yml");
		if(file.exists()){
			if(file.renameTo(new File("./plugins/Essentials/userdata/"+file.getName()))){
				returnMsg.append(ChatColor.GRAY).append("Restored ")
						.append(playerName).append("'s Essentials userdata\n");
			}
			else returnMsg.append(ChatColor.GRAY).append("Failed to restore")
						.append(playerName).append("'s Essentials userdata\n");
			
			// Extract money directly from server bal
			try{Economy.getEconomy().chargeServer(VaultHook.getBalance(player));}
			catch(UserDoesNotExistException e){e.printStackTrace();}
			
//			//send money from server to player after restoring profile
//			try{Economy.getEconomy().serverToPlayer(uuid, VaultHook.getBalance(player));}
//			catch(UserDoesNotExistException e1){}
			
			Economy.getEconomy().updateBalance(uuid, player.isOnline());
		}
		else returnMsg.append(ChatColor.RED).append("Unable to find ").append(playerName)
					.append("'s Essentials userdata!\n");
		returnMsg.append(ChatColor.GRAY).append("-----------------------------------------------------\n")
			.append(ChatColor.GOLD).append("All stats Restored for ").append(playerName).append('!');
		pl.getServer().getConsoleSender().sendMessage(returnMsg.toString());
		return true;
	}

	public static boolean deletePlayer(OfflinePlayer player){
		Eventials pl = Eventials.getPlugin();
		String playerName = player.getName();
		UUID uuid = player.getUniqueId();
		
		StringBuilder returnMsg = new StringBuilder(ChatColor.GRAY+
				"\n-----------------------------------------------------\n");
		File delFolder = new File("./plugins/EvFolder/DELETED/"+playerName);
		if(!delFolder.mkdir()){
			new File("./plugins/EvFolder/DELETED").mkdir();
			delFolder.mkdir();
		}
		int deleted = 0;
		
		// Remove world data
		File file;
		for(World world : pl.getServer().getWorlds()){
			file = new File("./"+world.getName()+"/playerdata/"+uuid+".dat");
			if(file.renameTo(new File(delFolder.getPath()+'/'+world.getName()+"-playerdata-UUID.dat"))){
				returnMsg.append(ChatColor.GRAY+"Deleted ").append(playerName)
						.append("'s playerdata for world ").append(ChatColor.YELLOW)
						.append(world.getName()).append('\n');
				++deleted;
			}
			else returnMsg.append(ChatColor.RED+"Failed to delete ").append(playerName)
						.append("'s playerdata for world ").append(ChatColor.YELLOW)
						.append(world.getName()).append('\n');

			file = new File("./"+world.getName()+"/stats/"+uuid+".json");
			if(file.renameTo(new File(delFolder.getPath()+'/'+world.getName()+"-stats-UUID.json"))){
				returnMsg.append(ChatColor.GRAY).append("Deleted ").append(playerName)
						.append("'s world-stats data\n");
				++deleted;
			}

			file = new File("./"+world.getName()+"/advancements/"+uuid+".json");
			if(file.renameTo(new File(delFolder.getPath()+'/'+world.getName()+"-advancements-UUID.json"))){
				returnMsg.append(ChatColor.GRAY).append("Deleted ").append(playerName)
						.append("'s world-advancements data\n");
				++deleted;
			}
			// Only 1 world (usually) saves world-stats
//			else returnMsg.append(ChatColor.RED).append("Failed to delete ").append(playerName)
//						.append("'s world-stats data!\n");
		}

		Economy.getEconomy().removeBalance(uuid);
		
		// Remove Essentials data
		file = new File("./plugins/Essentials/userdata/"+uuid+".yml");
		if(file.exists()){
			// Add balance directly to server
			try{Economy.getEconomy().payServer(VaultHook.getBalance(player));}

//			// Alternatively, transfer money to server before deleting profile
//			try{Economy.getEconomy().playerToServer(uuid, VaultHook.getBalance(player));}
			catch(UserDoesNotExistException e1){}
			
			if(file.renameTo(new File(delFolder.getPath()+'/'+"Essentials-userdata-UUID.yml"))){
				returnMsg.append(ChatColor.GRAY).append("Deleted ")
						.append(playerName).append("'s Essentials userdata\n");
				++deleted;
			}
			else returnMsg.append(ChatColor.GRAY).append("Failed to delete")
						.append(playerName).append("'s Essentials userdata\n");
		}
		else returnMsg.append(ChatColor.RED).append("Unable to find ").append(playerName)
					.append("'s Essentials userdata!\n");
		returnMsg.append(ChatColor.GRAY).append("-----------------------------------------------------\n")
			.append(ChatColor.GOLD).append("All stats Cleared for ").append(playerName).append('!');
		pl.getServer().getConsoleSender().sendMessage(returnMsg.toString());
		return deleted > 0;
	}

	public static int clearEntitiesByWorld(World world,
			boolean hostile, boolean animals, boolean complex, boolean nonliving, boolean named, boolean ignoreNearby){
		int killCount = 0;
		
		if(world == null){
			for(World w : Bukkit.getServer().getWorlds())
				killCount += clearEntitiesByWorld(w, hostile, animals, complex, nonliving, named, ignoreNearby);

			return killCount;
		}
		for(Entity entity : (!nonliving && !animals ? world.getEntitiesByClass(Monster.class) :
							(!nonliving ? world.getLivingEntities() : world.getEntities()))
		){
			if(ignoreNearby){
				boolean near = false;
				for(Player p : Bukkit.getServer().getOnlinePlayers()){
					if(UsefulUtils.notFar(p.getLocation(), entity.getLocation())){
						near = true;
						break;
					}
				}
				if(near) continue;
			}
			if(complex == false){
				EntityType type = entity.getType();

				if(entity instanceof LivingEntity){
					LivingEntity le = (LivingEntity) entity;
					if(le.isLeashed() ||
							// 2.1 is a special number that signifies that the item is of foreign origin (picked up)
							le.getEquipment().getItemInMainHandDropChance() >= 1 ||
							le.getEquipment().getChestplateDropChance() >= 1 ||
							le.getEquipment().getLeggingsDropChance()	>= 1 ||
							le.getEquipment().getHelmetDropChance()		>= 1 ||
							le.getEquipment().getBootsDropChance()		>= 1) continue;
				}
				else if(type == EntityType.HORSE || type == EntityType.VILLAGER ||
						type == EntityType.GIANT || type == EntityType.ENDER_DRAGON ||
						type == EntityType.ENDER_CRYSTAL || type == EntityType.DROPPED_ITEM ||
						type == EntityType.FALLING_BLOCK || type == EntityType.ITEM_FRAME ||
						type == EntityType.LEASH_HITCH || type == EntityType.COMPLEX_PART ||
						type == EntityType.PAINTING || type == EntityType.OCELOT ||
						type == EntityType.WOLF || type == EntityType.FISHING_HOOK)
				{
					continue;
				}
			}

			//
			if(hostile && entity instanceof Monster && (named || ((LivingEntity)entity).getCustomName() == null)){
				entity.remove();
				++killCount;
			}
			//
			else if(animals && entity instanceof LivingEntity && (named || ((LivingEntity)entity).getCustomName() == null)){
				entity.remove();
				++killCount;
			}
			else if(nonliving && entity instanceof LivingEntity == false){
				entity.remove();
				++killCount;
			}
		}
		return killCount;
	}

	public static int clearEntitiesByChunk(Chunk chunk,
			boolean hostile, boolean animals, boolean complex, boolean nonliving, boolean named, boolean ignoreNearby){
		int killCount = 0;
		
		if(chunk == null) return -1;

		for(Entity entity : chunk.getEntities()){
			if(ignoreNearby){
				boolean near = false;
				for(Player p : Bukkit.getServer().getOnlinePlayers()){
					if(UsefulUtils.notFar(p.getLocation(), entity.getLocation())){
						near = true;
						break;
					}
				}
				if(near) continue;
			}
			if(complex == false){
				EntityType type = entity.getType();

				if(entity instanceof LivingEntity){
					LivingEntity le = (LivingEntity) entity;
					if(le.isLeashed() ||
							// 2.1 is a special number that signifies that the item is of foriegn origin (picked up)
							le.getEquipment().getItemInMainHandDropChance() >= 1 ||
							le.getEquipment().getChestplateDropChance() >= 1 ||
							le.getEquipment().getLeggingsDropChance()	>= 1 ||
							le.getEquipment().getHelmetDropChance()		>= 1 ||
							le.getEquipment().getBootsDropChance()		>= 1) continue;
				}
				else if(type == EntityType.HORSE || type == EntityType.VILLAGER ||
						type == EntityType.GIANT || type == EntityType.ENDER_DRAGON ||
						type == EntityType.ENDER_CRYSTAL || type == EntityType.DROPPED_ITEM ||
						type == EntityType.FALLING_BLOCK || type == EntityType.ITEM_FRAME ||
						type == EntityType.LEASH_HITCH || type == EntityType.COMPLEX_PART ||
						type == EntityType.PAINTING || type == EntityType.OCELOT ||
						type == EntityType.WOLF || type == EntityType.FISHING_HOOK)
				{
					continue;
				}
			}

			//
			if(hostile && entity instanceof Monster && (named || ((LivingEntity)entity).getCustomName() == null)){
				entity.remove();
				++killCount;
			}
			//
			else if(animals && entity instanceof LivingEntity && (named || ((LivingEntity)entity).getCustomName() == null)){
				entity.remove();
				++killCount;
			}
			else if(nonliving && entity instanceof LivingEntity == false){
				entity.remove();
				++killCount;
			}
		}
		return killCount;
	}

	public static void displayHyperWarps(Player player){
		Vector<String> warps = new Vector<String>();

//		warps.addAll(Arrays.asList("AdminShop","Commands","Creative",/*"Donor_perks",*/
//				"Downtown","Forest","Freebuild","Market",/*"minis_tirith",*/"Parkour",
//				/*"more_parkour","PvP_Arena",*/"Racetrack",/*"Spawn",*/"Suggestions"));

		IEssentials ess = (IEssentials) player.getServer().getPluginManager().getPlugin("Essentials");
		for(String warp : ess.getWarps().getList()){
			if(player.hasPermission("essentials.warps."+warp) ||
			ess.getPermissionsHandler().hasPermission(player, "essentials.warps."+warp)){
				warps.add(warp);
			}
		}
		if(warps.isEmpty()) return;
		String[] preMsgs = new String[warps.size()]; preMsgs[0] = ChatColor.GOLD+"HyperWarps: ";
		String[] hyperMsgs = new String[warps.size()]; hyperMsgs[0] = ChatColor.GREEN+warps.firstElement();
		String[] cmdMsgs = new String[warps.size()]; cmdMsgs[0] = "/warp "+warps.firstElement();
		TextAction[] actions = new TextAction[warps.size()]; actions[0] = TextAction.CMD;
		for(int i=1; i<warps.size(); ++i){
			preMsgs[i] = ChatColor.GOLD+", ";
			hyperMsgs[i] = ChatColor.GREEN+warps.get(i);
			cmdMsgs[i] = "/warp "+warps.get(i);
			actions[i] = TextAction.CMD;
		}
		Text.sendModifiedText(preMsgs, hyperMsgs, actions, cmdMsgs, null, player);
	}

	public static void showFancyHelp(CommandSender sender, int pageNum){
		User user = null;
		IEssentials essHook;
		if(sender instanceof Player &&
				(essHook = (IEssentials)sender.getServer().getPluginManager().getPlugin("Essentials")) != null)
			user = essHook.getUser((Player)sender);

		List<String> commandNames = new ArrayList<String>();
		for(String cmdName : pluginCommands.keySet()){
			if(user == null || user.isAuthorized(pluginCommands.get(cmdName)[0])) commandNames.add(cmdName);
		}
		Collections.sort(commandNames);

		int totalPages = (int)((commandNames.size()-2)/9) + 1;
		if(pageNum > totalPages) pageNum = totalPages;

		//essentials-style help, minus the plugins.
		StringBuilder helpPage = new StringBuilder("").append(ChatColor.YELLOW).append(" ---- ")
				.append(ChatColor.GOLD).append("Help").append(ChatColor.YELLOW).append(" -- ").append(ChatColor.GOLD)
				.append("Page").append(ChatColor.RED).append(" ").append(pageNum).append(ChatColor.GOLD).append("/")
				.append(ChatColor.RED).append(totalPages).append(ChatColor.YELLOW).append(" ----");
		
		int i, startingVal = (pageNum-1)*9;
		for(i = startingVal; i < startingVal+9 && i < commandNames.size(); ++i){
			helpPage.append("\n").append(ChatColor.GOLD).append("/").append(commandNames.get(i)).append(ChatColor.WHITE)
					.append(": ").append(pluginCommands.get(commandNames.get(i))[1]);
		}
		if(pageNum != totalPages){
			helpPage.append("\n").append(ChatColor.GOLD).append("Type ").append(ChatColor.RED).append("/help ")
					.append(pageNum+1).append(ChatColor.GOLD).append(" to read the next page.");
		}
		else if(i < commandNames.size()){
			helpPage.append("\n").append(ChatColor.GOLD).append("/").append(commandNames.get(i)).append(ChatColor.WHITE)
					.append(": ").append(pluginCommands.get(commandNames.get(i))[1]);
		}
		sender.sendMessage(helpPage.toString());
	}

	public static void showCommandHelp(CommandSender sender, Command cmd){
		sender.sendMessage(new StringBuilder(ChatColor.GOLD+"Help for command ")
			.append(ChatColor.RED).append(cmd.getName()).append(ChatColor.GOLD).append(":\n").append(ChatColor.GOLD)

			.append("Description: ").append(ChatColor.WHITE).append(cmd.getDescription()).append('\n').append(ChatColor.GOLD)
			.append("Usage: ").append(ChatColor.WHITE).append(cmd.getUsage()).append('\n').append(ChatColor.GOLD)
			.append("Aliases: ").append(ChatColor.WHITE).append(cmd.getAliases()).append('\n').append(ChatColor.GOLD)
			.append("Permission: ").append(ChatColor.WHITE).append(cmd.getPermission())
			.toString());
	}

	public static String redIfDisabled(String pluginName){
		if(Eventials.getPlugin().getServer().getPluginManager().getPlugin(pluginName).isEnabled()) return "";
		else return ""+ChatColor.RED;
	}

	public static void showFancyPlugins(Player player){//TODO: Move to EventAndMisc.AlternateNew
		String raw = Text.TextAction.parseToRaw(
				"Plugins: §a" +
				redIfDisabled("OpenTerrainGenerator")+"OTG=>Open Terrain Generator (custom terrain)§r, §a" +
				redIfDisabled("Renewable")+"Renewable=>Prevents unrenewable items from being destroyed§r, §a" +
				redIfDisabled("Essentials")+"Essentials=>Collection of useful commands§r, §a" +
				redIfDisabled("DropHeads")+"DropHeads=>Provides a chance to get heads from mobs/players§r, §a" +
				redIfDisabled("Eventials")+"Eventials=>Package of custom-built tools, features, and tweaks§r, \n§a" +
				redIfDisabled("Factions")+"Factions=>Protect your land and build communities§r, §a" +
				redIfDisabled("HorseOwners")+"HorseOwners=>Claim, name, teleport, and view stats for horses§r, §a" +
				redIfDisabled("ChatManager")+"ChatManager=>Keeps chat clean + Color/Format for chat & signs§r, §a" +
				redIfDisabled("EnchantBook")+"EnchantBook=>Color with anvils, looting on axes, etc!§r, §a" +
				"More=>\\§a"+
				redIfDisabled("WorldEdit")+"WorldEdit\\§f, \\§a" +
				redIfDisabled("WorldGuard")+"WorldGuard\\§f, \\§a" +
				redIfDisabled("PluginLoader")+"PluginLoader\\§f, \\§a" +
				redIfDisabled("EssentialsSpawn")+"EssentialsSpawn\\§f, \\§a" +
				redIfDisabled("Votifier")+"Votifier\\§f, \\§a" +
				redIfDisabled("EssentialsChat")+"EssentialsChat\\§f, \\§a" +
				redIfDisabled("BungeeTabListPlus")+"BungeeTabListPlus\\§f, \\§a" +
				redIfDisabled("PermissionsBukkit")+"PermissionsBukkit§r.\n" +
				"§7§oHover over a plugin to see more details!",
				"§r"
		);
		Eventials.getPlugin().runCommand("tellraw "+player.getName()+' '+raw);
//		Eventials.getPlugin().getLogger().info("Raw="+raw);
	}

	public static boolean isAdminShop(Block block){
		return false;
	}
	public static void setAdminShop(Block block, boolean newState){
		;
	}
}