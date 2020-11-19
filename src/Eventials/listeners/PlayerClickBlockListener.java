package Eventials.listeners;

import java.util.Random;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import Eventials.Eventials;

public class PlayerClickBlockListener implements Listener{
	final double SHATTER_CHANCE;
	private Random rand;

	public PlayerClickBlockListener(){
		SHATTER_CHANCE = Eventials.getPlugin().getConfig().getDouble("remove-eye-of-ender-shatter-chance", 0.20D);
	}

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
	}
}
