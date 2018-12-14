 package Eventials.spawners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import Eventials.Eventials;

public class PlayerInteractEntityListener implements Listener {
	private Eventials plugin;
	final boolean feedSlimes;

	public PlayerInteractEntityListener(){
		plugin = Eventials.getPlugin();
		feedSlimes = plugin.getConfig().getBoolean("feed-slimes");
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent evt){
		if(evt.getRightClicked().getType() == EntityType.SLIME && feedSlimes){
			ItemStack heldItem = evt.getPlayer().getInventory().getItemInMainHand();
			Slime slime = ((Slime)evt.getRightClicked());

			if(heldItem != null && heldItem.getType() == Material.SLIME_BLOCK){
				int newAmt = heldItem.getAmount()-slime.getSize();
				if(newAmt >= 0){
					heldItem.setAmount(newAmt);
					evt.getPlayer().getInventory().setItemInMainHand(heldItem.getAmount() > 0 ? heldItem : new ItemStack(Material.AIR));
					slime.setSize(slime.getSize()+1);
				}
			}
		}
	}
}