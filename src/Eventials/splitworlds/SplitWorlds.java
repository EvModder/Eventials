package Eventials.splitworlds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import Eventials.Eventials;
import com.google.common.io.Files;

public final class SplitWorlds{
	final private Eventials plugin;
	final private HashMap<String, String> sharedInvWorlds;
	final String DEFAULT_WORLD;
	final String DEFAULT_PLAYERDATA;
	final static String SKIP_TP_INV_CHECK = "skipTeleportInvCheck";
	final boolean removeDisease;

	public SplitWorlds(Eventials pl){
		plugin = pl;
		removeDisease = pl.getConfig().getBoolean("vaccinate-players", true);

		Properties properties = new Properties();
		try{
			final FileInputStream in = new FileInputStream("./server.properties");
			properties.load(in); in.close();
		}
		catch(IOException ex){
			plugin.getLogger().severe("Unable to read server.properties file!");
		}
		DEFAULT_WORLD = properties.getProperty("level-name");
		DEFAULT_PLAYERDATA = "./"+DEFAULT_WORLD+"/playerdata/";

		sharedInvWorlds = new HashMap<String, String>();
		ConfigurationSection worldSettings = plugin.getConfig().getConfigurationSection("shared-inv-worlds");
		if(worldSettings != null){
			List<String> worldNames = new ArrayList<String>();
			plugin.getServer().getWorlds().forEach(w->worldNames.add(w.getName()));// yay lambdas!
			final HashSet<String> primaryKeys1 = new HashSet<String>();
			for(String w : worldNames) if(new File(getPlayerdataFolder(w, true)).exists()) primaryKeys1.add(w);
			final UnionFind<String> ufind = new UnionFind<String>();

			final HashSet<String> primaryKeys2 = new HashSet<String>();
			for(String groupName : worldSettings.getKeys(false)){
				List<String> groupWorlds = worldSettings.getStringList(groupName);
				primaryKeys2.add(groupWorlds.get(0));
				ufind.insertSets(SplitWorldUtils.findMatchGroups(worldNames, groupWorlds, false));
			}
			for(List<String> group : ufind.getSets()){
				String pKey = null;
				for(String s : group){
					if(primaryKeys1.contains(s)){
						if(primaryKeys2.contains(pKey = s)) break;
					}
					else if(pKey == null && primaryKeys2.contains(s)) pKey = s;
				}
				if(pKey == null) pKey = group.get(0);
				for(String s : group) sharedInvWorlds.put(s, pKey);
			}
			for(String world : worldNames){
				if(!sharedInvWorlds.containsKey(world)) sharedInvWorlds.put(world, world);
			}
		}

		plugin.getServer().getPluginManager().registerEvents(new TeleportListener(this), plugin);
		plugin.getServer().getPluginManager().registerEvents(new RespawnListener(this), plugin);
		new CommandEnderchest(plugin, this);
		new CommandInvsee(plugin, this);
	}

	public boolean inSharedInvGroup(String world1, String world2){
		return sharedInvWorlds.containsKey(world1) &&
				sharedInvWorlds.get(world1).equals(sharedInvWorlds.get(world2));
	}

	public String getInvGroup(String worldName){
		return sharedInvWorlds.containsKey(worldName) ? sharedInvWorlds.get(worldName) : worldName;
	}

	public String getPlayerdataFolder(String worldName, boolean ignoreShared){
		String mainWorld = sharedInvWorlds.get(worldName);
		if(ignoreShared || mainWorld == null || mainWorld.equals(worldName)){
			return "./"+worldName+"/playerdata/";
		}
		return "./"+mainWorld+"/playerdata/";
	}

	public File getPlayerdata(UUID playerUUID, String worldName, boolean ignoreShared, boolean staticSource){
		String useWorld = ignoreShared ? worldName : sharedInvWorlds.get(worldName);
		if(useWorld == null) useWorld = worldName;

		return (!staticSource && inSharedInvGroup(useWorld, getCurrentInvGroup(playerUUID))) ?
				getMainPlayerdata(playerUUID)
				: new File("./" + useWorld +
						(useWorld.equals(DEFAULT_WORLD) ? "/playerdata_"+DEFAULT_WORLD+"/" : "/playerdata/")
						+ playerUUID + ".dat");
/*		// Logically equivalent
		String subfolder = useWorld.equals(DEFAULT_WORLD) ? "/playerdata_"+DEFAULT_WORLD+"/" : "/playerdata/";
		if(staticSource){
			return new File("./" + useWorld + subfolder + playerUUID + ".dat");
		}
		else{
			if(inSharedInvGroup(useWorld, getCurrentInvGroup(playerUUID))) return getMainPlayerdata(playerUUID);
			else return new File("./" + useWorld + subfolder + playerUUID + ".dat");
		}*/
	}

	public File getMainPlayerdata(UUID playerUUID){
		File dataFile = new File(DEFAULT_PLAYERDATA + playerUUID + ".dat");
		return dataFile;
	}

	boolean loadProfile(Player handler, UUID fromPlayer, String fromWorld, boolean flexible){
		if(flexible && handler.getUniqueId().equals(fromPlayer) &&
				inSharedInvGroup(handler.getWorld().getName(), fromWorld)) return true;

		File currentFile = getMainPlayerdata(handler.getUniqueId());
		File sourceFile = getPlayerdata(fromPlayer, fromWorld, false, !flexible);
		if(sourceFile == null || !sourceFile.exists() || currentFile == null){
			plugin.getLogger().warning("Unable to load profile from world: "+fromWorld);
			return false;
		}

		try{Files.copy(sourceFile, currentFile);}
		catch(IOException e){e.printStackTrace(); return false;}

		handler.loadData();
		if(removeDisease) SplitWorldUtils.resetPlayer(handler); // Remove disease AFTER loading data (treat infected file)

		// This file provides a means to figure out what sharedInv group an OfflinePlayer is in
		File currentInv = new File(DEFAULT_PLAYERDATA + handler.getUniqueId() + ".group");
		try{Files.write(sharedInvWorlds.get(fromWorld)+" "+fromPlayer, currentInv, Charset.defaultCharset());}
		catch(IOException e){e.printStackTrace();}
		return true;
	}

	boolean saveProfile(Player handler, UUID toPlayer, String toWorld, boolean flexible){
		if(flexible && handler.getUniqueId().equals(toPlayer) &&
				!inSharedInvGroup(handler.getWorld().getName(), toWorld)) return false;
		File currentFile = getMainPlayerdata(handler.getUniqueId());
		File destFile = getPlayerdata(toPlayer, toWorld, false, !flexible);
		if(currentFile == null || !currentFile.exists() || destFile == null){
			plugin.getLogger().warning("Unable to save profile to world: "+toWorld);
			return false;
		}
		if(removeDisease) SplitWorldUtils.resetPlayer(handler);// Remove disease BEFORE saving data (vaccinate the file)
		handler.saveData();

		try{Files.move(currentFile, destFile);}
		catch(IOException e){e.printStackTrace(); return false;}
		return true;
	}

	boolean forceSaveProfile(Player player){
		return saveProfile(player, player.getUniqueId(), player.getWorld().getName(), false);
	}
	boolean forceLoadProfile(Player player){
		return loadProfile(player, player.getUniqueId(), player.getWorld().getName(), false);
	}

	@SuppressWarnings("unused") private boolean loadProfile0(Player player, String worldName){
		if(inSharedInvGroup(player.getWorld().getName(), worldName)) return false; // Already loaded

		File currentFile = getMainPlayerdata(player.getUniqueId());
		File sourceFile = getPlayerdata(player.getUniqueId(), worldName, false, true);
		if(sourceFile == null || !sourceFile.exists() || currentFile == null){
			plugin.getLogger().warning("Unable to load profile from world: "+worldName);
			return false;
		}

		try{Files.copy(sourceFile, currentFile);}
		catch(IOException e){e.printStackTrace(); return false;}

		player.loadData();
		if(removeDisease) SplitWorldUtils.resetPlayer(player); // Remove disease AFTER loading data (treat infected file)

		// This file provides a means to figure out what sharedInv group an OfflinePlayer is in
		File currentGroup = new File(DEFAULT_PLAYERDATA + player.getUniqueId() + ".group");
		try{Files.write(sharedInvWorlds.get(worldName), currentGroup, Charset.defaultCharset());}
		catch(IOException e){e.printStackTrace();}
		return true;
	}

	@SuppressWarnings("unused") private boolean saveProfile0(Player player, String worldName){
		if(!inSharedInvGroup(player.getWorld().getName(), worldName)) return false; // Wrong world!
		File currentFile = getMainPlayerdata(player.getUniqueId());
		File destFile = getPlayerdata(player.getUniqueId(), worldName, false, true);
		if(currentFile == null || !currentFile.exists() || destFile == null){
			plugin.getLogger().warning("Unable to save profile to world: "+worldName);
			return false;
		}
		if(removeDisease) SplitWorldUtils.resetPlayer(player);// Remove disease BEFORE saving data (vaccinate the file)
		player.saveData();

		try{Files.move(currentFile, destFile);}
		catch(IOException e){e.printStackTrace(); return false;}
		return true;
	}

	public String getCurrentInvGroup(UUID playerUUID){
		File currentGroup = new File(DEFAULT_PLAYERDATA + playerUUID + ".group");
		if(currentGroup == null || !currentGroup.exists()) return DEFAULT_WORLD;
		try{return sharedInvWorlds.get(Files.readFirstLine(currentGroup, Charset.defaultCharset()));}
		catch(IOException e){e.printStackTrace(); return null;}
	}

	@Deprecated boolean switchToInv(Player player, String worldFrom, String worldTo){
		if(inSharedInvGroup(worldFrom, worldTo)) return true;

		if(saveProfile(player, player.getUniqueId(), worldFrom, true)){
			if(loadProfile(player, player.getUniqueId(), worldTo, true)){
				//TODO: test if this is actually a thing
				GameMode gm = player.getGameMode();// fix gamemode glitch
				player.setGameMode(GameMode.SPECTATOR);
				player.setGameMode(gm);
				return true;
			}
		}
		return false;
	}

	public boolean transInvWorldTp(final Player player, final Location from, final Location to){
		String worldFrom = from.getWorld().getName();
		String worldTo = to.getWorld().getName();
		if(inSharedInvGroup(worldFrom, worldTo)) return true;

		// Save inventory from current world
		if(saveProfile(player, player.getUniqueId(), worldFrom, true) == false) return false;

		// Sterilize player's inventory while their new file data is being loaded
		ItemStack[] oldInv = player.getInventory().getContents();
		ItemStack[] oldArmor = player.getInventory().getArmorContents();
		player.getInventory().clear();

		// Send them (empty-handed) to the destination world
		if(SplitWorldUtils.untrackedTeleport(player, to, true) == false){// Failed to teleport; reload their old inventory
			player.getInventory().setContents(oldInv);
			player.getInventory().setArmorContents(oldArmor);
			return false;
		}

		// Load the new world inventory
		if(loadProfile(player, player.getUniqueId(), worldTo, true) == false) return false;

		//TODO: test if this is actually a thing
		GameMode gm = player.getGameMode();// fix gamemode glitch
		player.setGameMode(GameMode.SPECTATOR);
		player.setGameMode(gm);

		// Teleport (again) to destination, in case loadProfile() changed the player's location
		SplitWorldUtils.untrackedTeleport(player, to, true);
		return true;
	}
}