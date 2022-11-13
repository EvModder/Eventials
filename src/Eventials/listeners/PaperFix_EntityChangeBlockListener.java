package Eventials.listeners;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import Eventials.Eventials;

// Based off of E-im's code from https://github.com/e-im/GravityControl
public class PaperFix_EntityChangeBlockListener implements Listener{
	public PaperFix_EntityChangeBlockListener(Eventials pl){
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}

	//Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)
	private static final List<Vector> DIRECTIONS = Arrays.asList(
			new Vector(1, 0, 0),
			new Vector(0, 0, 1),
			new Vector(-1, 0, 0),
			new Vector(0, 0, -1)
	);
	// Magic numbers...
	private static final Vector VELOCITY_COEFF = new Vector(1.46D, -2.4D, 1.46D);

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityChangeBlock(final EntityChangeBlockEvent evt){
		if(evt.getTo() == Material.AIR || !(evt.getEntity() instanceof final FallingBlock falling)) return;

		final BoundingBox boundingBox = falling.getBoundingBox().expand(-0.01D);

		for(Vector direction : DIRECTIONS){
			final Location loc = evt.getBlock().getLocation().add(direction);
			if(!loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) continue;
			final Block block = loc.getBlock();
			if(block.getType() != Material.END_PORTAL || !block.getBoundingBox().overlaps(boundingBox)) continue;

			final Vector velocity = falling.getVelocity();

			if(velocity.getX() == 0 && velocity.getZ() == 0){
				loc.getWorld().spawnFallingBlock(falling.getLocation().add(direction.getX()*0.25D, 0.05D, direction.getZ()*0.25D), falling.getBlockData());
			}
			else{
				falling.getWorld().spawnFallingBlock(
						falling.getLocation().add(direction.clone().multiply(0.25D)),
						falling.getBlockData()
				)
				.setVelocity(velocity.multiply(VELOCITY_COEFF));
			}
		}
	}
}
