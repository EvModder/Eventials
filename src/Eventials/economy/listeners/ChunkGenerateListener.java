package Eventials.economy.listeners;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import Eventials.Eventials;
import Eventials.economy.Economy;

public class ChunkGenerateListener implements Listener{
	enum FailureResult{TP_SPAWN};
	final double CHUNK_GEN_COST;
	final FailureResult result;
	HashMap<UUID, Double> moneySpam;
	Eventials pl;

	public ChunkGenerateListener(){
		pl = Eventials.getPlugin();
		CHUNK_GEN_COST = pl.getConfig().getDouble("chunk-generate-cost", 0.25);
		result = FailureResult.valueOf(pl.getConfig().getString("chunk-generate-failure", "TP_SPAWN"));
		moneySpam = new HashMap<UUID, Double>();
	}

	double trimZeros(double x){
		for(double place = 10000; place > 1; place /= 10){
			double trimmed = Math.round(x*place)/place;
			if(x - trimmed < 1/(place*10)) x = trimmed;
			else return x;
		}
		return x;
	}

	@SuppressWarnings("deprecation") @EventHandler
	public void onChunkGenerate(ChunkLoadEvent evt){
		if(evt.isNewChunk()){
			UUID nearest = null;
			double dSqNearest = Double.MAX_VALUE;
			for(Player p : evt.getWorld().getPlayers()){
				double dSq = p.getLocation().distance(evt.getChunk().getBlock(7, 127, 7).getLocation());
				if(dSq < dSqNearest){
					dSqNearest = dSq;
					nearest = p.getUniqueId();
				}
			}
			if(dSqNearest < 200*200){
				if(Economy.getEconomy().playerToServer(nearest, CHUNK_GEN_COST)){
					Double has = moneySpam.get(nearest);
					moneySpam.put(nearest, (has == null ? 0 : has) + CHUNK_GEN_COST);
					if(has == null){
						final UUID uuid = nearest;
						pl.getServer().getScheduler().scheduleSyncDelayedTask(pl, new Runnable(){
							@Override public void run(){
								Player p = pl.getServer().getPlayer(uuid);
								double amt = trimZeros(moneySpam.remove(uuid));
								if(p != null){
									int numGen = (int)(amt/CHUNK_GEN_COST);
									p.sendMessage(""+ChatColor.GRAY+ChatColor.BOLD+"Notice: "+
										ChatColor.YELLOW+Math.abs(amt)+ChatColor.DARK_GREEN+'L'+
										ChatColor.GRAY+" was transferred "+
										(amt < 0 ? "to you from" : "from you to")+
										ChatColor.GOLD+" Server Bank\n"+
										ChatColor.GRAY+ChatColor.ITALIC+"This fee was from you generating "+
										ChatColor.YELLOW+ChatColor.ITALIC+numGen+
										ChatColor.GRAY+ChatColor.ITALIC+" new chunk"+(numGen == 1 ? "" : "s"));
								}
							}
						}, 20);
					}

				}
				else{
					Player p = pl.getServer().getPlayer(nearest);
					p.sendMessage(ChatColor.RED+"You do not have enough money to generate new chunks");
					if(result == FailureResult.TP_SPAWN)
						p.teleport(new Location(p.getWorld(), 0, 65, 0));//TODO: specify spawn more generally
					evt.getChunk().unload(false, false);
				}
			}//endif must be within 200 blocks of chunk
		}//endif must be a new chunk
	}
}