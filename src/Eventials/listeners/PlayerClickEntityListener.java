package Eventials.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerClickEntityListener implements Listener{
	@EventHandler
	public void onEntityClicked(PlayerInteractEntityEvent evt){
		if(evt.getPlayer().getGameMode() == GameMode.CREATIVE && !evt.getPlayer().isSneaking()
				&& evt.getPlayer().getInventory().getItemInMainHand().getType() == Material.SADDLE){
			evt.getRightClicked().addPassenger(evt.getPlayer());
		}
	}
}
