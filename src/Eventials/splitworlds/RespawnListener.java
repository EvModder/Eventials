package Eventials.splitworlds;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;

class RespawnListener implements Listener {
	final private Eventials plugin;
	RespawnListener(){plugin = Eventials.getPlugin();}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(final PlayerRespawnEvent evt){
		String deathWorld = evt.getPlayer().getWorld().getName();
		String respawnWorld = evt.getRespawnLocation().getWorld().getName();
		if(SplitWorlds.inSharedInvGroup(deathWorld, respawnWorld)) return;
		plugin.getLogger().info("Died in: "+deathWorld+", Respawning in: "+respawnWorld);

		// No need to vaccinate or clear inventory since player is currently dead.
		// Loading the respawnWorld's inventory will override the player's location, so we need need to
		// teleport them back to the respawn point immediately afterward
		final UUID playerUUID = evt.getPlayer().getUniqueId();
		new BukkitRunnable(){@Override public void run(){
			Player player = plugin.getServer().getPlayer(playerUUID);
			if(player == null || player.getWorld().getName().equals(respawnWorld) == false) return;

			SplitWorlds.loadProfile(player, player.getUniqueId(), respawnWorld, true, /*false*/true, /*false*/true);
			SplitWorldUtils.untrackedTeleport(player, evt.getRespawnLocation(), true);
		}}.runTaskLater(Eventials.getPlugin(), 1);
	}
}