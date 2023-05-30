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
	final long MIN_EXTRA_SH_R, MIN_EXTRA_SH_R_SQ;
	private Random rand;

	// Computed constants:
	final long MAX_VANILLA_SH_R = 24_320; // From the Minecraft wiki
	final long MAX_WORLD_SIZE = 30_000_000;
	final long GRID_WIDTH, GRID_MAX_OFFSET, GRID_CHECK_OFFSET, MULT_X_FOR_RNG;
	final long ONLY_EXTRA_SH_MIN_R, ONLY_VANILLA_SH_MAX_R = 22509;

	// Main API
	public Location nearestCustomStronghold(Location loc){
		final long x = loc.getBlockX() / GRID_WIDTH;
		final long z = loc.getBlockZ() / GRID_WIDTH;
		Location closestLoc = null;
		final Location WORLD_ORIGIN = new Location(loc.getWorld(), 0, 0, 0);
		double minDistSq = Double.MAX_VALUE;
		for(long xi = x-GRID_CHECK_OFFSET; xi<=x+GRID_CHECK_OFFSET+1; ++xi){
			for(long zi = z-GRID_CHECK_OFFSET; zi<=z+GRID_CHECK_OFFSET+1; ++zi){
				final long seed = (xi*MULT_X_FOR_RNG + zi) * loc.getWorld().getSeed();
				final Random rand = new Random(seed);
				final long offX = rand.nextLong(GRID_MAX_OFFSET);
				final long y = loc.getWorld().getMinHeight() + rand.nextLong(loc.getWorld().getSeaLevel() - loc.getWorld().getMinHeight());
				final long offZ = rand.nextLong(GRID_MAX_OFFSET);
				final Location shLoc = new Location(loc.getWorld(), xi*GRID_WIDTH + offX, y, zi*GRID_WIDTH + offZ);
				final double distSq = shLoc.distanceSquared(loc);
				if(distSq < minDistSq && shLoc.distanceSquared(WORLD_ORIGIN) > MIN_EXTRA_SH_R_SQ){
					minDistSq = distSq;
					closestLoc = shLoc;
				}
			}
		}
		return closestLoc;
	}

	public PlayerClickBlockListener(){
		final Eventials pl = Eventials.getPlugin();
		final FileConfiguration config = pl.getConfig();
		REMOVE_EYES_FROM_PORTAL = config.getBoolean("click-to-remove-eyes-of-ender", true);
		SHATTER_CHANCE = config.getDouble("remove-eye-of-ender-shatter-chance", .2d);

		if(ADD_EXTRA_STRONGHOLDS = config.getBoolean("add-extra-strongholds", false)){
			// Config constants
			STRONGHOLD_DENSITY = config.getDouble("extra-stronghold-density-in-km2", 0.1);
			GRID_MAX_OFFSET = config.getLong("extra-stronghold-max-random-offset", 6075);
			MIN_EXTRA_SH_R = config.getLong("extra-stronghold-min-distance-to-origin", 25856);
			if(MIN_EXTRA_SH_R <= MAX_VANILLA_SH_R){
				pl.getLogger().warning("Extra stronghold distance ("+MIN_EXTRA_SH_R+") is <= max vanilla distance ("
								+MAX_VANILLA_SH_R+"), this may cause overlap errors and incorrect eye of ender trajectories");
			}

			// Computed constants
			MIN_EXTRA_SH_R_SQ = MIN_EXTRA_SH_R * MIN_EXTRA_SH_R;
			GRID_WIDTH = (long)(1000d / Math.sqrt(STRONGHOLD_DENSITY));
			GRID_CHECK_OFFSET = GRID_MAX_OFFSET/GRID_WIDTH + (GRID_MAX_OFFSET%GRID_WIDTH >= GRID_WIDTH/2 ? 1 : 0);
			pl.getLogger().info("GRID_WIDTH: "+GRID_WIDTH);
			pl.getLogger().info("GRID_CHECK_OFFSET: "+GRID_CHECK_OFFSET);
			MULT_X_FOR_RNG = MAX_WORLD_SIZE / GRID_WIDTH;

			ONLY_EXTRA_SH_MIN_R = (long)Math.sqrt((768d/Math.sqrt(2) + (GRID_MAX_OFFSET/GRID_WIDTH + 1)*GRID_WIDTH));

			pl.getServer().getPluginManager().registerEvents(new Listener(){
				@EventHandler public void onBlockClicked(ChunkLoadEvent evt){
					if(!evt.isNewChunk()) return;
					final Location chunkCenter = evt.getChunk().getBlock(7, evt.getChunk().getWorld().getSeaLevel(), 7).getLocation();
					if(chunkCenter.distanceSquared(new Location(chunkCenter.getWorld(), 0, 0, 0)) < MIN_EXTRA_SH_R_SQ) return;

					final Location shLoc = nearestCustomStronghold(chunkCenter);
					if(shLoc != null && shLoc.getBlockX()/16 == evt.getChunk().getX() && shLoc.getBlockZ()/16 == evt.getChunk().getZ()){
						new BukkitRunnable(){@Override public void run(){placeStronghold(shLoc);}}.runTaskLater(pl, 20);
					}
				}
			}, pl);
		}
		else STRONGHOLD_DENSITY = GRID_WIDTH = GRID_MAX_OFFSET = GRID_CHECK_OFFSET =
				MIN_EXTRA_SH_R = MIN_EXTRA_SH_R_SQ = MULT_X_FOR_RNG = ONLY_EXTRA_SH_MIN_R = -1;
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

	private Location getEyeTarget(Location from){
		final double dToOriginSq = from.distanceSquared(new Location(from.getWorld(), 0, 0, 0));
		if(dToOriginSq < ONLY_VANILLA_SH_MAX_R){
			Bukkit.getLogger().info("only using vanilla SHs");
			return null;
		}
		Location vanillaSH = null;
		if(dToOriginSq < ONLY_EXTRA_SH_MIN_R){
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
					evt.getPlayer().getServer().broadcastMessage(evt.getPlayer().getName()+": "
								+eyeTarget.getBlockX()+", "+eyeTarget.getBlockY()+", "+eyeTarget.getBlockZ());
				}
			}
		}
	}
}
