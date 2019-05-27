package Eventials;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.Vector;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import Eventials.economy.Economy;
import net.ess3.api.IEssentials;
import net.evmodder.EvLib.hooks.EssEcoHook;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.extras.TextUtils.TextAction;

public class Extras{
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
			try{userBal = EssEcoHook.getBalance(p);}catch(Exception e){userBal=0;}
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
			try{Economy.getEconomy().chargeServer(EssEcoHook.getBalance(player));}
			catch(Exception e){e.printStackTrace();}
			
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
			try{Economy.getEconomy().payServer(EssEcoHook.getBalance(player));}

//			// Alternatively, transfer money to server before deleting profile
//			try{Economy.getEconomy().playerToServer(uuid, VaultHook.getBalance(player));}
			catch(Exception e1){}

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
		TextAction[] actions = new TextAction[warps.size()]; actions[0] = TextAction.RUN_CMD;
		for(int i=1; i<warps.size(); ++i){
			preMsgs[i] = ChatColor.GOLD+", ";
			hyperMsgs[i] = ChatColor.GREEN+warps.get(i);
			cmdMsgs[i] = "/warp "+warps.get(i);
			actions[i] = TextAction.RUN_CMD;
		}
		TextUtils.sendModifiedText(preMsgs, hyperMsgs, actions, cmdMsgs, null, player);
	}

	public static ChatColor redGreenTest(String pluginName){
		Plugin plugin = Eventials.getPlugin().getServer().getPluginManager().getPlugin(pluginName);
		return (plugin != null && plugin.isEnabled()) ? ChatColor.GREEN : ChatColor.RED;
	}

	public static void showFancyPlugins(Player player){//TODO: Move to EventAndMisc.AlternateNew
		String raw = TextUtils.TextAction.parseToRaw(
				"Plugins: §a\\" +
				redGreenTest("OpenTerrainGenerator")+"OTG=>Open Terrain Generator (custom terrain)§r, §a\\" +
				redGreenTest("Renewable")+"Renewable=>Prevents unrenewable items from being destroyed§r, §a\\" +
				redGreenTest("Essentials")+"Essentials=>Collection of useful commands§r, §a\\" +
				redGreenTest("DropHeads")+"DropHeads=>Provides a chance to get heads from mobs/players§r, §a\\" +
				redGreenTest("Eventials")+"Eventials=>Package of custom-built tools, features, and tweaks§r, \\\\n§a\\" +
				redGreenTest("Factions")+"Factions=>Protect your land and build communities§r, §a\\" +
				redGreenTest("HorseOwners")+"HorseOwners=>Claim, name, teleport, and view stats for horses§r, §a\\" +
				redGreenTest("ChatManager")+"ChatManager=>Keeps chat clean + Color/Format for chat & signs§r, §a\\" +
				redGreenTest("EnchantBook")+"EnchantBook=>Color with anvils, looting on axes, etc!§r, §a\\" +
				"More=>\\"+
				redGreenTest("WorldEdit")+"WorldEdit\\§f, \\" +
				redGreenTest("WorldGuard")+"WorldGuard\\§f, \\" +
				redGreenTest("PluginLoader")+"PluginLoader\\§f, \\" +
				redGreenTest("EssentialsSpawn")+"EssentialsSpawn\\§f, \\" +
				redGreenTest("Votifier")+"Votifier\\§f, \\" +
				redGreenTest("EssentialsChat")+"EssentialsChat\\§f, \\" +
				redGreenTest("BungeeTabListPlus")+"BungeeTabListPlus\\§f, \\" +
				redGreenTest("PermissionsBukkit")+"PermissionsBukkit§r.\\\\n" +
				"\\§7\\§oHover over a plugin to see more details!",
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