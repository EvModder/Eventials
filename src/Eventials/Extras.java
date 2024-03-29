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
import Eventials.economy.EvEconomy;
import net.ess3.api.IEssentials;
import net.evmodder.EvLib.hooks.EssEcoHook;
import net.evmodder.EvLib.extras.TellrawUtils.TextClickAction;
import net.evmodder.EvLib.extras.TellrawUtils.ClickEvent;
import net.evmodder.EvLib.extras.TellrawUtils.ListComponent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;

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
			try{EvEconomy.getEconomy().chargeServer(EssEcoHook.getBalance(player));}
			catch(Exception e){e.printStackTrace();}
			EvEconomy.getEconomy().updateBalance(uuid, player.isOnline());
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

		StringBuilder returnMsg = new StringBuilder(ChatColor.GRAY+"\n-----------------------------------------------------\n");
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

		EvEconomy.getEconomy().removeBalance(uuid);

		// Remove Essentials data
		file = new File("./plugins/Essentials/userdata/"+uuid+".yml");
		if(file.exists()){
			// Add balance directly to server
			try{EvEconomy.getEconomy().payServer(EssEcoHook.getBalance(player));}

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

	@SuppressWarnings("deprecation")
	public static void displayHyperWarps(Player player){
		Vector<String> warps = new Vector<>();

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
		
		ListComponent blob = new ListComponent();
		for(int i=0; i<warps.size(); ++i){
			if(i == 0) blob.addComponent(ChatColor.GOLD+"HyperWarps: ");
			else blob.addComponent(ChatColor.GOLD+", ");
			blob.addComponent(new RawTextComponent(ChatColor.GREEN+warps.get(i), new TextClickAction(ClickEvent.RUN_COMMAND, "/warp "+warps.get(i))));
		}
		Eventials.getPlugin().sendTellraw(player.getName(), blob.toString());
	}

	public static boolean isAdminShop(Block block){
		return false;
	}
	public static void setAdminShop(Block block, boolean newState){
		;
	}
}