package Eventials.economy.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import Eventials.economy.Economy;

public class _UNUSED_PlayerFishingListener implements Listener {
	Material currency;
	public _UNUSED_PlayerFishingListener(){
		currency = Economy.getEconomy().getCurrency();
	}

	@EventHandler
	public void onFishCatch(PlayerFishEvent evt){
		if(evt.getCaught() != null && evt.getCaught() instanceof Item
				&& ((Item)evt.getCaught()).getItemStack().getType() == currency){
			Economy.getEconomy().addGlobalBal(((Item)evt.getCaught()).getItemStack().getAmount());
		}
	}
}