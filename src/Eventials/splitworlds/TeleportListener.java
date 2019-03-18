package Eventials.splitworlds;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import Eventials.Eventials;

class TeleportListener implements Listener {
	final private Eventials pl;
	final private SplitWorlds splitWorlds;
	public TeleportListener(SplitWorlds sw){splitWorlds = sw; pl = Eventials.getPlugin();}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onTp(PlayerTeleportEvent evt){
		if(evt.getPlayer().hasMetadata(SplitWorlds.SKIP_TP_INV_CHECK)){
			evt.getPlayer().removeMetadata(SplitWorlds.SKIP_TP_INV_CHECK, pl);
			return;
		}
		if(evt.isCancelled() || evt.getPlayer().hasPermission("eventials.inventory.universal")) return;

		String toWorldName = evt.getTo().getWorld().getName();
		String fromWorldName = evt.getFrom().getWorld().getName();
		if(splitWorlds.inSharedInvGroup(toWorldName, fromWorldName)) return;

		//otherwise, update to the new world's inventory
		evt.setCancelled(true);
		splitWorlds.transInvWorldTp(evt.getPlayer(), evt.getFrom(), evt.getTo());
	}
}