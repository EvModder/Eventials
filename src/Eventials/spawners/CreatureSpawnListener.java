package Eventials.spawners;

import java.util.Random;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import Eventials.Eventials;

public class CreatureSpawnListener implements Listener{
	final int MEAN, STD_DEV;
	final double CHANCE;
	final boolean RECURSIVE_SPAWNING;
	private boolean armySpawnEnabled = true;
	Random rand;

	public CreatureSpawnListener(){
		Eventials plugin = Eventials.getPlugin();
		MEAN = plugin.getConfig().getInt("army-size-mean", 50);
		STD_DEV = plugin.getConfig().getInt("army-size-standard-deviation", 50);
		CHANCE = plugin.getConfig().getDouble("army-spawn-likelyhood", .005);
		RECURSIVE_SPAWNING = plugin.getConfig().getBoolean("army-enable-recursive-spawning", true);
		rand = new Random();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onSpawn(CreatureSpawnEvent evt){
		if(evt.isCancelled()) return;

		if(evt.getSpawnReason() == SpawnReason.NATURAL && evt.getEntity() instanceof Monster
				&& armySpawnEnabled && evt.getEntityType() != EntityType.PIG_ZOMBIE && rand.nextDouble() < CHANCE)
		{
			if(!RECURSIVE_SPAWNING) armySpawnEnabled = false;
			//
			int armySize = (int) (rand.nextGaussian()*STD_DEV + MEAN);
			for(int i=0; i<armySize; ++i) evt.getLocation().getWorld().spawnEntity(evt.getLocation(), evt.getEntityType());
			//
			if(!RECURSIVE_SPAWNING) armySpawnEnabled = true;
		}
	}
}
