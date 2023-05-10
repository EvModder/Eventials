package Eventials.listeners;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;

public class PlayerClickBlockListener implements Listener{
	final double SHATTER_CHANCE, STRONGHOLD_DENSITY;
	final boolean REMOVE_EYES_FROM_PORTAL, ADD_EXTRA_STRONGHOLDS;
	private Random rand;

	// Magic constants:
	final double ONLY_USE_VANILLA_SH_R_SQ = 20_000*20_000;
	final double ONLY_USE_CUSTOM_SH_R_SQ = 40_000*40_000;
	final long GRID_WIDTH, GRID_MAX_OFFSET, GRID_CHECK_OFFSET, MULT_X_FOR_RNG;

	public PlayerClickBlockListener(){
		final Eventials pl = Eventials.getPlugin();
		final FileConfiguration config = pl.getConfig();
		REMOVE_EYES_FROM_PORTAL = config.getBoolean("click-to-remove-eyes-of-ender", true);
		SHATTER_CHANCE = config.getDouble("remove-eye-of-ender-shatter-chance", .2d);
		ADD_EXTRA_STRONGHOLDS = config.getBoolean("add-extra-strongholds", false);
		STRONGHOLD_DENSITY = config.getDouble("extra-stronghold-density-in-km2", .1d);
		GRID_WIDTH = (long)(1000d / Math.sqrt(STRONGHOLD_DENSITY));
		GRID_MAX_OFFSET = config.getLong("extra-stronghold-max-random-offset", 5000l);
		GRID_CHECK_OFFSET = GRID_MAX_OFFSET/GRID_WIDTH + (GRID_MAX_OFFSET%GRID_WIDTH >= GRID_WIDTH/2 ? 1 : 0);
		MULT_X_FOR_RNG = 30_000_000 / GRID_WIDTH;

		if(ADD_EXTRA_STRONGHOLDS){
			pl.getServer().getPluginManager().registerEvents(new Listener(){
				@EventHandler public void onBlockClicked(ChunkLoadEvent evt){
					if(!evt.isNewChunk()) return;
					final Location chunkCenter = evt.getChunk().getBlock(7, evt.getChunk().getWorld().getSeaLevel(), 7).getLocation();
					if(chunkCenter.distanceSquared(new Location(chunkCenter.getWorld(), 0, 0, 0)) < ONLY_USE_VANILLA_SH_R_SQ) return;

					final Location shLoc = nearestCustomStronghold(chunkCenter);
					if(shLoc != null && shLoc.getBlockX()/16 == evt.getChunk().getX() && shLoc.getBlockZ()/16 == evt.getChunk().getZ()){
						new BukkitRunnable(){@Override public void run(){placeStronghold(shLoc);}}.runTaskLater(pl, 20);
					}
				}
			}, pl);
		}
	}

	final int SH_LOAD_CHUNK_R = 8;
	private void placeStronghold(Location loc){
		final World world = loc.getChunk().getWorld();
		for(int x = -SH_LOAD_CHUNK_R; x <= SH_LOAD_CHUNK_R; ++x){
			for(int z = -SH_LOAD_CHUNK_R; z <= SH_LOAD_CHUNK_R; ++z){
				final Chunk c = world.getChunkAt(x + loc.getChunk().getX(), z + loc.getChunk().getZ());
				c.load(/*generate=*/true);
				c.setForceLoaded(true);
			}
		}

		final String shGenCmd = "place structure stronghold "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ();
		//Eventials.getPlugin().getServer().broadcastMessage("running cmd: "+shGenCmd);
		Eventials.getPlugin().runCommand(shGenCmd);

		for(int x = -SH_LOAD_CHUNK_R; x <= SH_LOAD_CHUNK_R; ++x){
			for(int z = -SH_LOAD_CHUNK_R; z <= SH_LOAD_CHUNK_R; ++z){
				world.getChunkAt(x + loc.getChunk().getX(), z + loc.getChunk().getZ()).setForceLoaded(false);
			}
		}
	}

	private Location nearestCustomStronghold(Location loc){
		long x = loc.getBlockX() / GRID_WIDTH;
		long z = loc.getBlockZ() / GRID_WIDTH;
		Location closestLoc = null;
		final Location WORLD_ORIGIN = new Location(loc.getWorld(), 0, 0, 0);
		double minDistSq = Double.MAX_VALUE;
		for(long xi = x-GRID_CHECK_OFFSET; xi<=x+GRID_CHECK_OFFSET+1; ++xi){
			for(long zi = z-GRID_CHECK_OFFSET; zi<=z+GRID_CHECK_OFFSET+1; ++zi){
				long seed = (xi*MULT_X_FOR_RNG + zi) * loc.getWorld().getSeed();
				Random rand = new Random(seed);
				long offX = rand.nextLong(GRID_MAX_OFFSET);
				long y = loc.getWorld().getMinHeight() + rand.nextLong(loc.getWorld().getSeaLevel() - loc.getWorld().getMinHeight());
				long offZ = rand.nextLong(GRID_MAX_OFFSET);
				Location shLoc = new Location(loc.getWorld(), xi*GRID_WIDTH + offX, y, zi*GRID_WIDTH + offZ);
				double distSq = shLoc.distanceSquared(loc);
				if(distSq < minDistSq && shLoc.distanceSquared(WORLD_ORIGIN) > 25_000d*25_000d){// Don't generate extra strongholds <25k
					minDistSq = distSq;
					closestLoc = shLoc;
				}
			}
		}
		return closestLoc;
	}

	private Location getEyeTarget(Location from){
		final double dToOriginSq = from.distanceSquared(new Location(from.getWorld(), 0, 0, 0));
		if(dToOriginSq < ONLY_USE_VANILLA_SH_R_SQ){
			Bukkit.getLogger().info("<20k, only using vanilla SHs");
			return null;
		}
		Location vanillaSH = null;
		if(dToOriginSq < ONLY_USE_CUSTOM_SH_R_SQ){
			vanillaSH = from.getWorld().locateNearestStructure(from, StructureType.STRONGHOLD, 9000, false).getLocation();
		}
		else Bukkit.getLogger().info(">40k, only using extra SHs");
		Location customSH = nearestCustomStronghold(from);
		return vanillaSH == null || customSH.distanceSquared(from) < vanillaSH.distanceSquared(from) ? customSH : null;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockClicked(PlayerInteractEvent evt){
		if(REMOVE_EYES_FROM_PORTAL && evt.hasBlock() && evt.useInteractedBlock() == Result.ALLOW &&
				(!evt.getPlayer().isSneaking() || evt.getItem() == null)
				&& evt.getClickedBlock().getType() == Material.END_PORTAL_FRAME
				&& ((EndPortalFrame)evt.getClickedBlock().getState()).hasEye()){
			((EndPortalFrame)evt.getClickedBlock().getState()).setEye(false);
			evt.setUseItemInHand(Result.DENY);
			Location loc = evt.getClickedBlock().getLocation();
			loc.add(0, 0.1, 0);
			loc.setPitch(-90F);
			evt.getPlayer().getWorld().playEffect(loc, Effect.ENDEREYE_LAUNCH, 1);
			if(rand == null) rand = new Random();
			if(rand.nextDouble() > SHATTER_CHANCE){
				loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.ENDER_EYE, 1));
			}
			else{
				evt.getPlayer().playSound(loc, Sound.ENTITY_ENDER_EYE_DEATH, 1F, 1F);
			}
		}
		else if(ADD_EXTRA_STRONGHOLDS && evt.getMaterial() == Material.ENDER_EYE){
			if(evt.getAction() == Action.RIGHT_CLICK_BLOCK){
				evt.setCancelled(true);
				return;
			}
			else if(evt.getAction() == Action.RIGHT_CLICK_AIR){
				final Location eyeTarget = getEyeTarget(evt.getPlayer().getLocation());
				if(eyeTarget != null){
					evt.setCancelled(true);
					final EnderSignal es = (EnderSignal)evt.getPlayer().getWorld().spawnEntity(evt.getPlayer().getEyeLocation(), EntityType.ENDER_SIGNAL);
					es.setTargetLocation(eyeTarget);
					evt.getPlayer().getServer().broadcastMessage(evt.getPlayer().getName()+": "+eyeTarget.getBlockX()+", "+eyeTarget.getBlockY()+", "+eyeTarget.getBlockZ());
				}
			}
		}
	}
}
