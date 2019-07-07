package EventAndMisc;

import java.io.InputStream;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import Eventials.Eventials;
import net.evmodder.EvLib.FileIO;

public class EventAndMisc{
	final Eventials pl;

	public EventAndMisc(final Eventials pl){
		this.pl = pl;
		if(pl.getServer().getWorld("VictoryHills") != null){
			pl.getServer().getPluginManager().registerEvents(new FactionsProtectPatch(pl), pl);
			new AC_Old();
		}
		else if(pl.getServer().getWorld("Reliquist") != null){
			InputStream rssHC = getClass().getResourceAsStream("/config_hardcore.yml");
			YamlConfiguration hardConf = FileIO.loadConfig(pl, "config_hardcore.yml", rssHC);
			FileIO.deleteFile("config_hardcore.yml");
			InputStream rssDefault = getClass().getResourceAsStream("/config.yml");
			YamlConfiguration defaultConf = FileIO.loadConfig(pl, "config-Eventials.yml", rssDefault);
			if(pl.getConfig().toString().equals(defaultConf.toString())){
				for(String key : pl.getConfig().getKeys(false)) pl.getConfig().set(key, null);
				pl.getConfig().setDefaults(hardConf);//TODO: This, or line below?
				for(String key : hardConf.getKeys(false)) pl.getConfig().set(key, hardConf.get(key));
			}
			new AC_Hardcore();
		}
		else/*if(pl.getServer().getWorld("MysteryPeaks") != null)*/{
			pl.getServer().getPluginManager().registerEvents(new FactionsProtectPatch(pl), pl);
			new AC_New();
		}

		if(pl.getConfig().isConfigurationSection("world-borders")) loadWorldBorders();
		if(pl.getConfig().getBoolean("add-recipes", true)) loadRecipes();
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

	public void loadRecipes(){
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
}