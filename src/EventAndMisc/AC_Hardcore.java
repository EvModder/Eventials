package EventAndMisc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import Eventials.Eventials;
import Eventials.Extras;
import net.evmodder.EvLib.EvUtils;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.HorseOwners.HorseManager;

public class AC_Hardcore implements Listener{
	private final Eventials pl;
	final boolean fancyPl;
	final static String WORLD_NAME = "Reliquist";
	final ItemStack starterBook;
	final int numPreGenSpawns = 5;
	final double EULERS_CONSTANT = 0.57721566490153286060651209d;
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
		new HC_SpectatorListener();
		new HC_ScoreboardUpdater();

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
		if(pl.getServer().getScoreboardManager().getMainScoreboard().getTeam("Spectators") == null){
			pl.getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("Spectators");
		}
		hardcoreWorld.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true);
	}

	static boolean deletePlayerdata(UUID uuid){
		HorseManager horsePl = (HorseManager) Eventials.getPlugin()
				.getServer().getPluginManager().getPlugin("HorseOwners");
		if(horsePl != null){
			ArrayList<String> horses = new ArrayList<String>();
			if(horsePl.getHorseOwners().containsKey(uuid)) horses.addAll(horsePl.getHorseOwners().get(uuid));
			for(String horseName : horses) horsePl.removeHorse(uuid, horseName, false);
		}
		Eventials.getPlugin().getLogger().info("Deleting playerdata for: "+uuid);
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

	static int getNumDeaths(String name){
		File deathDir = new File("./plugins/EvFolder/deaths/"+name);
		return deathDir.exists() ? deathDir.listFiles().length : 0;
	}
	static String getLastDeath(String name){
		File deathDir = new File("./plugins/EvFolder/deaths/"+name);
		if(!deathDir.exists()) return "N/A";
		File[] files = deathDir.listFiles();
		if(files.length == 0) return "N/A";
		String lastDeath = files[0].getName();
		for(File file : files) if(file.getName().compareTo(lastDeath) > 0) lastDeath = file.getName();
		return lastDeath;
	}
	static void add_tp_tags(Player p1, Player p2){
		p1.sendMessage(ChatColor.GRAY+"You will no longer be able to tp to "+
				ChatColor.WHITE+p2.getName()+ChatColor.GRAY+" (in this life).");
		p2.sendMessage(ChatColor.GRAY+"You will no longer be able to tp to "+
				ChatColor.WHITE+p1.getName()+ChatColor.GRAY+" (in this life).");

		TreeSet<String> p1tps = new TreeSet<String>(), p2tps = new TreeSet<String>();
		for(String tag : p1.getScoreboardTags())
			if(tag.startsWith("tp_") && !p2.getScoreboardTags().contains(tag)) p1tps.add(tag);
		for(String tag : p2.getScoreboardTags())
			if(tag.startsWith("tp_") && !p1.getScoreboardTags().contains(tag)) p2tps.add(tag);
		if(!p1tps.isEmpty()){
			p2.sendMessage(ChatColor.GRAY+"Due to "+
					ChatColor.WHITE+p1.getName()+ChatColor.GRAY+"'s past teleports, you can no longer tp:");
			StringBuilder noTps = new StringBuilder("");
			for(String tag : p1tps){
				try{
					UUID uuid = UUID.fromString(tag.substring(3));
					OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(uuid);
					if(player != null) noTps.append(ChatColor.WHITE).append(player.getName())
						.append(ChatColor.GRAY).append(", ");
				}
				catch(IllegalArgumentException ex){continue;}
				p2.addScoreboardTag(tag);
			}
			p2.sendMessage(noTps.toString().substring(0, noTps.length()-2)+".");
		}
		if(!p2tps.isEmpty()){
			p1.sendMessage(ChatColor.GRAY+"Due to "+
					ChatColor.WHITE+p2.getName()+ChatColor.GRAY+"'s past teleports, you can no longer tp:");
			StringBuilder noTps = new StringBuilder("");
			for(String tag : p2tps){
				try{
					UUID uuid = UUID.fromString(tag.substring(3));
					OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(uuid);
					if(player != null) noTps.append(ChatColor.WHITE).append(player.getName())
						.append(ChatColor.GRAY).append(", ");
				}
				catch(IllegalArgumentException ex){continue;}
				p1.addScoreboardTag(tag);
			}
			p1.sendMessage(noTps.toString().substring(0, noTps.length()-2)+".");
		}
		p1.addScoreboardTag("tp_"+p2.getUniqueId());
		p2.addScoreboardTag("tp_"+p1.getUniqueId());
	}
	static boolean check_tp_tags(Player p1, Player p2){
		return p1.getScoreboardTags().contains("tp_"+p2.getUniqueId()) ||
				p2.getScoreboardTags().contains("tp_"+p1.getUniqueId());
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
	double getRandomCoord(double maxCoord, double scale, double randDouble){
		//Into wolfram: (e^(p*11.58 - 0.57721566490) - 0.5)*500
		pl.getLogger().info("Generating random coord, scale="+scale);
		double maxHn = Math.log(maxCoord/scale + 0.5) + EULERS_CONSTANT;
		double coord = (Math.exp(randDouble*maxHn - EULERS_CONSTANT) - 0.5)*scale;
		pl.getLogger().info("Max N="+Math.round(maxCoord)
				+", Max Hn="+(Math.round(maxHn*100d)/100d)
				+", Rand="+(Math.round(randDouble*100d)/100d)
				+", Hn="+(Math.round(randDouble*maxHn*100d)/100d)
				+", N="+Math.round(coord));
		return coord;
	}
	// Warning: Very laggy!  Call asynchronously when possible
	Location getRandomSpawnLoc(){
		World world = pl.getServer().getWorld(WORLD_NAME);
		int seaLevel = world.getSeaLevel();
		WorldBorder border = world.getWorldBorder();
		double maxOffset = border.getSize()/2;
		double stdDev = maxOffset/4;
		//double borderHn = Math.log(maxOffset + 0.5) + EULERS_CONSTANT;
		Random rand = new Random();
		Location loc;
		while(true){
			//double x = (rand.nextGaussian() * rand.nextGaussian()) * stdDev;
			//double z = (rand.nextGaussian() * rand.nextGaussian()) * stdDev;
			double x = getRandomCoord(maxOffset, 500, rand.nextDouble());
			double z = getRandomCoord(maxOffset, 500, rand.nextDouble());
			while(Math.abs(x) > maxOffset) x = rand.nextGaussian() * stdDev;
			while(Math.abs(z) > maxOffset) z = rand.nextGaussian() * stdDev;
			if(rand.nextBoolean()) x = -x;
			if(rand.nextBoolean()) z = -z;

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
		pl.getLogger().warning("Spawning new player: "+player.getName());
		final UUID uuid = player.getUniqueId();
		player.addScoreboardTag("unconfirmed");
		String Adv0TeamName = HC_ScoreboardUpdater.getAdvancementTeamName(0);
		Team newTeam = player.getScoreboard().getTeam(Adv0TeamName);
		if(newTeam == null) newTeam = player.getScoreboard().registerNewTeam(Adv0TeamName);
		newTeam.addEntry(player.getName());

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
		if(player.getScoreboardTags().contains("unconfirmed")){
			player.sendMessage(ChatColor.GREEN+">> "+ChatColor.GOLD+ChatColor.BOLD+"Read the book to get started");
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt){
		pendingTpas.remove(evt.getPlayer().getUniqueId());
		pendingTpaheres.remove(evt.getPlayer().getUniqueId());
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
			enableTest("ChatManager")+"Chat++=>Keeps chat pg13 + Color/Format for chat & signs§r, \\\\n§a\\" +
			enableTest("DropHeads")+"DropHeads=>Provides a chance to get heads from mobs/players§r, §a\\" +
			enableTest("HorseOwners")+"HorseOwners=>Claim, name, and view stats for horses§r, §a\\" +
//			enableTest("EnchantBook")+"EnchantBook=>Color item names in anvils, looting on axes, etc!§r, §a\\" +
			"More=>\\"+
//			enableTest("WorldEdit")+"WorldEdit\\§f, \\" +
//			enableTest("WorldGuard")+"WorldGuard\\§f, \\" +
//			enableTest("PluginLoader")+"PluginLoader\\§f, \\" +
			ChatColor.GREEN+"EvNoCheat\\§f, \\" +
//			enableTest("PermissionsBukkit")+"PermissionsBukkit\\§f, \\" +
			enableTest("BungeeTabListPlus")+"BungeeTabList\\§f, \\" +
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
			player.addScoreboardTag("has_tpahere");
			player.addScoreboardTag("has_tpa");
			player.addScoreboardTag("has_tpaccept");
			setPermission(player, "essentials.tpa", true);
			setPermission(player, "essentials.tpahere", true);
			setPermission(player, "essentials.tpaccept", true);
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
		else if(command.equals("seen")){
			if(player.hasPermission("essentials.seen") && space > 0){
				OfflinePlayer target = pl.getServer().getOfflinePlayer(evt.getMessage().substring(space + 1));
				if(target != null && target.hasPlayedBefore()){
					final String lastDeath = getLastDeath(target.getName());
					final UUID uuid = player.getUniqueId();
					new BukkitRunnable(){@Override public void run(){
						Player player = pl.getServer().getPlayer(uuid);
						if(player != null) player.sendMessage(ChatColor.GOLD+" - Last Death: "+
										ChatColor.RED+lastDeath);
					}}.runTaskLater(pl, 2);
				}
			}
		}
		else if(command.equals("tp")) {
			if(HC_SpectatorListener.isSpectator(player)){
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
			if((!player.hasPermission("essentials.tpa") || !player.getScoreboardTags().contains("has_tpa"))
					&& !player.isOp()){
				player.sendMessage(ChatColor.RED+"You have already used your one /tpa");
				evt.setCancelled(true);
			}
			else{
				Player target = pl.getServer().getPlayer(evt.getMessage().substring(space+1));
				if(space < 0 || target == null){
					player.sendMessage(ChatColor.RED+"Please specify who to tpa to "+ChatColor.UNDERLINE+"exactly");
					evt.setCancelled(true);
				}
				else if(check_tp_tags(player, target)){
					player.sendMessage(ChatColor.RED+"You have already used a tp that is connected to "+target.getName());
					evt.setCancelled(true);
				}
				else{
					player.sendMessage(ChatColor.LIGHT_PURPLE+"Sent a tpa to "+target.getName());
					pendingTpas.put(player.getUniqueId(), target.getUniqueId());
				}
			}
		}
		else if(tpahereAliases.contains(command)){
			if((!player.hasPermission("essentials.tpahere") || !player.getScoreboardTags().contains("has_tpahere"))
					&& !player.isOp()){
				player.sendMessage(ChatColor.RED+"You have already used your one /tpahere");
				evt.setCancelled(true);
			}
			else{
				Player target = pl.getServer().getPlayer(evt.getMessage().substring(space+1));
				if(space < 0 || target == null){
					player.sendMessage(ChatColor.RED+"Please specify who to tpahere "+ChatColor.UNDERLINE+"exactly");
					evt.setCancelled(true);
				}
				else if(check_tp_tags(player, target)){
					player.sendMessage(ChatColor.RED+"You have already used a tp that is connected to "+target.getName());
					evt.setCancelled(true);
				}
				else{
					player.sendMessage(ChatColor.LIGHT_PURPLE+"Sent a tpahere to "+target.getName());
					pendingTpaheres.put(player.getUniqueId(), target.getUniqueId());
				}
			}
		}
		else if(tpacceptAliases.contains(command)){
			if(!player.hasPermission("essentials.tpaccept") || !player.getScoreboardTags().contains("has_tpaccept")){
				player.sendMessage(ChatColor.RED+"You have already used your one /tpaccept");
				evt.setCancelled(true);
			}
			else{
				Player target = pl.getServer().getPlayer(evt.getMessage().substring(space+1));
				if(space < 0 || target == null){
					player.sendMessage(ChatColor.RED+"Please specify the player whose request you are accepting");
					evt.setCancelled(true);
				}
				else{
					player.sendMessage(ChatColor.LIGHT_PURPLE+"Accepted "+target.getName()+"'s tp request");
				}
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent evt){
		final UUID uuid = evt.getEntity().getUniqueId();
		final String name = evt.getEntity().getName();
		evt.getEntity().saveData();
		evt.getEntity().loadData();
		evt.getEntity().getScoreboard().resetScores(name);
		evt.getEntity().addScoreboardTag("dead");
		pl.getLogger().warning("Death of "+name+": "+evt.getDeathMessage());

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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTeleport(PlayerTeleportEvent evt){
		if(evt.getCause() != TeleportCause.COMMAND || evt.isCancelled()) return;
		Player teleporter = evt.getPlayer();
		Player receiver = null;
		double closestDist = 100;
		for(Player player : pl.getServer().getOnlinePlayers()){
			if(player.getLocation().distance(evt.getTo()) < closestDist){
				receiver = player;
				closestDist = player.getLocation().distance(evt.getTo());
			}
		}
		if(receiver == null){
			if(teleporter.getGameMode() != GameMode.SURVIVAL || teleporter.isOp()) return;
			teleporter.sendMessage(ChatColor.RED+"Could not locate destination player");
			evt.setCancelled(true);
		}
		else if(pendingTpas.containsKey(teleporter.getUniqueId())
				&& pendingTpas.get(teleporter.getUniqueId()).equals(receiver.getUniqueId())){
			pendingTpas.remove(teleporter.getUniqueId());
			teleporter.sendMessage(ChatColor.GREEN+receiver.getName()+" accepted your tpa");
			receiver.sendMessage(ChatColor.GREEN+"Accepted "+teleporter.getName()+"'s tpa");
			if(!teleporter.isOp()) setPermission(teleporter, "essentials.tpa", false);
			if(!receiver.isOp()) setPermission(receiver, "essentials.tpaccept", false);
			add_tp_tags(teleporter, receiver);
			teleporter.removeScoreboardTag("has_tpa");
			receiver.removeScoreboardTag("has_tpaccept");
		}
		else if(pendingTpaheres.containsKey(receiver.getUniqueId())
				&& pendingTpaheres.get(receiver.getUniqueId()).equals(teleporter.getUniqueId())){
			pendingTpaheres.remove(receiver.getUniqueId());
			receiver.sendMessage(ChatColor.GREEN+teleporter.getName()+" accepted your tpa");
			teleporter.sendMessage(ChatColor.GREEN+"Accepted "+receiver.getName()+"'s tpa");
			if(!receiver.isOp()) setPermission(receiver, "essentials.tpahere", false);
			if(!teleporter.isOp()) setPermission(teleporter, "essentials.tpaccept", false);
			add_tp_tags(receiver, teleporter);
			receiver.removeScoreboardTag("has_tpahere");
			teleporter.removeScoreboardTag("has_tpaccept");
		}
		else{
			if(teleporter.getGameMode() != GameMode.SURVIVAL || teleporter.isOp()) return;
			//teleporter.sendMessage(ChatColor.RED+"Error: Could not find a pending tp with "+receiver.getName());
			//evt.setCancelled(true);
			return;
		}
	}
}