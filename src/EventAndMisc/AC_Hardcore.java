package EventAndMisc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;
import Eventials.Extras;
import net.evmodder.EvLib.EvUtils;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.extras.TextUtils;

public class AC_Hardcore implements Listener{
	private final Eventials pl;
	final boolean fancyPl;
	final String WORLD_NAME = "Reliquist";
	final ItemStack starterBook;
	private float normalWalkSpeed;
	ArrayDeque<Location> spawnLocs;
	final int numPreGenSpawns = 5;

	public AC_Hardcore(){
		pl = Eventials.getPlugin();
		pl.getServer().getPluginManager().registerEvents(this, pl);
		fancyPl = pl.getConfig().getBoolean("fancy-pl", true);
		World hardcoreWorld = pl.getServer().getWorld(WORLD_NAME);
		hardcoreWorld.setSpawnLocation(0, 1, 0);
		Block chestBlock = hardcoreWorld.getBlockAt(0, 1, 0);
		if(chestBlock.getState() instanceof BlockInventoryHolder){
			Container chest = (Container)chestBlock.getState();
			ItemStack book = null;
			for(ItemStack item : chest.getInventory().getContents()){
				if(item != null && item.getType() == Material.WRITTEN_BOOK){
					book = item;
					break;
				}
			}
			starterBook = book;
		}
		else starterBook = null;
		if(starterBook == null) pl.getLogger().warning("Unable to find starter book");
		spawnLocs = new ArrayDeque<Location>();
		String[] spawnLocStrs = FileIO.loadFile("pre-gen-spawns.txt", "0.5,75,0.5").split("\n");
		for(String str : spawnLocStrs){
			Location loc = EvUtils.getLocationFromString(hardcoreWorld, str);
			if(loc != null) spawnLocs.add(loc);
		}
		if(spawnLocs.size() < numPreGenSpawns){
			pl.getLogger().info("Pre-Generating "+(numPreGenSpawns - spawnLocs.size())+" spawnpoints...");
			while(spawnLocs.size() < numPreGenSpawns){
				spawnLocs.add(getRandomSpawnLoc());
			}
			saveSpawnLocs();
		}
	}

	void saveSpawnLocs(){
		FileIO.saveFile("pre-gen-spawns.txt", StringUtils.join(
				spawnLocs.stream()
				.map(loc -> loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ())
				.iterator(), '\n'));
	}

	// Warning: Very laggy!  Call asynchronously when possible
	Location getRandomLocation(){
		World world = pl.getServer().getWorld(WORLD_NAME);
		WorldBorder border = world.getWorldBorder();
		double maxOffset = border.getSize()/2;
//		String minMaxStrX = " (min: "+Math.floor(border.getCenter().getX()-maxOffset)+
//							", max: "+Math.floor(border.getCenter().getX()+maxOffset)+")";
//		String minMaxStrZ = " (min: "+Math.floor(border.getCenter().getZ()-maxOffset)+
//							", max: "+Math.floor(border.getCenter().getZ()+maxOffset)+")";
		Random rand = new Random();
		double x = rand.nextDouble() * maxOffset;
		if(rand.nextBoolean()) x = -x;
		double z = rand.nextDouble() * maxOffset;
		if(rand.nextBoolean()) z = -z;

		Location loc = border.getCenter();
		loc.setX(Math.floor(loc.getX() + x) + 0.5d);
		loc.setZ(Math.floor(loc.getZ() + z) + 0.5d);
//		pl.getLogger().info("Random X: "+loc.getBlockX()+minMaxStrX);
//		pl.getLogger().info("Random Z: "+loc.getBlockZ()+minMaxStrZ);
		loc.setY(250);

		if(!loc.getChunk().load(true)){
			pl.getLogger().severe("Failed to generate spawnLoc chunk!");
			return null;
		}

		while(loc.getY() > 5 & (loc.getBlock() == null || loc.getBlock().isEmpty()
				|| loc.getBlock().isPassable())) loc.setY(loc.getY() - 1);
		loc.setY(loc.getY() + 2);
		pl.getLogger().info("Candidate X,Y,Z: "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ());
		return loc;
	}

	boolean isOnChunkBoundary(Location loc){
		return Math.abs(loc.getBlockX()) % 16 < 2 || Math.abs(loc.getBlockZ()) % 16 < 2;
	}
	Location getRandomSpawnLoc(){
		Location spawnLoc = getRandomLocation();
		while(spawnLoc == null || spawnLoc.getY() < pl.getServer().getWorld(WORLD_NAME).getSeaLevel()
				|| spawnLoc.getBlock().getRelative(BlockFace.DOWN).isLiquid()
				|| isOnChunkBoundary(spawnLoc))
			spawnLoc = getRandomLocation();
		return spawnLoc;
	}

	HashSet<UUID> newJoins = new HashSet<UUID>();
	HashSet<UUID> unconfirmed = new HashSet<UUID>();
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerFirstJoin(PlayerLoginEvent evt){
		if(!evt.getPlayer().hasPlayedBefore() &&
				evt.getPlayer().getScoreboardTags().isEmpty()){
			pl.getLogger().warning("New Player: "+evt.getPlayer().getName());
			newJoins.add(evt.getPlayer().getUniqueId());
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerFirstJoin(PlayerJoinEvent evt){
		if(!newJoins.remove(evt.getPlayer().getUniqueId()) ||
				!evt.getPlayer().getScoreboardTags().isEmpty()) return;
		pl.getLogger().warning("New Unconfirmed: "+evt.getPlayer().getName());
		unconfirmed.add(evt.getPlayer().getUniqueId());
		evt.getPlayer().addScoreboardTag("joined");

		evt.getPlayer().setInvulnerable(true);

		Location spawnLoc = spawnLocs.remove();
		pl.getLogger().warning("Spawning in at: "
			+Extras.locationToString(spawnLoc, ChatColor.GREEN, ChatColor.YELLOW));
		saveSpawnLocs();
		new BukkitRunnable(){
			@Override public void run(){
				Location spawnLoc = getRandomSpawnLoc();
				spawnLocs.add(spawnLoc);
			}
		}.runTaskLater/*Asynchronously*/(pl, 20*60);//60s

		spawnLoc.getBlock().getRelative(BlockFace.UP).setType(Material.BEDROCK);
		spawnLoc.getBlock().getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
		spawnLoc.getBlock().getRelative(BlockFace.NORTH).setType(Material.BEDROCK);
		spawnLoc.getBlock().getRelative(BlockFace.SOUTH).setType(Material.BEDROCK);
		spawnLoc.getBlock().getRelative(BlockFace.EAST).setType(Material.BEDROCK);
		spawnLoc.getBlock().getRelative(BlockFace.WEST).setType(Material.BEDROCK);

		evt.getPlayer().teleport(spawnLoc);
		evt.getPlayer().setBedSpawnLocation(spawnLoc);
		evt.getPlayer().getInventory().setItemInMainHand(starterBook);
		normalWalkSpeed = evt.getPlayer().getWalkSpeed();
		pl.getLogger().info("Default walk speed: "+normalWalkSpeed);
		evt.getPlayer().setWalkSpeed(0f);

		File deathDir = new File("./plugins/EvFolder/deaths/"+evt.getPlayer().getUniqueId());
		if(deathDir.exists()){
			int numDeaths = deathDir.listFiles().length;
			evt.getPlayer().setStatistic(Statistic.DEATHS, numDeaths);
		}
	}

	ChatColor enableTest(String pluginName){
		Plugin plugin = Eventials.getPlugin().getServer().getPluginManager().getPlugin(pluginName);
		return (plugin != null && plugin.isEnabled()) ? ChatColor.GREEN : ChatColor.RED;
	}
	void showFancyPlugins(Player player){
		String raw = TextUtils.TextAction.parseToRaw(
			"Plugins: §a\\" +
			enableTest("Renewable")+"Renewable=>Prevents unrenewable items from being destroyed§r, §a\\" +
			enableTest("Essentials")+"Essentials=>Collection of useful tools and commands§r, §a\\" +
			enableTest("DropHeads")+"DropHeads=>Provides a chance to get heads from mobs/players§r, §a\\" +
			enableTest("Eventials")+"Eventials=>Package of custom-built features and tweaks§r, \\\\n§a\\" +
			enableTest("HorseOwners")+"HorseOwners=>Claim, name, and view stats for horses§r, §a\\" +
			enableTest("ChatManager")+"ChatManager=>Keeps chat pg13 + Color/Format for chat & signs§r, §a\\" +
			enableTest("EnchantBook")+"EnchantBook=>Color item names in anvils, looting on axes, etc!§r, §a\\" +
			"More=>\\"+
			enableTest("WorldEdit")+"WorldEdit\\§f, \\" +
//			enableTest("WorldGuard")+"WorldGuard\\§f, \\" +
			enableTest("PluginLoader")+"PluginLoader\\§f, \\" +
			enableTest("Votifier")+"Votifier\\§f, \\" +
			enableTest("BungeeTabListPlus")+"BungeeTabListPlus\\§f, \\" +
			enableTest("PermissionsBukkit")+"PermissionsBukkit§r.\\\\n" +
			"\\§7\\§oHover over a plugin to see more details!",
			"§r"
		);
		Eventials.getPlugin().runCommand("tellraw "+player.getName()+' '+raw);
	}

	void removeNearbyBedrock(Location loc){
		for(int x=-2; x<=2; ++x) for(int y=-2; y<=2; ++y) for(int z=-2; z<=2; ++z){
			Block block = loc.clone().add(x, y, z).getBlock();
			if(block != null && block.getType() == Material.BEDROCK) block.setType(Material.AIR);
		}
	}

	@EventHandler
	public void onPreCommand(PlayerCommandPreprocessEvent evt){
		String command = evt.getMessage().toLowerCase();
		int space = command.indexOf(' ');
		if(space > 0) command = command.substring(0, space);

		if(command.equals("/pl") || command.equals("/plugins") || command.equals("/?")){
			if(fancyPl && evt.getPlayer().hasPermission("bukkit.command.plugins")){
				evt.setCancelled(true);
				showFancyPlugins(evt.getPlayer());
			}
		}
		else if(command.equals("/accept-terms") && unconfirmed.remove(evt.getPlayer().getUniqueId())){
			evt.setCancelled(true);
			evt.getPlayer().setWalkSpeed(normalWalkSpeed);
			removeNearbyBedrock(evt.getPlayer().getLocation());
			evt.getPlayer().addPotionEffect(
					new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10, 3), true);
			evt.getPlayer().addPotionEffect(
					new PotionEffect(PotionEffectType.REGENERATION, 10, 3), true);
			evt.getPlayer().addPotionEffect(
					new PotionEffect(PotionEffectType.JUMP, 2, 0), true);
			evt.getPlayer().setInvulnerable(false);
			evt.getPlayer().setSaturation(20);
			evt.getPlayer().setHealth(evt.getPlayer().getAttribute(
					Attribute.GENERIC_MAX_HEALTH).getValue());
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent evt){
		final UUID uuid = evt.getEntity().getUniqueId();
		String dateStr = new SimpleDateFormat("yyy-MM-dd").format(new Date());
		if(!new File("./plugins/EvFolder/deaths").exists())
			new File("./plugins/EvFolder/deaths").mkdir();
		String deathDir = "./plugins/EvFolder/deaths/"+uuid+"/"+dateStr;
		if(new File(deathDir).exists()){
			int i = 2;
			while(new File(deathDir+" "+i).exists()) ++i;
			deathDir += " "+i;
		}
		final String DEATH_DIR = deathDir;
		evt.getEntity().saveData();
		evt.getEntity().kickPlayer(""+ChatColor.RED+ChatColor.BOLD+"You died");
		pl.runCommand("tempban "+evt.getEntity().getName()+" 1d1s "
					+ChatColor.GOLD+"Died in hardcore beta");
		new BukkitRunnable(){
			@Override public void run(){
				pl.getLogger().info("Clearing playerdata for "+uuid+"...");
				if(!new File("./"+WORLD_NAME+"/playerdata/"+uuid+".dat").exists()){
					pl.getLogger().severe("Playerdata not found!");
					pl.getLogger().severe("Target: ./"+WORLD_NAME+"/playerdata/"+uuid+".dat");
				}
				new File(DEATH_DIR).mkdir();
				if(!new File("./"+WORLD_NAME+"/playerdata/"+uuid+".dat").renameTo(
						new File(DEATH_DIR+"/playerdata "+uuid+".dat"))){
					pl.getLogger().warning("Failed to reset playerdata");
				}
				if(!new File("./"+WORLD_NAME+"/stats/"+uuid+".dat").renameTo(
						new File(DEATH_DIR+"/stats "+uuid+".json"))){
					pl.getLogger().warning("Failed to reset stats");
				}
				if(!new File("./"+WORLD_NAME+"/advancements/"+uuid+".dat").renameTo(
						new File(DEATH_DIR+"/advancements "+uuid+".json"))){
					pl.getLogger().warning("Failed to reset advancements");
					new File("./"+WORLD_NAME+"/advancements/"+uuid+".dat").delete();
				}
			}
		}.runTaskLater(pl, 20*10);
	}
}