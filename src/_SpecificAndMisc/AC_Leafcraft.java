package _SpecificAndMisc;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import Eventials.Eventials;
import net.evmodder.EvLib.EvUtils;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.Renewable.Renewable;

public class AC_Leafcraft implements Listener{
	private final Eventials pl;
	private final Renewable renewablePl;
	private final String TAG_PREFIX = "came_from_";
	private final String SPAWN_WORLD = "Skyland";
	private final String MAIN_WORLD = "DaWorld";

	private void leaveSpawn(Player p){
		Location returnLoc = null;
		float fallDistance = 0;
		for(String tag : p.getScoreboardTags()){
			if(tag.startsWith(TAG_PREFIX)){
				//pl.getLogger().info("found came_from tag");
				String[] data = tag.substring(TAG_PREFIX.length()).split("\\+");
				returnLoc = new Location(pl.getServer().getWorld(data[0]),
						Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3]));
				fallDistance = Float.parseFloat(data[4]);
			}
		}
		if(returnLoc == null){
//			if(p.getBedSpawnLocation() != null
//					&& !p.getBedSpawnLocation().getWorld().getName().equals(SPAWN_WORLD)
////					&& p.getBedLocation() != null
////					&& p.getBedLocation().getBlock().getType().name().endsWith("_BED")
//			){
//				p.sendMessage(TextUtils.translateAlternateColorCodes('&', "&#fddReturning to your bed"));
//				returnLoc = p.getBedSpawnLocation();
//			}
//			else{
				p.sendMessage(TextUtils.translateAlternateColorCodes('&', "&#ffddddBed missing or obstructed, sending you to worldspawn"));
				returnLoc = pl.getServer().getWorld(MAIN_WORLD).getSpawnLocation();
//			}
		}
		else{
			p.sendMessage(TextUtils.translateAlternateColorCodes('&', "&#fddReturning to your previous location"));
			p.getScoreboardTags().removeIf(t -> t.startsWith(TAG_PREFIX));
		}
		p.setFallDistance(0);
		p.teleport(returnLoc, TeleportCause.PLUGIN);
		p.setFallDistance(fallDistance);
	}

	public AC_Leafcraft(){
		pl = Eventials.getPlugin();
		renewablePl = ((Renewable)pl.getServer().getPluginManager().getPlugin("Renewable"));
		pl.getServer().getPluginManager().registerEvents(this, pl);

		new BukkitRunnable(){
			@Override public void run(){
				final World spawnWorld = pl.getServer().getWorld(SPAWN_WORLD);
				if(spawnWorld == null) cancel();
				//pl.getLogger().info("cherry spawn: "+TextUtils.locationToString(spawnWorld.getSpawnLocation()));
				for(Player p : spawnWorld.getPlayers()){
					//pl.getLogger().info(p.getName()+"'s dist: "+p.getLocation().distanceSquared(spawnWorld.getSpawnLocation()));
					if(p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR
							&& p.getLocation().distanceSquared(spawnWorld.getSpawnLocation()) > 100*100//100
//							&& p.getLocation().distanceSquared(spawnWorld.getSpawnLocation()) > 1600d//40
//							&& p.getLocation().distanceSquared(new Location(spawnWorld, 2988, 88, -2743)) > 1024d//32
//							&& p.getLocation().distanceSquared(new Location(spawnWorld, 8, 320, -31)) > 51984d//228
					){
						pl.getLogger().info(p.getName()+" left the cherry spawn zone");
						leaveSpawn(p);
					}
				}
			}
		}.runTaskTimer(pl, 20L, 20L);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPreCommand(PlayerCommandPreprocessEvent evt){
		final int space = evt.getMessage().indexOf(' ');
		final String cmd = evt.getMessage().substring(1, space == -1 ? evt.getMessage().length() : space).toLowerCase();
		if(!evt.getPlayer().getWorld().getName().equals(SPAWN_WORLD)){
			if(cmd.endsWith("spawn")){
				evt.setCancelled(true);
				final World spawnWorld = pl.getServer().getWorld(SPAWN_WORLD);
				if(spawnWorld == null){
					evt.getPlayer().sendMessage(ChatColor.RED+"ERROR: spawn world unavailable");
					return;
				}
				evt.getPlayer().getScoreboardTags().removeIf(t -> t.startsWith(TAG_PREFIX));
				Location loc = evt.getPlayer().getLocation();
				evt.getPlayer().addScoreboardTag(TAG_PREFIX+loc.getWorld().getName()
						+"+"+loc.getX()+"+"+loc.getY()+"+"+loc.getZ()+"+"+evt.getPlayer().getFallDistance());
				//pl.getLogger().info("saved came_from tag");

				evt.getPlayer().sendMessage(TextUtils.translateAlternateColorCodes('&', "&#fddTeleporting to spawn"));
				evt.getPlayer().teleport(spawnWorld.getSpawnLocation(), TeleportCause.COMMAND);
			}
		}
		else if((cmd.endsWith("back") || cmd.endsWith("return") || cmd.endsWith("spawn"))){
			leaveSpawn(evt.getPlayer());
			evt.setCancelled(true);
		}
	}

	@EventHandler public void onEntityDeathEvent(EntityDeathEvent evt){
		if(evt.getEntityType() == EntityType.ENDER_DRAGON){
			ItemStack unplacingEgg = new ItemStack(Material.DRAGON_EGG);
			ItemMeta meta = unplacingEgg.getItemMeta();
			meta.setLore(Arrays.asList(ChatColor.GRAY+"Unplacing"));
			unplacingEgg.setItemMeta(meta);
			evt.getEntity().getWorld().dropItem(evt.getEntity().getLocation(), unplacingEgg);
		}
	}

	// Prevent chunk trails
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkLoad(ChunkLoadEvent evt){
		if(evt.isNewChunk()) evt.getChunk().setInhabitedTime(3_600_000);
	}

	@EventHandler public void onPlayerJoin(PlayerJoinEvent evt){
		if(!evt.getPlayer().getScoreboardTags().contains("joined")){
			evt.getPlayer().addScoreboardTag("joined");
			final String name = evt.getPlayer().getName();
			final String date = new SimpleDateFormat("yyy-MM-dd").format(new Date());
			pl.getLogger().info("Minting new player token: "+name);
//			pl.runCommand("minecraft:give "+name+" "
			pl.runCommand("minecraft:item replace entity "+name+" enderchest.0 with "
					+ "structure_void{CustomModelData:1,display:{"
					+ "Name:'{\"text\":\"Sigil of "+name+"\",\"color\":\"#33bbaf\",\"italic\":false}',"
					+ "Lore:['{\"text\":\""+date+"\",\"italic\":false,\"bold\":true,\"color\":\"#aaaa77\"}',"
					+ "'{\"text\":\"Unplacing\",\"italic\":false,\"color\":\"gray\"}',"
					+ "'{\"text\":\"Soul Bound\",\"italic\":false,\"color\":\"gray\"}']},"
					+ "Enchantments:[{id:lure,lvl:1}],HideFlags:1}");
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent evt){
		final Scoreboard mainBoard = evt.getEntity().getServer().getScoreboardManager().getMainScoreboard();
		final Score score = mainBoard.getObjective("pstats-unrenewables_destroyed").getScore(evt.getEntity().getName());
		if(score.isScoreSet() && score.getScore() > 0) return;
		evt.setKeepInventory(true);
		evt.getDrops().clear();

		// Regular inventory
		ItemStack[] contents = evt.getEntity().getInventory().getContents();
		for(int i=0; i<contents.length; ++i){
			if(contents[i] != null && !renewablePl.getAPI().isUnrenewable(contents[i])){
				//getLogger().info("dropping: "+contents[i].getType());
				EvUtils.dropItemNaturally(evt.getEntity().getLocation(), contents[i], null);
				contents[i] = null;
			}
		}
		evt.getEntity().getInventory().setContents(contents);
//		// Armor contents
//		contents = evt.getEntity().getInventory().getArmorContents();
//		for(int i=0; i<contents.length; ++i){
//			if(contents[i] != null && !IsSoulBound(contents[i])){
//				evt.getEntity().getWorld().dropItem(evt.getEntity().getLocation(), contents[i]);
//				contents[i] = null;
//			}
//		}
//		evt.getEntity().getInventory().setArmorContents(contents);
	}
}