package Eventials.spawners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class TrapSpawnListener implements Listener{
	@EventHandler
	public void onSpawn(CreatureSpawnEvent evt){
		if(evt.getSpawnReason() == SpawnReason.TRAP) evt.setCancelled(true);
	}
}