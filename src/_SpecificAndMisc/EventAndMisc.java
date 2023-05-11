package _SpecificAndMisc;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import Eventials.Eventials;
import Eventials.listeners.PaperFix_EntityAddToWorldListener;
import Eventials.listeners.PaperFix_EntityChangeBlockListener;
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
			pl.getLogger().info("Empty EventAndMisc (Testcraft/Leafcraft v0)");
		}

		if(pl.getConfig().isConfigurationSection("world-borders")) loadWorldBorders();
		if(pl.getConfig().getBoolean("add-recipes", true)) loadRecipes();
		if(pl.getConfig().getBoolean("fix-paper-rng-manip", true)) new PaperFix_EntityAddToWorldListener(pl);
		if(pl.getConfig().getBoolean("fix-paper-gravity-dupe", true)) new PaperFix_EntityChangeBlockListener(pl);
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