package EventAndMisc;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.TextUtils;

public class AC_Hardcore implements Listener{
	private final Eventials pl;
	final boolean fancyPl;
	final String WORLD_NAME = "Reliquist";
	final ItemStack starterBook;
	private float normalWalkSpeed;

	public AC_Hardcore(){
		pl = Eventials.getPlugin();
		pl.getServer().getPluginManager().registerEvents(this, pl);
		fancyPl = pl.getConfig().getBoolean("fancy-pl", true);
		World hardcoreWorld = pl.getServer().getWorld(WORLD_NAME);
		hardcoreWorld.setSpawnLocation(0, 75, 0);
		Block chestBlock = hardcoreWorld.getBlockAt(0, 75, 0);
		if(chestBlock.getType() == Material.CHEST){
			Chest chest = (Chest)chestBlock.getState();
			ItemStack book = null;
			for(ItemStack item : chest.getBlockInventory().getContents()){
				if(item != null && item.getType() == Material.WRITTEN_BOOK){
					book = item;
					break;
				}
			}
			starterBook = book;
		}
		else starterBook = null;
	}

	Location getRandomLocation(){
		World world = pl.getServer().getWorld(WORLD_NAME);
		WorldBorder border = world.getWorldBorder();
		Random rand = new Random();
		double x = rand.nextDouble() * border.getSize();
		if(rand.nextBoolean()) x = -x;
		double z = rand.nextDouble() * border.getSize();
		if(rand.nextBoolean()) z = -z;

		Location loc = border.getCenter();
		loc.setX(loc.getX() + x);
		loc.setZ(loc.getZ() + z);
		while(loc.getY() > 5 & (loc.getBlock() == null || loc.getBlock().isEmpty()
				|| loc.getBlock().isPassable())) loc.setY(loc.getY() - 1);
		loc.setY(loc.getY() + 2);
		return loc;
	}

	HashSet<UUID> newJoins = new HashSet<UUID>();
	HashSet<UUID> unconfirmed = new HashSet<UUID>();
	@EventHandler
	public void onPlayerFirstJoin(PlayerLoginEvent evt){
		if(!evt.getPlayer().hasPlayedBefore()) newJoins.add(evt.getPlayer().getUniqueId());
	}
	@EventHandler
	public void onPlayerFirstJoin(PlayerJoinEvent evt){
		if(!newJoins.remove(evt.getPlayer().getUniqueId())) return;
		unconfirmed.add(evt.getPlayer().getUniqueId());

		evt.getPlayer().setInvulnerable(true);
		Location spawnLoc = getRandomLocation();
		while(spawnLoc.getY() < 60 || spawnLoc.getBlock().getRelative(BlockFace.DOWN).isLiquid())
			spawnLoc = getRandomLocation();

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
			Block block = loc.add(x, y, z).getBlock();
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
			evt.getPlayer().setInvulnerable(false);
		}
	}

}