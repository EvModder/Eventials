package Eventials.economy.listeners;

import java.util.LinkedList;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import Eventials.economy.Economy;
import net.evmodder.EvLib.EvPlugin;

public class _UNUSED_CurrencyLoseGainListener implements Listener{
	Material currency;
	EvPlugin plugin;
	LinkedList<Integer> chunkX, chunkZ;
	LinkedList<UUID> world;

	public _UNUSED_CurrencyLoseGainListener(){
		currency = Economy.getEconomy().getCurrency();
	}
}