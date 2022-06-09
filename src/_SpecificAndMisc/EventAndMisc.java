package _SpecificAndMisc;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import Eventials.Eventials;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.extras.TellrawUtils.TextHoverAction;
import net.evmodder.EvLib.extras.TellrawUtils.Component;
import net.evmodder.EvLib.extras.TellrawUtils.HoverEvent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;

public class EventAndMisc{
	final Eventials pl;

	void loadCustomConfig(String configName){
		InputStream rssAC = getClass().getResourceAsStream("/"+configName);
		YamlConfiguration hardConf = FileIO.loadConfig(pl, configName, rssAC, /*notifyIfNew=*/false);
		InputStream rssDefault = getClass().getResourceAsStream("/config.yml");
		YamlConfiguration defaultConf = FileIO.loadConfig(pl, "config-Eventials.yml", rssDefault, /*notifyIfNew=*/true);
		if(pl.getConfig().toString().equals(defaultConf.toString())){
			for(String key : pl.getConfig().getKeys(false)) pl.getConfig().set(key, null);
			for(String key : hardConf.getKeys(false)) pl.getConfig().set(key, hardConf.get(key));
		}
		long millisSinceEdit = System.currentTimeMillis() - new File(FileIO.DIR+"config-Eventials.yml").lastModified();
		if(millisSinceEdit < 10000){
			new File(FileIO.DIR+configName).renameTo(new File(FileIO.DIR+"config-Eventials.yml"));
		}
		else FileIO.deleteFile(configName);
	}

	public EventAndMisc(final Eventials pl){
		this.pl = pl;
		if(pl.getServer().getWorld("VictoryHills") != null){
			pl.getLogger().info("Loading AC_Alternate(Old)-specific config");
			loadCustomConfig("config_oldworld.yml");
			new AC_Old();
			pl.getServer().getPluginManager().registerEvents(new FactionsProtectPatch(pl), pl);
		}
		else if(pl.getServer().getWorld("Reliquist") != null){
			pl.getLogger().info("Loading Hardcore-specific config");
			loadCustomConfig("config_hardcore.yml");
			new AC_Hardcore();
		}
		else if(pl.getServer().getWorld("MysteryPeaks") != null){
			pl.getLogger().info("Loading NewWorld-specific config");
			loadCustomConfig("config_newworld.yml");
			new AC_New();
			pl.getServer().getPluginManager().registerEvents(new FactionsProtectPatch(pl), pl);
		}
		else{
			// Testcraft? Other?
			pl.getLogger().info("Empty EventAndMisc (Testcraft)");
		}

		if(pl.getConfig().isConfigurationSection("world-borders")) loadWorldBorders();
		if(pl.getConfig().getBoolean("add-recipes", true)) loadRecipes();
		if(pl.getConfig().getBoolean("scoreboards-for-all-statistics", false)) addScoreboardsForAllStats();
		pl.getServer().getPluginManager().registerEvents(new Listener(){
			@EventHandler public void onPlayerQuit(PlayerQuitEvent evt){evt.setQuitMessage("");}
		}, pl);
	}

	void loadWorldBorders(){
		ConfigurationSection worldSettings = pl.getConfig().getConfigurationSection("world-borders");
		if(worldSettings != null) for(String worldName : worldSettings.getKeys(false)){
			World w = pl.getServer().getWorld(worldName);
			if(w == null) pl.getLogger().warning("UNKNOWN WORLD: \""+worldName+"\" (from config)");
			else{
				try{
					ConfigurationSection borderData = worldSettings.getConfigurationSection(worldName);
					double centerX = borderData.getDouble("center-x", 0), centerZ = borderData.getDouble("center-z", 0);
					double radius = borderData.getDouble("radius", 0);
//					double scale = borderData.getDouble("scale", 1);//TODO: world scale
					if(radius != 0){
						w.getWorldBorder().setCenter(centerX, centerZ);
						w.getWorldBorder().setSize(2*radius);
						w.getWorldBorder().setDamageBuffer(1);//can be 1 block outside of border before taking damage
						pl.getLogger().fine("WorldBorder for "+worldName+": x="+centerX+", z="+centerZ+", radius="+radius);
					}
//					if(scale != w.getScale()){
//						w.setScale(scale);
//					}
				}
				catch(NullPointerException ex){
					pl.getLogger().severe("Missing/Broken setting in config for world-border of \""+worldName+"\"");
				}
			}
		}
	}

	void loadRecipes(){
		try{
			// Add saddles
			ShapedRecipe Saddle = (new ShapedRecipe(new NamespacedKey(pl, "saddle"),
					new ItemStack(Material.SADDLE, 1))).shape(new String[] { "^*^", "* *", "   " })
					.setIngredient('*', Material.LEATHER)
					.setIngredient('^', Material.IRON_INGOT);
			pl.getServer().addRecipe(Saddle);
		}
		catch(IllegalStateException ex){
			pl.getLogger().warning("Tried to load recipes that were already loaded!");
			return;
		}

		/* Alternate Method
		final ShapedRecipe diamond = new ShapedRecipe(new ItemStack(Material.DIAMOND_BARDING, 1));
		diamond.shape("  *", "*^*", "***");
		diamond.setIngredient('*', Material.DIAMOND);
		diamond.setIngredient('^', Material.WOOL);
		pl.getServer().addRecipe(diamond);
		 */

//		// Add Diamond Horse Armor
//		ItemStack dyedWool = new ItemStack(Material.WOOL, 1, (byte) 11);
//		ShapedRecipe HorseArmorDiamond = (new ShapedRecipe(new NamespacedKey(pl, "diamond_barding"),
//				new ItemStack(Material.DIAMOND_BARDING, 1))).shape("  *", "*^*", "***")
//				.setIngredient('*', Material.DIAMOND)
//				.setIngredient('^', dyedWool.getData());
//		pl.getServer().addRecipe(HorseArmorDiamond);

//		// Add Iron Horse Armor
//		ItemStack dyedWool = new ItemStack(Material.WOOL, 1, (byte) 7);
//		ShapedRecipe HorseArmorIron = (new ShapedRecipe(new NamespacedKey(pl, "iron_barding"),
//				new ItemStack(Material.IRON_BARDING, 1))).shape(new String[] { "  *", "*^*", "***" })
//				.setIngredient('*', Material.IRON_INGOT)
//				.setIngredient('^', dyedWool.getData());
//		pl.getServer().addRecipe(HorseArmorIron);

//		// Add Gold Horse Armor
//		dyedWool = new ItemStack(Material.WOOL, 1, (byte) 14);
//		ShapedRecipe HorseArmorGold = (new ShapedRecipe(new NamespacedKey(pl, "gold_barding"),
//				new ItemStack(Material.GOLD_BARDING, 1))).shape(new String[] { "  *", "*^*", "***" })
//				.setIngredient('*', Material.GOLD_INGOT)
//				.setIngredient('^', dyedWool.getData());
//		pl.getServer().addRecipe(HorseArmorGold);

		// Add name tags
		ShapedRecipe NameTag = (new ShapedRecipe(new NamespacedKey(pl, "name_tag"),
				new ItemStack(Material.NAME_TAG, 1))).shape(new String[] { "^^*", "^^ ", "^^ " })
				.setIngredient('*', Material.STRING)
				.setIngredient('^', Material.PAPER);
		pl.getServer().addRecipe(NameTag);

		// Add cracked bricks (mossy cobble & bricks in 1.8+)
		ItemStack crackedBricks = new ItemStack(Material.CRACKED_STONE_BRICKS);
		ShapelessRecipe crackedbrick = (new ShapelessRecipe(new NamespacedKey(pl, "cracked_bricks"),
				crackedBricks))
				.addIngredient(Material.BRICK).addIngredient(Material.BRICK)
				.addIngredient(Material.COBBLESTONE);
		pl.getServer().addRecipe(crackedbrick);

/*		// Double stone slabs (id=43)
		ShapelessRecipe doubleSlab = (new ShapelessRecipe(new NamespacedKey(pl, "double_step"),
				new ItemStack(Material.DOUBLE_STEP)))
				.addIngredient(Material.STONE_PLATE)
				.addIngredient(Material.STONE_PLATE);
		pl.getServer().addRecipe(doubleSlab);*///TODO: figure out what Material type double slabs are these days

//		// Packed Ice
//		ShapelessRecipe packedIce = (new ShapelessRecipe(new NamespacedKey(pl, "packed_ice"),
//				new ItemStack(Material.PACKED_ICE)))
//				.addIngredient(Material.ICE).addIngredient(Material.ICE)
//				.addIngredient(Material.ICE).addIngredient(Material.ICE);
//		pl.getServer().addRecipe(packedIce);

//		Chiseled bricks
//		ItemStack chiseledBricks = new ItemStack(Material.BRICK, 1, (byte)3);
//		pl.getServer().addRecipe((new ShapedRecipe(chiseledBricks)));
	}

	private String capitalizeAndSpacify(String str){
		StringBuilder builder = new StringBuilder("");
		boolean wordStart = true;
		for(final char c : str.toCharArray()){
			final char upperC = Character.toUpperCase(c);
			if(wordStart){builder.append(upperC); wordStart = false;}
			else if(upperC < 'A' || upperC > 'Z'){if(!wordStart) builder.append(' '); wordStart = true;}
			else if(c == upperC) builder.append(' ').append(c);
			else builder.append(c);
		}
		return builder.toString().replaceAll(" Cm$", " CM");
	}
	private void registerZstatObjective(String name, String criteria, String displayName){
		if(!displayName.startsWith("\"")) displayName = '"'+displayName+'"';
		final String cmd = new StringBuilder("scoreboard objectives add ")
				.append(name).append(' ').append(criteria).append(' ').append(displayName).toString();
		pl.runCommand(cmd);
//		pl.getLogger().info("ran cmd: "+cmd);
	}
	void addScoreboardsForAllStats(){
		Scoreboard board = pl.getServer().getScoreboardManager().getMainScoreboard();
		//registerZstatObjective(/*name=*/"zstats-", /*criteria=*/"dummy", /*displayName=*/"name"/*, RenderType.INTEGER*/);

		try{board.registerNewObjective("zstats-dummy", "dummy", "Zstats Test");}
		catch(IllegalArgumentException ex){return;}
		new BukkitRunnable(){@Override public void run(){
			pl.getLogger().info("Registering all statistics as scoreboards (may take a minute)...");
	
			// Simple stats (excluding trigger & dummy)
			for(String simpleStat : Arrays.asList("air", "armor", "deathCount", "food", "health", "level", "playerKillCount", "totalKillCount", "xp")){
				registerZstatObjective("zstats-"+simpleStat, simpleStat, capitalizeAndSpacify(simpleStat));
			}
			// "minecraft.custom:minecraft.<custom_stat>"
			for(String customStat : Arrays.asList(
					"animals_bred", "aviate_one_cm", "bell_ring", "boat_one_cm", "clean_armor", "clean_banner", "clean_shulker_box",
					"climb_one_cm", "crouch_one_cm", "damage_absorbed", "damage_blocked_by_shield", "damage_dealt", "damage_dealt_absorbed",
					"damage_dealt_resisted", "damage_resisted", "damage_taken", "deaths", "drop", "eat_cake_slice", "enchant_item",
					"fall_one_cm", "fill_cauldron", "fish_caught", "fly_one_cm", "horse_one_cm", "inspect_dispenser", "inspect_dropper",
					"inspect_hoppers", "interact_with_anvil", "interact_with_beacon", "interact_with_blast_furnace", "interact_with_brewingstand",
					"interact_with_campfire", "interact_with_cartography_table", "interact_with_crafting_table", "interact_with_furnace",
					"interact_with_grindstone", "interact_with_lectern", "interact_with_loom", "interact_with_smithing_table", "interact_with_smoker",
					"interact_with_stonecutter", "jump", "leave_game", "minecart_one_cm", "mob_kills", "open_barrel", "open_chest", "open_enderchest",
					"open_shulker_box", "pig_one_cm", "play_noteblock", "play_record", "play_time", "player_kills", "pot_flower", "raid_trigger",
					"raid_win", "sleep_in_bed", "sneak_time", "sprint_one_cm", "strider_one_cm", "swim_one_cm", "talked_to_villager", "target_hit",
					"time_since_death", "time_since_rest", "total_world_time", "traded_with_villager", "trigger_trapped_chest", "tune_noteblock",
					"use_cauldron", "walk_on_water_one_cm", "walk_one_cm", "walk_under_water_one_cm"
			)){
				registerZstatObjective("zstats-"+customStat, "minecraft.custom:minecraft."+customStat, capitalizeAndSpacify(customStat));
			}
			// "killedByTeam.<color>", "teamKill.<color>"
			for(ChatColor color : ChatColor.values()){
				final String criteria1 = "killedByTeam."+color.getChar();
				registerZstatObjective("zstats-"+criteria1, criteria1, criteria1);
				final String criteria2 = "teamKill."+color.getChar();
				registerZstatObjective("zstats-"+criteria2, criteria2, criteria2);
			}
			for(Material mat : Material.values()){
				final String matNameL = mat.name().toLowerCase();
				if(mat.getMaxDurability() > 0){
					final String criteria = "minecraft.broken:minecraft."+matNameL;
					registerZstatObjective("zstats-"+criteria, criteria, capitalizeAndSpacify("Broken "+matNameL));
				}
				registerZstatObjective("zstats-dropped_"+matNameL, "minecraft.dropped:minecraft."+matNameL, capitalizeAndSpacify("Dropped "+matNameL));
				registerZstatObjective("zstats-pick_up_"+matNameL, "minecraft.picked_up:minecraft."+matNameL, capitalizeAndSpacify("Picked up "+matNameL));
				registerZstatObjective("zstats-used_"+matNameL, "minecraft.used:minecraft."+matNameL, capitalizeAndSpacify("Used "+matNameL));
				if(mat.isBlock()){
					registerZstatObjective("zstats-mined_"+matNameL, "minecraft.mined:minecraft."+matNameL, capitalizeAndSpacify("Mined "+matNameL));
				}
			}
			for(EntityType e : EntityType.values()){
				final String entNameL = e.name().toLowerCase();
				registerZstatObjective("zstats-killed_"+entNameL, "minecraft.killed:minecraft."+entNameL, capitalizeAndSpacify("Killed "+entNameL));
				registerZstatObjective("zstats-killed_by_"+entNameL, "minecraft.killed_by:minecraft."+entNameL, capitalizeAndSpacify("Killed by "+entNameL));
			}
			HashSet<Material> alreadyAdded = new HashSet<>();
			pl.getServer().recipeIterator().forEachRemaining(r -> {
				if(alreadyAdded.add(r.getResult().getType())){
					final String matNameLower = r.getResult().getType().name().toLowerCase();
					final String criteria = "minecraft.crafted:minecraft."+matNameLower;
					registerZstatObjective("zstats-crafted_"+matNameLower, criteria, capitalizeAndSpacify("Crafted "+matNameLower));
				}
			});
			pl.getLogger().info("Zstats scoreboards registered");
		}}.runTaskLater(pl, 20*5);
	}

	public static Component getPluginDisplay(String pluginName){//TODO: this beautiful function is currently unused!
		Plugin plugin = Eventials.getPlugin().getServer().getPluginManager().getPlugin(pluginName);
		if(plugin == null) return new RawTextComponent(ChatColor.RED+pluginName);
		ChatColor color = plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
		
		String version = plugin.getDescription().getVersion();
		String description = plugin.getDescription().getDescription();
		String website = plugin.getDescription().getWebsite();
		List<String> authors = plugin.getDescription().getAuthors();
		
		StringBuilder builder = new StringBuilder();
		if(version != null && !version.trim().isEmpty())
			builder.append(ChatColor.RESET).append("Version: ").append(ChatColor.GRAY).append(version).append('\n');
		if(description != null && !description.trim().isEmpty())
			builder.append(ChatColor.RESET).append("Description: ").append(ChatColor.GRAY).append(description).append('\n');
		if(website != null && !website.trim().isEmpty())
			builder.append(ChatColor.RESET).append("Website: ").append(ChatColor.GRAY).append(website).append('\n');
		if(authors != null && !authors.isEmpty())
			builder.append(ChatColor.RESET).append(authors.size() == 1 ? "Author: " : "Authors: ")
			.append(ChatColor.GRAY).append(String.join(ChatColor.RESET+", "+ChatColor.GRAY, authors)).append('\n');
		if(builder.length() == 0) return new RawTextComponent(color+plugin.getName());
		return new RawTextComponent(color+plugin.getName(), new TextHoverAction(HoverEvent.SHOW_TEXT, builder.substring(0, builder.length()-1)));
	}
}