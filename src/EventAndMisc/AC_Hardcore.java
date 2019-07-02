package EventAndMisc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
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
import org.bukkit.command.PluginCommand;
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
	final int numPreGenSpawns = 5;
	final ArrayDeque<Location> spawnLocs;
	final HashSet<UUID> newJoins, unconfirmed;
	final HashSet<String> tpaAliases, tpahereAliases, tpacceptAliases;

	public AC_Hardcore(){
		pl = Eventials.getPlugin();
		pl.getLogger().setLevel(Level.ALL);
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
		newJoins = new HashSet<UUID>();
		unconfirmed = new HashSet<UUID>();
		new SpectatorListener();

		PluginCommand cmdTpa = pl.getServer().getPluginCommand("tpa");
		PluginCommand cmdTpahere = pl.getServer().getPluginCommand("tpahere");
		PluginCommand cmdTpaccept = pl.getServer().getPluginCommand("tpaccept");
		if(cmdTpa == null) pl.getLogger().warning("Could not find command: /tpa");
		if(cmdTpahere == null) pl.getLogger().warning("Could not find command: /tpahere");
		if(cmdTpaccept == null) pl.getLogger().warning("Could not find command: /tpaccept");
		tpaAliases = new HashSet<String>();
		tpaAliases.addAll(cmdTpa.getAliases()); tpaAliases.add(cmdTpa.getLabel());
		tpahereAliases = new HashSet<String>();
		tpahereAliases.addAll(cmdTpahere.getAliases()); tpahereAliases.add(cmdTpahere.getLabel());
		tpacceptAliases = new HashSet<String>();
		tpacceptAliases.addAll(cmdTpaccept.getAliases()); tpacceptAliases.add(cmdTpaccept.getLabel());
		pl.getLogger().fine("Tpa aliases: "+tpaAliases.toString());
		pl.getLogger().fine("Tpahere aliases: "+tpahereAliases.toString());
		pl.getLogger().fine("Tpaccept aliases: "+tpacceptAliases.toString());
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
	boolean hasNearbyLava(Location loc){
		for(int x=-15; x<=15; ++x) for(int y=-10; y<=10; ++y) for(int z=-15; z<=15; ++z){
			Block block = loc.clone().add(x, y, z).getBlock();
			if(block != null && block.getType() == Material.LAVA) return true;
		}
		return false;
	}
	Location getRandomSpawnLoc(){
		Location spawnLoc = getRandomLocation();
		while(spawnLoc == null || spawnLoc.getY() < pl.getServer().getWorld(WORLD_NAME).getSeaLevel()
				|| spawnLoc.getBlock().getRelative(BlockFace.DOWN).isLiquid()
				|| isOnChunkBoundary(spawnLoc) || hasNearbyLava(spawnLoc))
			spawnLoc = getRandomLocation();
		return spawnLoc;
	}

	void removeNearbyBedrock(Location loc){
		for(int x=-6; x<=6; ++x) for(int y=-6; y<=6; ++y) for(int z=-6; z<=6; ++z){
			Block block = loc.clone().add(x, y, z).getBlock();
			if(block != null && block.getType() == Material.BEDROCK) block.setType(Material.AIR);
		}
	}

	void createSpawnBox(Location loc){
		// Always set the 6 faces
		loc.getBlock().getRelative(BlockFace.UP).setType(Material.BEDROCK);
		loc.getBlock().getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
		loc.getBlock().getRelative(BlockFace.NORTH).setType(Material.BEDROCK);
		loc.getBlock().getRelative(BlockFace.SOUTH).setType(Material.BEDROCK);
		loc.getBlock().getRelative(BlockFace.EAST).setType(Material.BEDROCK);
		loc.getBlock().getRelative(BlockFace.WEST).setType(Material.BEDROCK);
		for(int x=-3; x<=3; ++x) for(int y=-3; y<=3; ++y) for(int z=-3; z<=3; ++z){
			Block block = loc.clone().add(x, y, z).getBlock();
			if(block != null && block.getType() == Material.AIR) block.setType(Material.BEDROCK);
		}
		loc.getBlock().setType(Material.AIR);
	}

	void spawnNewPlayer(Player player){
		pl.getLogger().warning("New player: "+player.getName());
		final UUID uuid = player.getUniqueId();
		unconfirmed.add(uuid);
		player.addScoreboardTag("joined");
		player.addScoreboardTag("has_tpahere");
		player.addScoreboardTag("has_tpa");
		player.addScoreboardTag("has_tpaccept");

		player.setInvulnerable(true);

		Location spawnLoc = spawnLocs.remove();
		pl.getLogger().warning("Spawning in at: "
			+Extras.locationToString(spawnLoc, ChatColor.GREEN, ChatColor.YELLOW));
		saveSpawnLocs();
		new BukkitRunnable(){@Override public void run(){
			Location spawnLoc = getRandomSpawnLoc();
			spawnLocs.add(spawnLoc);
		}}.runTaskLater/*Asynchronously*/(pl, 20*60);//60s

		createSpawnBox(spawnLoc);

		spawnLoc.setX(spawnLoc.getBlockX() + 0.5);
		spawnLoc.setZ(spawnLoc.getBlockZ() + 0.5);
		player.teleport(spawnLoc);
		new BukkitRunnable(){@Override public void run(){
			Player player = pl.getServer().getPlayer(uuid);
			if(player != null) player.teleport(spawnLoc);
		}}.runTaskLater(pl, 20);
		player.setBedSpawnLocation(spawnLoc);
		player.getInventory().setItemInMainHand(starterBook);
		player.setWalkSpeed(0f);

		File deathDir = new File("./plugins/EvFolder/deaths/"+uuid);
		if(deathDir.exists()){
			int numDeaths = deathDir.listFiles().length;
			player.setStatistic(Statistic.DEATHS, numDeaths);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent evt){
		if(!evt.getPlayer().hasPlayedBefore() && evt.getPlayer().getLastPlayed() == 0 &&
				evt.getPlayer().getScoreboardTags().isEmpty()){
			newJoins.add(evt.getPlayer().getUniqueId());
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent evt){
		Player player = evt.getPlayer();
		if(newJoins.remove(player.getUniqueId()) && player.getScoreboardTags().isEmpty()){
			spawnNewPlayer(player);
		}
		if(!player.isOp()){
			setPermission(player, "essentials.tpa", player.getScoreboardTags().contains("has_tpa"));
			setPermission(player, "essentials.tpahere", player.getScoreboardTags().contains("has_tpahere"));
			setPermission(player, "essentials.tpaccept", player.getScoreboardTags().contains("has_tpaccept"));
		}
	}

	public boolean setPermission(Player player, String permission, boolean value){
		if(player.hasPermission(permission) == value) return false;
		pl.runCommand("perms player setperm "+player.getName()+" "+permission+" "+(""+value).toLowerCase());
		/*final PermissionsPlugin permsPlugin = (PermissionsPlugin)
				pl.getServer().getPluginManager().getPlugin("PermissionsBukkit");
		if(permsPlugin == null) return false;
		PermissionInfo playerInfo = permsPlugin.getPlayerInfo(player.getUniqueId());
		String uuid = player.getUniqueId().toString();
		if(playerInfo == null){
			permsPlugin.getConfig().set("users."+uuid, new YamlConfiguration());
			permsPlugin.getConfig().set("users."+uuid+".name", player.getName());
			permsPlugin.getConfig().set("users."+uuid+".permissions", new YamlConfiguration());
		}
		permsPlugin.getConfig().set("users."+uuid+".permissions."+permission, value);
		java.lang.reflect.Method methodRegisterPlayer;
		try{methodRegisterPlayer = permsPlugin.getClass().getDeclaredMethod("registerPlayer", Player.class);}
		catch(SecurityException | NoSuchMethodException ex){
			pl.getLogger().warning("Failed to access registerPlayer(): " + ex.getMessage());
			return false;
		}
		methodRegisterPlayer.setAccessible(true);
		try{methodRegisterPlayer.invoke(permsPlugin, player);}
		catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException ex){
			pl.getLogger().warning("Failed to invoke registerPlayer(): " + ex.getMessage());
			return false;
		}*/
		return true;
	}

	ChatColor enableTest(String pluginName){
		Plugin plugin = Eventials.getPlugin().getServer().getPluginManager().getPlugin(pluginName);
		return (plugin != null && plugin.isEnabled()) ? ChatColor.GREEN : ChatColor.RED;
	}
	void showFancyPlugins(Player player){
		String raw = TextUtils.TextAction.parseToRaw(
			"Plugins: §a\\" +
//			enableTest("Renewable")+"Renewable=>Prevents unrenewable items from being destroyed§r, §a\\" +
			enableTest("Essentials")+"Essentials=>Collection of useful tools and commands§r, §a\\" +
			enableTest("DropHeads")+"DropHeads=>Provides a chance to get heads from mobs/players§r, §a\\" +
			enableTest("HorseOwners")+"HorseOwners=>Claim, name, and view stats for horses§r, \\\\n§a\\" +
			enableTest("Eventials")+"Eventials=>Package of custom-built features and tweaks§r, §a\\" +
			enableTest("ChatManager")+"ChatManager=>Keeps chat pg13 + Color/Format for chat & signs§r, §a\\" +
			enableTest("EnchantBook")+"EnchantBook=>Color item names in anvils, looting on axes, etc!§r, §a\\" +
			"More=>\\"+
//			enableTest("WorldEdit")+"WorldEdit\\§f, \\" +
//			enableTest("WorldGuard")+"WorldGuard\\§f, \\" +
			enableTest("PluginLoader")+"PluginLoader\\§f, \\" +
			enableTest("PermissionsBukkit")+"PermissionsBukkit\\§f, \\" +
			enableTest("BungeeTabListPlus")+"BungeeTabListPlus\\§f, \\" +
			enableTest("Votifier")+"Votifier§r" +
			".\\\\n\\§7\\§oHover over a plugin to see more details!",
			"§r"
		);
		Eventials.getPlugin().runCommand("tellraw "+player.getName()+' '+raw);
	}

	@EventHandler @SuppressWarnings("deprecation")
	public void onPreCommand(PlayerCommandPreprocessEvent evt){
		if(evt.getMessage().charAt(0) != '/') return;
		String command = evt.getMessage().toLowerCase();
		int space = command.indexOf(' ');
		command = (space > 0 ? command.substring(1, space) : command.substring(1));

		if(command.equals("pl") || command.equals("plugins") || command.equals("?")){
			if(fancyPl && evt.getPlayer().hasPermission("bukkit.command.plugins")){
				evt.setCancelled(true);
				showFancyPlugins(evt.getPlayer());
			}
		}
		else if(command.equals("accept-terms") && unconfirmed.remove(evt.getPlayer().getUniqueId())){
			evt.setCancelled(true);
			evt.getPlayer().setWalkSpeed(0.2f);
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
		else if(evt.getPlayer().isOp()){
			return;// Everything below here modifies permissions
		}
		else if(tpaAliases.contains(command)){
			if(!evt.getPlayer().removeScoreboardTag("has_tpa")){
				evt.getPlayer().sendMessage(ChatColor.RED+"You have already used your one /tpa");
				evt.setCancelled(true);
			}
			if(space < 0 || pl.getServer().getPlayer(evt.getMessage().substring(space+1)) == null){
				evt.getPlayer().sendMessage(ChatColor.RED+
						"Pleast specify who you want to tpa to "+ChatColor.UNDERLINE+"exactly");
				evt.getPlayer().addScoreboardTag("has_tpa");
				evt.setCancelled(true);
			}
			else{
				pl.getLogger().info(evt.getPlayer().getName()+" used their /tpa");
				new BukkitRunnable(){@Override public void run(){
					if(!setPermission(evt.getPlayer(), "essentials.tpa", false))
						pl.getLogger().warning("Failed to set permission");
				}}.runTaskLater(pl, 2);
			}
		}
		else if(tpahereAliases.contains(command)){
			if(!evt.getPlayer().removeScoreboardTag("has_tpahere")){
				evt.getPlayer().sendMessage(ChatColor.RED+"You have already used your one /tpahere");
				evt.setCancelled(true);
			}
			if(space < 0 || pl.getServer().getPlayer(evt.getMessage().substring(space+1)) == null){
				evt.getPlayer().sendMessage(ChatColor.RED+
						"Pleast specify who you want to tpa here "+ChatColor.UNDERLINE+"exactly");
				evt.getPlayer().addScoreboardTag("has_tpahere");
				evt.setCancelled(true);
			}
			else{
				pl.getLogger().info(evt.getPlayer().getName()+" used their /tpahere");
				new BukkitRunnable(){@Override public void run(){
					setPermission(evt.getPlayer(), "essentials.tpahere", false);
				}}.runTaskLater(pl, 2);
			}
		}
		else if(tpacceptAliases.contains(command)){
			if(!evt.getPlayer().removeScoreboardTag("has_tpaccept")){
				evt.getPlayer().sendMessage(ChatColor.RED+"You have already used your one /tpaccept");
				evt.setCancelled(true);
			}
			if(space < 0 || pl.getServer().getPlayer(evt.getMessage().substring(space+1)) == null){
				evt.getPlayer().sendMessage(ChatColor.RED+
						"Pleast specify the exact player whose request you are tpaccepting");
				evt.getPlayer().addScoreboardTag("has_tpaccept");
				evt.setCancelled(true);
			}
			else{
				pl.getLogger().info(evt.getPlayer().getName()+" used their /tpaccept");
				new BukkitRunnable(){@Override public void run(){
					setPermission(evt.getPlayer(), "essentials.tpaccept", false);
				}}.runTaskLater(pl, 2);
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent evt){
		final UUID uuid = evt.getEntity().getUniqueId();
		final String name = evt.getEntity().getName();
		evt.getEntity().saveData();
		evt.getEntity().loadData();
		//evt.getEntity().kickPlayer("" + ChatColor.RED + ChatColor.BOLD + "You died");
		new BukkitRunnable(){@Override public void run(){
			pl.runCommand("tempban " + name + " 1d1s " + ChatColor.GOLD + "Died in hardcore beta");
		}}.runTaskLater(pl, 5);
		new BukkitRunnable(){
			@Override public void run(){
				String dateStr = new SimpleDateFormat("yyy-MM-dd").format(new Date());
				String deathDir = "./plugins/EvFolder/deaths";
				if(!new File(deathDir).exists()) new File(deathDir).mkdir();
				deathDir += "/"+/*uuid*/name;
				if(!new File(deathDir).exists()) new File(deathDir).mkdir();
				deathDir += "/"+dateStr;
				if(new File(deathDir).exists()){
					int i = 1;
					while(new File(deathDir+"."+i).exists()) ++i;
					deathDir += "."+i;
				}
				new File(deathDir).mkdir();

				pl.getLogger().info("Clearing playerdata for " + uuid + "...");
				if(!new File("./" + WORLD_NAME + "/playerdata/" + uuid + ".dat").exists()) {
					pl.getLogger().severe("Playerdata not found!");
					pl.getLogger().severe("Target: ./" + WORLD_NAME + "/playerdata/" + uuid + ".dat");
				}
				if(!new File("./" + WORLD_NAME + "/playerdata/" + uuid + ".dat")
						.renameTo(new File(deathDir + "/playerdata_" + uuid + ".dat"))) {
					pl.getLogger().warning("Failed to reset playerdata");
				}
				if(!new File("./" + WORLD_NAME + "/stats/" + uuid + ".dat")
						.renameTo(new File(deathDir + "/stats_" + uuid + ".json"))) {
					pl.getLogger().warning("Failed to reset stats");
				}
				if(!new File("./" + WORLD_NAME + "/advancements/" + uuid + ".dat")
						.renameTo(new File(deathDir + "/advancements_" + uuid + ".json"))) {
					pl.getLogger().warning("Failed to reset advancements");
					new File("./" + WORLD_NAME + "/advancements/" + uuid + ".dat").delete();
				}
			}
		}.runTaskLater(pl, 20 * 10);
	}
}