package Eventials.splitworlds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib.EvPlugin;
import Eventials.Eventials;

public final class SplitWorlds{
	final private EvPlugin plugin;
	private static HashMap<String, String> sharedInvWorlds;
	private static String DEFAULT_WORLD, DEFAULT_PLAYERDATA;
	public static String getDefaultWorld(){return DEFAULT_WORLD;}
	private static boolean SINGLE_INV_GROUP;
	final static String SKIP_TP_INV_CHECK_TAG = "skipTeleportInvCheck";

	static String loadDefaultWorldName(Logger logger){
		Properties properties = new Properties();
		try{
			final FileInputStream in = new FileInputStream("./server.properties");
			properties.load(in); in.close();
		}
		catch(IOException ex){
			logger.severe("Unable to read server.properties file!");
		}
		return properties.getProperty("level-name");
	}
	public static void loadSharedInvMap(ConfigurationSection worldSettings, Logger logger){
		sharedInvWorlds = new HashMap<>();
		List<String> worldNames = new ArrayList<>();
		org.bukkit.Bukkit.getWorlds().forEach(w -> worldNames.add(w.getName()));// yay lambdas!
		final HashSet<String> primaryKeys1 = new HashSet<>();
		for(String w : worldNames){
			File playerdataFolder = new File("./" + w + "/playerdata/");
			if(playerdataFolder.exists() && playerdataFolder.list().length > 0) primaryKeys1.add(w);
		}
		final UnionFind<String> ufind = new UnionFind<>();

		final HashSet<String> primaryKeys2 = new HashSet<>();
		for(String groupName : worldSettings.getKeys(false)){
			List<String> groupWorlds = worldSettings.getStringList(groupName);
			logger.fine("World group primary (in config): " + groupWorlds.get(0));
			primaryKeys2.add(groupWorlds.get(0));
			ufind.insertSets(SplitWorldUtils.findMatchGroups(worldNames, groupWorlds));
		}
		int numGroups = 0;
		for(List<String> group : ufind.getSets()){
			Collections.sort(group, Comparator.comparing(String::length));
			String pKey1 = null, pKey2 = null;
			for(String s : group){
				if(s.equalsIgnoreCase(DEFAULT_WORLD)){pKey1 = s; break;}// primaryKey0
				if(primaryKeys1.contains(s)){
					if(pKey1 != null){
						logger.warning("SharedInvGroup contains multiple worlds that have a /playerdata/ folder");
						if(pKey1.equals(pKey2)){
							if(primaryKeys2.contains(s) && s.length() < pKey1.length()) pKey1 = pKey2 = s;
						}
						else if(primaryKeys2.contains(pKey1 = s)) pKey2 = s;
					}
				}
				else if(pKey1 == null && primaryKeys2.contains(s)) pKey2 = s;
			}
			final String primaryWorld = pKey1 != null ? pKey1 : pKey2 != null ? pKey2 : group.get(0);
			//if(group.size() == worldNames.size()) logger.info("SplitWorlds: All worlds using same inventory");//also implies SINGLE_INV_GROUP=true
			//else
				logger.info("SharedInvGroup: [" + primaryWorld + "]->(" + String.join(",", group.toArray(new String[0])) + ")");
			++numGroups;
			for(String s : group) sharedInvWorlds.put(s, primaryWorld);
		}
		for(String world : worldNames){
			if(sharedInvWorlds.putIfAbsent(world, world) == null){
				++numGroups;
				logger.info("SharedInvGroup: [" + world + "]");
			}
		}
		SINGLE_INV_GROUP = numGroups == 1;
	}

	public static void minimal_init(EvPlugin pl){
		DEFAULT_WORLD = loadDefaultWorldName(pl.getLogger());
		DEFAULT_PLAYERDATA = "./"+DEFAULT_WORLD+"/playerdata/";
		pl.getLogger().fine("Default playerdata location: "+DEFAULT_PLAYERDATA);

		if(pl.getConfig().isConfigurationSection("shared-inv-worlds")){
			loadSharedInvMap(pl.getConfig().getConfigurationSection("shared-inv-worlds"), pl.getLogger());
		}
		else{
			ConfigurationSection defaultGroups = new YamlConfiguration();
			defaultGroups.set("default_group", Arrays.asList("*", "*_nether", "*_the_end"));
			loadSharedInvMap(defaultGroups, pl.getLogger());
		}
		if(SINGLE_INV_GROUP == false){
			File default_world_temp_data = new File("./"+DEFAULT_WORLD+"/playerdata_"+DEFAULT_WORLD+"/");
			if(!default_world_temp_data.exists()) default_world_temp_data.mkdir();
		}
		else pl.getLogger().fine("Only one inv group found, using 'simple settings'");
	}
	public SplitWorlds(EvPlugin pl){
		plugin = pl;
		minimal_init(pl);

		plugin.getServer().getPluginManager().registerEvents(new TeleportListener(this), plugin);
		plugin.getServer().getPluginManager().registerEvents(new RespawnListener(), plugin);
		new CommandEnderchest(plugin);
		new CommandInvsee(plugin);
	}

	// If no group defined, group with the default world. TODO: config setting with alternative: getOrDefault(worldName, worldName)
	public static String getInvGroup(String worldName){
		return sharedInvWorlds.getOrDefault(worldName, DEFAULT_WORLD);
	}
	public static boolean inSharedInvGroup(String world1, String world2){
		return getInvGroup(world1).equals(getInvGroup(world2));
	}
	public static String getCurrentInvGroup(UUID playerUUID){
		final Path currentGroup = Paths.get(DEFAULT_PLAYERDATA + playerUUID + ".group");
		if(!currentGroup.toFile().exists()) return DEFAULT_WORLD;//'simple settings'
		try{return getInvGroup(Files.readString(currentGroup));}
		catch(IOException e){e.printStackTrace(); return "FAILURE VERY BAD OOF";}
	}

	public static File getCurrentPlayerdata(UUID playerUUID){
		return new File(DEFAULT_PLAYERDATA + playerUUID + ".dat");
	}
	public static File getPlayerdata(UUID playerUUID, String worldName, boolean useShared, boolean useCurrent){
		final String useWorld = useShared ? getInvGroup(worldName) : worldName;
		final boolean inDefaultWorld = useWorld.equals(DEFAULT_WORLD);
		return (useCurrent && inSharedInvGroup(useWorld, getCurrentInvGroup(playerUUID)))
				? getCurrentPlayerdata(playerUUID)
				: new File("./" + useWorld +
						((!SINGLE_INV_GROUP && inDefaultWorld) ? "/playerdata_"+DEFAULT_WORLD+"/" : "/playerdata/")
						+ playerUUID +
						// So we can save/load file temporarily for /invsee and /echest in 'simple settings'
						(SINGLE_INV_GROUP ? "_tmp.dat" : ".dat"));
	}

	static public boolean loadProfile(Player handler, UUID fromPlayer, String fromWorld, boolean useShared, boolean useCurrent, boolean copy){
		if(useCurrent && handler.getUniqueId().equals(fromPlayer) && inSharedInvGroup(handler.getWorld().getName(), fromWorld)){
			return false;
		}
		File currentFile = getCurrentPlayerdata(handler.getUniqueId());
		final File sourceFile = getPlayerdata(fromPlayer, fromWorld, useShared, useCurrent);
		if(sourceFile == null || !sourceFile.exists() || currentFile == null){
			Eventials.getPlugin().getLogger().warning("Unable to load source profile: "+sourceFile.getPath());
			Eventials.getPlugin().getLogger().warning("Into current profile: "+currentFile.getPath());
			return false;
		}
		try{
			if(copy) Files.copy(sourceFile.toPath(), currentFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			else Files.move(sourceFile.toPath(), currentFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch(IOException e){e.printStackTrace(); return false;}

		SplitWorldUtils.resetPlayerState(handler);
		handler.loadData();

		if(!SINGLE_INV_GROUP){
			// This file provides a means to figure out what sharedInv group an OfflinePlayer is in
			final Path groupFile = Paths.get(DEFAULT_PLAYERDATA + handler.getUniqueId() + ".group");
			try{Files.writeString(groupFile, getInvGroup(fromWorld));}
			catch(IOException e){e.printStackTrace();}
		}
		return true;
	}
	static public boolean saveProfile(Player handler, UUID toPlayer, String toWorld, boolean useShared, boolean useCurrent, boolean copy){
		if(useCurrent && handler.getUniqueId().equals(toPlayer) && !inSharedInvGroup(handler.getWorld().getName(), toWorld)){
			return false;
		}
		final File currentFile = getCurrentPlayerdata(handler.getUniqueId());
		final File destFile = getPlayerdata(toPlayer, toWorld, useShared, useCurrent);
		if(currentFile == null || !currentFile.exists() || destFile == null){
			Eventials.getPlugin().getLogger().warning("Unable to save current profile: "+currentFile.getPath());
			Eventials.getPlugin().getLogger().warning("Into destination profile: "+destFile.getPath());
			return false;
		}
		handler.saveData();

		try{
			if(copy) Files.copy(currentFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			else Files.move(currentFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch(IOException e){e.printStackTrace(); return false;}
		return true;
	}

	static public boolean loadProfile(Player handler, File sourceFile){
		if(sourceFile == null || !sourceFile.exists()){
			Eventials.getPlugin().getLogger().warning("Unable to load profile from file: "+sourceFile.getPath());
			return false;
		}
		final File currentFile = getCurrentPlayerdata(handler.getUniqueId());

		try{Files.copy(sourceFile.toPath(), currentFile.toPath(), StandardCopyOption.REPLACE_EXISTING);}
		catch(IOException e){e.printStackTrace(); return false;}

		SplitWorldUtils.resetPlayerState(handler);
		handler.loadData();

		return true;
	}
	static public boolean saveProfile(Player handler, File toFile){
		final File currentFile = getCurrentPlayerdata(handler.getUniqueId());
		if(toFile == null || !currentFile.exists()){
			Eventials.getPlugin().getLogger().warning("Unable to save profile to file: "+toFile.getPath());
			return false;
		}
		handler.saveData();

		try{Files.copy(currentFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);}
		catch(IOException e){e.printStackTrace(); return false;}
		return true;
	}

	static public boolean loadCurrentProfile(Player player){
		return loadProfile(player, player.getUniqueId(), player.getWorld().getName(), /*useShared=*/true, /*useCurrent=*/false, /*copy=*/false);
	}
	static public boolean saveCurrentProfile(Player player){
		return saveProfile(player, player.getUniqueId(), player.getWorld().getName(), /*useShared=*/true, /*useCurrent=*/false, /*copy=*/true);
	}

	public boolean transInvWorldTp(final Player player, final Location from, final Location to){
		final String worldFrom = from.getWorld().getName();
		final String worldTo = to.getWorld().getName();
		if(inSharedInvGroup(worldFrom, worldTo)) return true;

		// Save inventory from current world
		if(!saveProfile(player, player.getUniqueId(), worldFrom, true, false, true)) return false;

		// Sterilize player's inventory while their new file data is being loaded
		ItemStack[] oldInv = player.getInventory().getContents();
		ItemStack[] oldArmor = player.getInventory().getArmorContents();
		player.getInventory().clear();

		// Send them (empty-handed) to the destination world
		if(SplitWorldUtils.untrackedTeleport(player, to, true) == false){
			// Failed to teleport; reload their old inventory
			player.getInventory().setContents(oldInv);
			player.getInventory().setArmorContents(oldArmor);
			return false;
		}

		// Load the new world inventory
		if(!loadProfile(player, player.getUniqueId(), worldTo, true, false, true)) return false;

		//TODO: test if this is actually a thing
		GameMode gm = player.getGameMode();// fix gamemode glitch
		player.setGameMode(GameMode.SPECTATOR);
		player.setGameMode(gm);

		// Teleport (again) to destination, in case loadProfile() changed the player's location
		SplitWorldUtils.untrackedTeleport(player, to, true);
		return true;
	}
}