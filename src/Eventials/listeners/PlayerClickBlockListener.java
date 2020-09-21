package Eventials.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerClickBlockListener implements Listener{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockClicked(PlayerInteractEvent evt){
		if(evt.hasBlock() && evt.useInteractedBlock() == Result.ALLOW &&
				(!evt.getPlayer().isSneaking() || evt.getItem() == null)
				&& evt.getClickedBlock().getType() == Material.END_PORTAL_FRAME
				&& ((EndPortalFrame)evt.getClickedBlock().getState()).hasEye()){
			((EndPortalFrame)evt.getClickedBlock().getState()).setEye(false);
			evt.setUseItemInHand(Result.DENY);
			Location loc = evt.getClickedBlock().getLocation();
			loc.add(0, 0.1, 0);
			loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.ENDER_EYE, 1));
		}
	}
}
