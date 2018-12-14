package Eventials.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener{
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent evt){
		if(/*evt.getInventory().getType() == InventoryType.COMMAND_BLOCK &&*/
				evt.getPlayer().hasPermission("evp.evm.commandblockcolor")){
			//TODO:
			//get blockstate, translate color, save back to blockstate
		}
	}
}
