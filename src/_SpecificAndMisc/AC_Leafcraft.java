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
import net.evmodder.Renewable.RenewableAPI;

public class AC_Leafcraft implements Listener{
	private final Eventials pl;
	private final RenewableAPI renewableAPI;
	private final String TAG_PREFIX = "came_from_";
	private final String SPAWN_WORLD = "CherrySpawn";

	private void leaveSpawn(Player p){
		Location returnLoc = null;
		for(String tag : p.getScoreboardTags()){
			if(tag.startsWith(TAG_PREFIX)){
				//pl.getLogger().info("found came_from tag");
				String[] data = tag.substring(TAG_PREFIX.length()).split("_");
				returnLoc = new Location(pl.getServer().getWorld(data[0]),
						Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3]));
				p.setFallDistance(Float.parseFloat(data[4]));
			}
		}
		if(returnLoc == null){
			p.setFallDistance(0);
			if(p.getBedSpawnLocation() != null
					&& p.getBedLocation() != null
					&& !p.getBedSpawnLocation().getWorld().getName().equals(SPAWN_WORLD)
					&& p.getBedLocation().getBlock().getType().name().endsWith("_BED")){
				p.sendMessage(TextUtils.translateAlternateColorCodes('&', "&#fddReturning to your bed"));
				returnLoc = p.getBedSpawnLocation();
			}
			else{
				p.sendMessage(TextUtils.translateAlternateColorCodes('&', "&#ffddddBed obstructed, sending you to worldspawn"));
				returnLoc = pl.getServer().getWorld("DaWorld").getSpawnLocation();
			}
		}
		else{
			p.sendMessage(TextUtils.translateAlternateColorCodes('&', "&#fddReturning to your previous location"));
			p.getScoreboardTags().removeIf(t -> t.startsWith(TAG_PREFIX));
		}
		p.teleport(returnLoc, TeleportCause.PLUGIN);
	}

	public AC_Leafcraft(){
		pl = Eventials.getPlugin();
		renewableAPI = ((Renewable)pl.getServer().getPluginManager().getPlugin("Renewable")).getAPI();
		pl.getServer().getPluginManager().registerEvents(this, pl);
		
		new BukkitRunnable(){
//			Location spawnPoint = spawnWorld.getSpawnLocation();
			final double tpDistSq = 40d*40d;
			@Override public void run(){
				World spawnWorld = pl.getServer().getWorld(SPAWN_WORLD);
				//pl.getLogger().info("cherry spawn: "+TextUtils.locationToString(spawnWorld.getSpawnLocation()));
				for(Player p : spawnWorld.getPlayers()){
					//pl.getLogger().info(p.getName()+"'s dist: "+p.getLocation().distanceSquared(spawnWorld.getSpawnLocation()));
					if(p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR
							&& p.getLocation().distanceSquared(spawnWorld.getSpawnLocation()) > tpDistSq){
						//pl.getLogger().info("they've left the cherry spawn zone");
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
		if(cmd.endsWith("spawn") && !evt.getPlayer().getWorld().getName().equals(SPAWN_WORLD)){
			evt.getPlayer().getScoreboardTags().removeIf(t -> t.startsWith(TAG_PREFIX));
			Location loc = evt.getPlayer().getLocation();
			evt.getPlayer().addScoreboardTag(TAG_PREFIX+loc.getWorld().getName()
					+"_"+loc.getX()+"_"+loc.getY()+"_"+loc.getZ()+"_"+evt.getPlayer().getFallDistance());
			//pl.getLogger().info("saved came_from tag");
		}
		if((cmd.endsWith("back") || cmd.endsWith("return")) && evt.getPlayer().getWorld().getName().equals(SPAWN_WORLD)){
			leaveSpawn(evt.getPlayer());
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
			pl.runCommand("minecraft:give "+name+" structure_void{CustomModelData:1,display:{"
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
			if(contents[i] != null && !renewableAPI.isUnrenewable(contents[i])){
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