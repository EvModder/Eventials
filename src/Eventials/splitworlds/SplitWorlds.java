package Eventials.splitworlds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
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
import org.apache.logging.log4j.util.Strings;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib.EvPlugin;
import com.google.common.io.Files;
import Eventials.Eventials;

public final class SplitWorlds{
	final private EvPlugin plugin;
	private static HashMap<String, String> sharedInvWorlds;
	private static String DEFAULT_WORLD, DEFAULT_PLAYERDATA;
	public static String getDefaultWorld(){return DEFAULT_WORLD;}
	private static boolean TREAT_DISEASE, SINGLE_INV_GROUP;
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
		sharedInvWorlds = new HashMap<String, String>();
		List<String> worldNames = new ArrayList<String>();
		org.bukkit.Bukkit.getWorlds().forEach(w -> worldNames.add(w.getName()));// yay lambdas!
		final HashSet<String> primaryKeys1 = new HashSet<String>();
		for(String w : worldNames){
			File playerdataFolder = new File("./" + w + "/playerdata/");
			if(playerdataFolder.exists() && playerdataFolder.list().length > 0) primaryKeys1.add(w);
		}
		final UnionFind<String> ufind = new UnionFind<String>();

		final HashSet<String> primaryKeys2 = new HashSet<String>();
		for(String groupName : worldSettings.getKeys(false)){
			List<String> groupWorlds = worldSettings.getStringList(groupName);
			logger.info("World group primary (in config): " + groupWorlds.get(0));
			primaryKeys2.add(groupWorlds.get(0));
			ufind.insertSets(SplitWorldUtils.findMatchGroups(worldNames, groupWorlds, false));
		}
		for(List<String> group : ufind.getSets()){
			Collections.sort(group, Comparator.comparing(String::length));
			String pKey1 = null, pKey2 = null;
			for(String s : group){
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
			String primaryWorld = pKey1 != null ? pKey1 : pKey2 != null ? pKey2 : group.get(0);
			logger.info("SharedInvGroup: [" + primaryWorld + "] -> (" + Strings.join(group, ',') + ")");
			for(String s : group)
				sharedInvWorlds.put(s, primaryWorld);
		}
		for(String world : worldNames){
			if(!sharedInvWorlds.containsKey(world)) sharedInvWorlds.put(world, world);
		}
		SINGLE_INV_GROUP = sharedInvWorlds.size() == 1;
	}

	public static void minimal_init(EvPlugin pl){
		TREAT_DISEASE = pl.getConfig().getBoolean("vaccinate-players", true);

		DEFAULT_WORLD = loadDefaultWorldName(pl.getLogger());
		DEFAULT_PLAYERDATA = "./"+DEFAULT_WORLD+"/playerdata/";

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
		else pl.getLogger().info("Only one inv group found, using 'simple settings'");
	}
	public SplitWorlds(EvPlugin pl){
		plugin = pl;
		minimal_init(pl);

		plugin.getServer().getPluginManager().registerEvents(new TeleportListener(this), plugin);
		plugin.getServer().getPluginManager().registerEvents(new RespawnListener(), plugin);
		new CommandEnderchest(plugin, this);
		new CommandInvsee(plugin, this);
	}

	public static String getInvGroup(String worldName){
		String mainWorldName = sharedInvWorlds.get(worldName);
		return mainWorldName != null ? mainWorldName : worldName;
	}
	public static boolean inSharedInvGroup(String world1, String world2){
		return getInvGroup(world1).equals(getInvGroup(world2));
	}
	public static String getCurrentInvGroup(UUID playerUUID){
		File currentGroup = new File(DEFAULT_PLAYERDATA + playerUUID + ".group");
		if(currentGroup == null || !currentGroup.exists()) return DEFAULT_WORLD;
		try{return sharedInvWorlds.get(Files.readFirstLine(currentGroup, Charset.defaultCharset()));}
		catch(IOException e){e.printStackTrace(); return null;}
	}

	public static File getCurrentPlayerdata(UUID playerUUID){
		File dataFile = new File(DEFAULT_PLAYERDATA + playerUUID + ".dat");
		return dataFile;
	}
	public static File getPlayerdata(UUID playerUUID, String worldName, boolean useShared, boolean useCurrent){
		String useWorld = useShared ? sharedInvWorlds.get(worldName) : worldName;
		if(useWorld == null) useWorld = worldName;

		boolean inDefaultWorld = useWorld.equals(DEFAULT_WORLD);
		return (useCurrent && inSharedInvGroup(useWorld, getCurrentInvGroup(playerUUID)))
				? getCurrentPlayerdata(playerUUID)
				: new File("./" + useWorld +
						((inDefaultWorld && !SINGLE_INV_GROUP) ? "/playerdata_"+DEFAULT_WORLD+"/" : "/playerdata/")
						+ playerUUID +
						((inDefaultWorld && SINGLE_INV_GROUP) ? "_tmp.dat" : ".dat"));
	}

	static public boolean loadProfile(Player handler, UUID fromPlayer, String fromWorld,
			boolean useShared, boolean useCurrent, boolean copy){
		if(useCurrent && handler.getUniqueId().equals(fromPlayer) && inSharedInvGroup(handler.getWorld().getName(), fromWorld)) {
			return false;
		}
		File currentFile = getCurrentPlayerdata(handler.getUniqueId());
		File sourceFile = getPlayerdata(fromPlayer, fromWorld, useShared, useCurrent);
		if(sourceFile == null || !sourceFile.exists() || currentFile == null){
			Eventials.getPlugin().getLogger().warning("Unable to load profile from world: "+fromWorld);
			Eventials.getPlugin().getLogger().warning("Source file: "+sourceFile.getPath()+
					", Current file: "+currentFile.getPath());
			return false;
		}

		try{
			if(copy) Files.copy(sourceFile, currentFile);
			else Files.move(sourceFile, currentFile);
		}
		catch(IOException e){e.printStackTrace(); return false;}

		handler.loadData();
		if(TREAT_DISEASE) SplitWorldUtils.resetPlayer(handler); // Remove disease AFTER loading data (treat infected file)

		// This file provides a means to figure out what sharedInv group an OfflinePlayer is in
		File currentInv = new File(DEFAULT_PLAYERDATA + handler.getUniqueId() + ".group");
		try{Files.write(sharedInvWorlds.get(fromWorld)+" "+fromPlayer, currentInv, Charset.defaultCharset());}
		catch(IOException e){e.printStackTrace();}
		return true;
	}
	static public boolean saveProfile(Player handler, UUID toPlayer, String toWorld,
			boolean useShared, boolean useCurrent, boolean copy){
		if(useCurrent && handler.getUniqueId().equals(toPlayer) && !inSharedInvGroup(handler.getWorld().getName(), toWorld)){
			return false;
		}
		File currentFile = getCurrentPlayerdata(handler.getUniqueId());
		File destFile = getPlayerdata(toPlayer, toWorld, useShared, useCurrent);
		if(currentFile == null || !currentFile.exists() || destFile == null){
			Eventials.getPlugin().getLogger().warning("Unable to save profile to world: "+toWorld);
			Eventials.getPlugin().getLogger().warning("Desination file: "+destFile.getPath()+
					", Current file: "+currentFile.getPath());
			return false;
		}
		if(TREAT_DISEASE) SplitWorldUtils.resetPlayer(handler);// Remove disease BEFORE saving data (vaccinate the file)
		handler.saveData();

		try{
			if(copy) Files.copy(currentFile, destFile);
			else Files.move(currentFile, destFile);
		}
		catch(IOException e){e.printStackTrace(); return false;}
		return true;
	}
	static public boolean loadProfile(Player handler, File fromFile){
		File currentFile = getCurrentPlayerdata(handler.getUniqueId());
		if(fromFile == null || !fromFile.exists() || currentFile == null){
			Eventials.getPlugin().getLogger().warning("Unable to load profile from file: "+fromFile.getAbsolutePath());
			return false;
		}

		Eventials.getPlugin().getLogger().info("[DEBUG] source file: "+fromFile.getAbsolutePath());
		try{Files.copy(fromFile, currentFile);}
		catch(IOException e){e.printStackTrace(); return false;}

		Eventials.getPlugin().getLogger().info("[DEBUG] Calling loadData()");
		handler.loadData();
		if(TREAT_DISEASE) SplitWorldUtils.resetPlayer(handler); // Remove disease AFTER loading data (treat infected file)
		Eventials.getPlugin().getLogger().info("[DEBUG] loadData() called, disease treated, saving .group file now");

		// This file provides a means to figure out what sharedInv group an OfflinePlayer is in
		File currentInv = new File(DEFAULT_PLAYERDATA + handler.getUniqueId() + ".group");
		try{Files.write("CUSTOM_FILE "+fromFile.getPath(), currentInv, Charset.defaultCharset());}
		catch(IOException e){e.printStackTrace();}
		return true;
	}
	static public boolean saveProfile(Player handler, File toFile){
		File currentFile = getCurrentPlayerdata(handler.getUniqueId());
		if(currentFile == null || !currentFile.exists() || toFile == null){
			Eventials.getPlugin().getLogger().warning("Unable to save profile to file: "+toFile.getAbsolutePath());
			return false;
		}
		if(TREAT_DISEASE) SplitWorldUtils.resetPlayer(handler);// Remove disease BEFORE saving data (vaccinate the file)
		handler.saveData();

		try{Files.copy(currentFile, toFile);}
		catch(IOException e){e.printStackTrace(); return false;}
		return true;
	}
	static public boolean loadCurrentProfile(Player player){
		return loadProfile(player, player.getUniqueId(), player.getWorld().getName(), true, false, false);
	}
	static public boolean saveCurrentProfile(Player player){
		return saveProfile(player, player.getUniqueId(), player.getWorld().getName(), true, false, true);
	}

	@Deprecated boolean switchToInv(Player player, String worldFrom, String worldTo){
		if(inSharedInvGroup(worldFrom, worldTo)) return true;

		if(saveProfile(player, player.getUniqueId(), worldFrom, true, false, true)){
			if(loadProfile(player, player.getUniqueId(), worldTo, true, false, true)){
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