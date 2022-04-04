package Eventials.economy.listeners;

import java.util.LinkedList;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import Eventials.Eventials;
import Eventials.economy.EvEconomy;
import net.evmodder.EvLib.EvPlugin;

public class _UNUSED_ChunkPopulateListener implements Listener {
	Material currency;
	EvPlugin plugin;
	LinkedList<Integer> chunkX, chunkZ;
	LinkedList<UUID> world;

	public _UNUSED_ChunkPopulateListener(){
		currency = EvEconomy.getEconomy().getCurrency();
		plugin = Eventials.getPlugin();
		chunkX = new LinkedList<>();
		chunkZ = new LinkedList<>();
		world = new LinkedList<>();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkGenerate(ChunkPopulateEvent evt){
		chunkX.add(evt.getChunk().getX());
		chunkZ.add(evt.getChunk().getZ());
		world.add(evt.getWorld().getUID());

		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
			@Override public void run(){
				Chunk chunk = plugin.getServer().getWorld(world.remove()).getChunkAt(chunkX.remove(), chunkZ.remove());
				int newMoney = 0;
				Eventials.getPlugin().getLogger().info("new chunk "+chunk.getX()+","+chunk.getZ());
				for(int x=0; x<16; ++x)
					for(int y=60; y<100; ++y)
						for(int z=0; z<16; ++z)
							if(chunk.getBlock(x, y, z).getType() == currency) ++newMoney;

				if(newMoney > 0){
					EvEconomy.getEconomy().addGlobalBal(newMoney);
					Eventials.getPlugin().getLogger().info("Added to global bal: "+newMoney);
				}
			}
		}, evt.isAsynchronous() ? 200 : 20);
	}
}