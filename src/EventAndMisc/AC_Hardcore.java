package EventAndMisc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
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
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
	final static String WORLD_NAME = "Reliquist";
	final ItemStack starterBook;
	final int numPreGenSpawns = 5;
	final ArrayDeque<Location> spawnLocs;
	final HashSet<String> tpaAliases, tpahereAliases, tpacceptAliases;
	final HashMap<UUID, UUID> pendingTpas, pendingTpaheres;//from -> to

	public AC_Hardcore(){
		pl = Eventials.getPlugin();
		pl.getLogger().setLevel(Level.ALL);
		pl.getServer().getPluginManager().registerEvents(this, pl);
		fancyPl = pl.getConfig().getBoolean("fancy-pl", true);
		World hardcoreWorld = pl.getServer().getWorld(WORLD_NAME);
		hardcoreWorld.setSpawnLocation(0, 1, 0);
		Block chestBlock = hardcoreWorld.getBlockAt(0, 1, 0);
		if(chestBlock.getState() instanceof BlockInventoryHolder) {
			Container chest = (Container)chestBlock.getState();
			ItemStack book = null;
			for(ItemStack item : chest.getInventory().getContents()){
				if(item != null && item.getType() == Material.WRITTEN_BOOK) {
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
		if(spawnLocs.size() < numPreGenSpawns) {
			pl.getLogger().info("Pre-Generating " + (numPreGenSpawns - spawnLocs.size()) + " spawnpoints...");
			while(spawnLocs.size() < numPreGenSpawns){
				spawnLocs.add(getRandomSpawnLoc());
			}
			saveSpawnLocs();
		}
		new SpectatorListener();
		new HC_AdvancementListener();

		PluginCommand cmdTpa = pl.getServer().getPluginCommand("tpa");
		PluginCommand cmdTpahere = pl.getServer().getPluginCommand("tpahere");
		PluginCommand cmdTpaccept = pl.getServer().getPluginCommand("tpaccept");
		if(cmdTpa == null) pl.getLogger().warning("Could not find command: /tpa");
		if(cmdTpahere == null) pl.getLogger().warning("Could not find command: /tpahere");
		if(cmdTpaccept == null) pl.getLogger().warning("Could not find command: /tpaccept");
		tpaAliases = new HashSet<String>();
		tpaAliases.addAll(cmdTpa.getAliases());
		tpaAliases.add(cmdTpa.getLabel());
		tpahereAliases = new HashSet<String>();
		tpahereAliases.addAll(cmdTpahere.getAliases());
		tpahereAliases.add(cmdTpahere.getLabel());
		tpacceptAliases = new HashSet<String>();
		tpacceptAliases.addAll(cmdTpaccept.getAliases());
		tpacceptAliases.add(cmdTpaccept.getLabel());
		pl.getLogger().fine("Tpa aliases: " + tpaAliases.toString());
		pl.getLogger().fine("Tpahere aliases: " + tpahereAliases.toString());
		pl.getLogger().fine("Tpaccept aliases: " + tpacceptAliases.toString());
		pendingTpas = new HashMap<UUID, UUID>();
		pendingTpaheres = new HashMap<UUID, UUID>();
	}

	static boolean deletePlayerdata(UUID uuid){
		return new File("./" + WORLD_NAME + "/playerdata/" + uuid + ".dat").delete()
			&& new File("./" + WORLD_NAME + "/stats/" + uuid + ".json").delete()
			&& new File("./" + WORLD_NAME + "/advancements/" + uuid + ".json").delete();
	}
	static boolean copyPlayerdata(UUID uuid, String dir){
		try{
			Files.copy(new File("./" + WORLD_NAME + "/playerdata/" + uuid + ".dat").toPath(),
						new File(dir+"/playerdata_" + uuid + ".dat").toPath());
			Files.copy(new File("./" + WORLD_NAME + "/stats/" + uuid + ".json").toPath(),
						new File(dir+"/stats_" + uuid + ".json").toPath());
			Files.copy(new File("./" + WORLD_NAME + "/advancements/" + uuid + ".json").toPath(),
						new File(dir+"/advancements_" + uuid + ".json").toPath());
			return true;
		}
		catch(IOException e){return false;}
	}

	void saveSpawnLocs(){
		FileIO.saveFile("pre-gen-spawns.txt", StringUtils.join(
				spawnLocs.stream()
				.map(loc -> loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ())
				.iterator(), '\n'));
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
	// Warning: Very laggy!  Call asynchronously when possible
	Location getRandomSpawnLoc(){
		World world = pl.getServer().getWorld(WORLD_NAME);
		int seaLevel = pl.getServer().getWorld(WORLD_NAME).getSeaLevel();
		WorldBorder border = world.getWorldBorder();
		double maxOffset = border.getSize()/2;
		double stdDev = maxOffset/4;
		Random rand = new Random();
		Location loc;
		while(true){
			double x = (rand.nextGaussian() * rand.nextGaussian()) * stdDev;
			double z = (rand.nextGaussian() * rand.nextGaussian()) * stdDev;
			while(Math.abs(x) > maxOffset) x = rand.nextGaussian() * stdDev;
			while(Math.abs(z) > maxOffset) z = rand.nextGaussian() * stdDev;

			loc = border.getCenter();
			loc.setX(Math.floor(loc.getX() + x) + 0.5d);
			loc.setZ(Math.floor(loc.getZ() + z) + 0.5d);
			loc.setY(250);
			String debugStr = "Candidate X,Y,Z: "+loc.getBlockX()+" _ "+loc.getBlockZ();
			if(isOnChunkBoundary(loc)){
				pl.getLogger().info(debugStr+" >> On chunk boundary");
				continue;
			}

			if(!loc.getChunk().load(true)){
				pl.getLogger().severe("Failed to generate spawnLoc chunk!");
				return null;
			}

			while(loc.getBlockY() > seaLevel & (loc.getBlock() == null || loc.getBlock().isEmpty() 
					|| loc.getBlock().isPassable())) loc.setY(loc.getY() - 1);
			loc.setY(loc.getY() + 2);
			debugStr = "Candidate X,Y,Z: "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ();
			if(loc.getY() < seaLevel + 3)
				pl.getLogger().info(debugStr+" >> Below sea level");
			else if(loc.getBlock().getRelative(BlockFace.DOWN).isLiquid())
				pl.getLogger().info(debugStr+" >> Over liquid");
			else if(hasNearbyLava(loc))
				pl.getLogger().info(debugStr+" >> Near to lava");
			else{
				pl.getLogger().info(debugStr+" >> SUCCESS");
				break;
			}
		}
		return loc;
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
		player.addScoreboardTag("unconfirmed");
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

		File deathDir = new File("./plugins/EvFolder/deaths/"+player.getName());
		if(deathDir.exists()){
			int numDeaths = deathDir.listFiles().length;
			player.setStatistic(Statistic.DEATHS, numDeaths);
		}

		//TODO: new permissions plugin. This is garbage.
		pl.runCommand("perms player addgroup "+player.getName()+" default");
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt){
		Player player = evt.getPlayer();
		if(player.getScoreboardTags().isEmpty()) spawnNewPlayer(player);
		if(!player.isOp()){
			setPermission(player, "essentials.tpa", player.getScoreboardTags().contains("has_tpa"));
			setPermission(player, "essentials.tpahere", player.getScoreboardTags().contains("has_tpahere"));
			setPermission(player, "essentials.tpaccept", player.getScoreboardTags().contains("has_tpaccept"));
		}
	}

	static boolean setPermission(Player player, String permission, boolean value){
		if(player.hasPermission(permission) == value) return false;
		Eventials.getPlugin().runCommand(
				"perms player setperm "+player.getName()+" "+permission+" "+(""+value).toLowerCase());
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
			enableTest("Eventials")+"Eventials=>Package of custom-built features and tweaks§r, §a\\" +
			enableTest("DropHeads")+"DropHeads=>Provides a chance to get heads from mobs/players§r, \\\\n§a\\" +
			enableTest("HorseOwners")+"HorseOwners=>Claim, name, and view stats for horses§r, §a\\" +
			enableTest("ChatManager")+"ChatManager=>Keeps chat pg13 + Color/Format for chat & signs§r, §a\\" +
//			enableTest("EnchantBook")+"EnchantBook=>Color item names in anvils, looting on axes, etc!§r, §a\\" +
			"More=>\\"+
//			enableTest("WorldEdit")+"WorldEdit\\§f, \\" +
//			enableTest("WorldGuard")+"WorldGuard\\§f, \\" +
//			enableTest("PluginLoader")+"PluginLoader\\§f, \\" +
			ChatColor.GREEN+"EvAntiCheat\\§f, \\" +
			enableTest("PermissionsBukkit")+"PermissionsBukkit\\§f, \\" +
			enableTest("BungeeTabList")+"BungeeTabListPlus\\§f, \\" +
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
		Player player = evt.getPlayer();

		if(command.equals("pl") || command.equals("plugins") || command.equals("?")){
			if(fancyPl && player.hasPermission("bukkit.command.plugins")){
				evt.setCancelled(true);
				showFancyPlugins(player);
			}
		}
		else if(command.equals("accept-terms") && player.removeScoreboardTag("unconfirmed")){
			evt.setCancelled(true);
			player.setWalkSpeed(0.2f);
			removeNearbyBedrock(player.getLocation());
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10, 3), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10, 3), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 2, 0), true);
			player.setInvulnerable(false);
			player.setSaturation(20);
			player.setHealth(evt.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			player.addScoreboardTag("joined");
		}
		else if(command.equals("color")){
			evt.setCancelled(true);
			if(space < 0){
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&00 &11 &22 &33 &44 &55 &66 &77 &88 &99 &aa &bb &cc &dd &ee &ff"));
				player.sendMessage(ChatColor.GRAY+"/color #");
			}
			else{
				String colorCh = evt.getMessage().substring(space+1).replaceAll("&", "");
				if(colorCh.length() > 1){
					player.sendMessage(ChatColor.GRAY+"Please provide just a single character");
				}
				else{
					ChatColor color = ChatColor.getByChar(colorCh.charAt(0));
					if(color == null){
						player.sendMessage(ChatColor.GRAY+"Unknown color '"+colorCh+"'");
					}
					else{
						String name = player.getName();
						pl.runCommand("nick "+name+" &"+colorCh+name);
						//evt.getPlayer().setDisplayName(color+evt.getPlayer().getName());
						//evt.getPlayer().setCustomName(color+evt.getPlayer().getName());
						player.sendMessage(color+"Color set!");
					}
				}
			}
		}
		else if(player.isOp()){
			return;// Everything below here modifies permissions
		}
		else if(command.equals("tp")) {
			if(SpectatorListener.isSpectator(player)){
				evt.setCancelled(true);
				Player target = null;
				if(space < 0 || (target=pl.getServer().getPlayer(evt.getMessage().substring(space + 1))) == null){
					player.sendMessage(ChatColor.RED+"Please specify who you wish to tp to (exact username)");
					player.sendMessage("Note: you can also use vanilla spectator menu (press 1)");
				}
				else{
					player.teleport(target);
					player.setSpectatorTarget(target);
				}
			}
		}
		else if(tpaAliases.contains(command)){
			
			if(!player.hasPermission("essentials.tpa") || !player.removeScoreboardTag("has_tpa")){
				player.sendMessage(ChatColor.RED+"You have already used your one /tpa");
				evt.setCancelled(true);
			}
			else{
				Player target = pl.getServer().getPlayer(evt.getMessage().substring(space+1));
				if(space < 0 || target == null){
					player.sendMessage(ChatColor.RED+"Please specify who to tpa to "+ChatColor.UNDERLINE+"exactly");
					player.addScoreboardTag("has_tpa");
					evt.setCancelled(true);
				}
				else if(player.getScoreboardTags().contains("tp_"+target.getUniqueId()) ||
						target.getScoreboardTags().contains("tp_"+player.getUniqueId())){
					player.sendMessage(ChatColor.RED+"You cannot tp "+target.getName()+" twice in the same life");
					player.addScoreboardTag("has_tpa");
					evt.setCancelled(true);
				}
				else{
					pendingTpas.put(player.getUniqueId(), target.getUniqueId());
					pendingTpaheres.remove(player.getUniqueId());
				}
			}
		}
		else if(tpahereAliases.contains(command)){
			if(!player.hasPermission("essentials.tpahere") || !player.removeScoreboardTag("has_tpahere")){
				player.sendMessage(ChatColor.RED+"You have already used your one /tpahere");
				evt.setCancelled(true);
			}
			else{
				Player target = pl.getServer().getPlayer(evt.getMessage().substring(space+1));
				if(space < 0 || target == null){
					player.sendMessage(ChatColor.RED+"Please specify who to tpahere "+ChatColor.UNDERLINE+"exactly");
					player.addScoreboardTag("has_tpahere");
					evt.setCancelled(true);
				}
				else if(player.getScoreboardTags().contains("tp_"+target.getUniqueId()) ||
						target.getScoreboardTags().contains("tp_"+player.getUniqueId())){
					player.sendMessage(ChatColor.RED+"You cannot tp "+target.getName()+" twice in the same life");
					player.addScoreboardTag("has_tpa");
					evt.setCancelled(true);
				}
				else{
					pendingTpaheres.put(player.getUniqueId(), target.getUniqueId());
					pendingTpas.remove(player.getUniqueId());
				}
			}
		}
		else if(tpacceptAliases.contains(command)){
			if(!player.hasPermission("essentials.tpaccept") || !player.removeScoreboardTag("has_tpaccept")){
				player.sendMessage(ChatColor.RED+"You have already used your one /tpaccept");
				evt.setCancelled(true);
			}
			else{
				Player target = pl.getServer().getPlayer(evt.getMessage().substring(space+1));
				if(space < 0 || target == null){
					player.sendMessage(ChatColor.RED+"Please specify the player whose request you are accepting");
					player.addScoreboardTag("has_tpaccept");
					evt.setCancelled(true);
				}
				else if(pendingTpas.containsKey(target.getUniqueId())
						&& pendingTpas.get(target.getUniqueId()).equals(player.getUniqueId())){
					player.sendMessage(ChatColor.GOLD+"Accepted "+target.getName()+"'s tpa");
					pendingTpas.remove(target.getUniqueId());
				}
				else if(pendingTpaheres.containsKey(target.getUniqueId())
						&& pendingTpaheres.get(target.getUniqueId()).equals(player.getUniqueId())){
					player.sendMessage(ChatColor.GOLD+"Accepted "+target.getName()+"'s tpahere");
					pendingTpaheres.remove(target.getUniqueId());
				}
				else{
					player.sendMessage(ChatColor.RED+"You do not have a pending tpa from "+target.getName());
					player.addScoreboardTag("has_tpaccept");
					evt.setCancelled(true);
					return;
				}
				new BukkitRunnable(){@Override public void run(){
					setPermission(player, "essentials.tpaccept", false);
					setPermission(target, "essentials.tpa", false);
				}}.runTaskLater(pl, 20*5);
				player.addScoreboardTag("tp_"+target.getUniqueId());
				target.addScoreboardTag("tp_"+player.getUniqueId());
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent evt){
		final UUID uuid = evt.getEntity().getUniqueId();
		final String name = evt.getEntity().getName();
		evt.getEntity().saveData();
		evt.getEntity().loadData();
		pl.runCommand("scoreboard players reset "+name);
		//evt.getEntity().kickPlayer("" + ChatColor.RED + ChatColor.BOLD + "You died");
		new BukkitRunnable(){@Override public void run(){
			pl.runCommand("tempban " + name + " 1m1s " + ChatColor.GOLD + "Died in hardcore beta");
		}}.runTaskLater(pl, 5);
		new BukkitRunnable(){@Override public void run(){
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
			pl.getLogger().warning("Copying playerdata for "+name+"...");
			if(!copyPlayerdata(uuid, deathDir)) pl.getLogger().severe("Copy faied");
		}}.runTaskLater(pl, 20 * 10);
	}
}