package EventAndMisc;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.projectiles.ProjectileSource;
import Eventials.Eventials;

public class FactionsProtectPatch implements Listener{
	final Eventials pl;

	FactionsProtectPatch(Eventials plugin){
		pl = plugin;
	}

	public boolean isProtected(EntityType entity){
		switch(entity){
			case ARMOR_STAND:
			case PAINTING:
			case ITEM_FRAME:
			case LEASH_HITCH:
				return true;
			default:
				return false;
		}
	}
	public void cancelIfCantPlace(Cancellable evt, Player player, Block block){
		BlockPlaceEvent testEvt = new BlockPlaceEvent(block, block.getState(), block,
				player.getInventory().getItemInMainHand(), player, true, EquipmentSlot.HAND);
		pl.getServer().getPluginManager().callEvent(testEvt);
		if(testEvt.isCancelled()) evt.setCancelled(true);
	}
	public void cancelIfCantBreak(Cancellable evt, Player player, Block block){
		BlockBreakEvent testEvt = new BlockBreakEvent(block, player);
		pl.getServer().getPluginManager().callEvent(testEvt);
		if(testEvt.isCancelled()) evt.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityInteract(PlayerInteractEntityEvent evt){
		if(evt.isCancelled() || !isProtected(evt.getRightClicked().getType())) return;
		Location loc = evt.getRightClicked().getLocation();
		if(loc == null || loc.getBlock() == null) return;
		cancelIfCantPlace(evt, evt.getPlayer(), loc.getBlock());
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityInteract(PlayerInteractAtEntityEvent evt){
		if(evt.isCancelled() || !isProtected(evt.getRightClicked().getType())) return;
		Location loc = evt.getRightClicked().getLocation();
		if(loc == null || loc.getBlock() == null) return;
		cancelIfCantPlace(evt, evt.getPlayer(), loc.getBlock());
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityInteract(EntityDamageByEntityEvent evt){
		if(evt.isCancelled() || !isProtected(evt.getEntity().getType())) return;
		Location loc = evt.getEntity().getLocation();
		if(loc == null || loc.getBlock() == null) return;

		Player player = null;
		if(evt.getDamager() instanceof Player) player = (Player) evt.getDamager();
		if(evt.getDamager() instanceof Projectile){
			ProjectileSource shooter = ((Projectile)evt.getDamager()).getShooter();
			if(shooter instanceof Player) player = (Player) shooter;
		}
		if(player == null) return;
		cancelIfCantBreak(evt, player, loc.getBlock());
	}
}
