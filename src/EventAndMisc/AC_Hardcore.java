package EventAndMisc;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import Eventials.Eventials;

public class AC_Hardcore implements Listener{
	private final Eventials pl;

	public AC_Hardcore(){
		pl = Eventials.getPlugin();
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerBreakBlock(BlockBreakEvent evt){
		if(evt.isCancelled() &&
				Math.abs(evt.getBlock().getLocation().getBlockX()) < 240 &&
				Math.abs(evt.getBlock().getLocation().getBlockZ()) < 240){
			evt.getPlayer().sendMessage(ChatColor.GRAY+"> "+ChatColor.AQUA+"Travel out "
				+ChatColor.GRAY+"240"+ChatColor.AQUA+" blocks to edit terrain "+ChatColor.GRAY+":)");
		}
	}
}