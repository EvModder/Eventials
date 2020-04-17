 package Eventials.spawners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.TypeUtils;

public class PlayerInteractEntityListener implements Listener {
	private Eventials plugin;
	final boolean GROW_SLIMES, DYE_SHULKERS;

	public PlayerInteractEntityListener(){
		plugin = Eventials.getPlugin();
		GROW_SLIMES = plugin.getConfig().getBoolean("feed-slimes", true);
		DYE_SHULKERS = plugin.getConfig().getBoolean("dye-shulkers", true);
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent evt){
		ItemStack heldItem = evt.getHand() == EquipmentSlot.HAND
				? evt.getPlayer().getInventory().getItemInMainHand() :  evt.getPlayer().getInventory().getItemInOffHand();
		if(heldItem == null) return;

		if(evt.getRightClicked().getType() == EntityType.SLIME && GROW_SLIMES){
			if(heldItem.getType() == Material.SLIME_BLOCK){
				Slime slime = ((Slime)evt.getRightClicked());
				int newAmt = heldItem.getAmount()-slime.getSize();
				if(newAmt >= 0){
					heldItem.setAmount(newAmt);
					evt.getPlayer().getInventory().setItemInMainHand(heldItem.getAmount() > 0 ? heldItem : new ItemStack(Material.AIR));
					slime.setSize(slime.getSize()+1);
				}
			}
		}
		else if(evt.getRightClicked().getType() == EntityType.SHULKER && DYE_SHULKERS){
			if(TypeUtils.isDye(heldItem.getType())){
				((Shulker)evt.getRightClicked()).setColor(TypeUtils.getDyeColor(heldItem.getType()));
				int newAmt = heldItem.getAmount() - 1;
				if(newAmt == 0) evt.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
				else {
					heldItem.setAmount(newAmt);
					evt.getPlayer().getInventory().setItemInMainHand(heldItem);
				}
			}
		}
	}
}