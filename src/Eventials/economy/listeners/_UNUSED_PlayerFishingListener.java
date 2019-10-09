package Eventials.economy.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import Eventials.economy.EvEconomy;

public class _UNUSED_PlayerFishingListener implements Listener {
	Material currency;
	public _UNUSED_PlayerFishingListener(){
		currency = EvEconomy.getEconomy().getCurrency();
	}

	@EventHandler
	public void onFishCatch(PlayerFishEvent evt){
		if(evt.getCaught() != null && evt.getCaught() instanceof Item
				&& ((Item)evt.getCaught()).getItemStack().getType() == currency){
			EvEconomy.getEconomy().addGlobalBal(((Item)evt.getCaught()).getItemStack().getAmount());
		}
	}
}