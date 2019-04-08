package Eventials.economy.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import Eventials.Eventials;
import Eventials.economy.Economy;

public class ChunkGenerateListener implements Listener{
	enum FailureResult{TP_SPAWN, NOTHING};
	final double DEFAULT_COST;
	final HashMap<String, Double> chunkGenCosts;
	final FailureResult result;
	final HashMap<UUID, Double> currentDebt;
	final HashSet<UUID> noSpam;
	final Eventials pl;

	public ChunkGenerateListener(){
		pl = Eventials.getPlugin();
		DEFAULT_COST = pl.getConfig().getDouble("default-chunk-generate-cost", 0.25);
		chunkGenCosts = new HashMap<String, Double>();
		ConfigurationSection costConfig = pl.getConfig().getConfigurationSection("chunk-generate-cost");
		if(costConfig != null) for(String worldName : costConfig.getKeys(false)){
			World w = pl.getServer().getWorld(worldName);
			if(w == null) pl.getLogger().warning("WORLD NOT FOUND: \""+worldName+"\" (from config)");
			else{
				chunkGenCosts.put(w.getName(), costConfig.getDouble(worldName, DEFAULT_COST));
			}
		}
		result = FailureResult.valueOf(pl.getConfig().getString("chunk-generate-failure", "TP_SPAWN"));
		currentDebt = new HashMap<UUID, Double>();
		noSpam = new HashSet<UUID>();
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
			Player player = null;
			double dSqNearest = Double.MAX_VALUE;
			for(Player p : evt.getWorld().getPlayers()){
				double dSq = p.getLocation().distance(evt.getChunk().getBlock(7, 127, 7).getLocation());
				if(dSq < dSqNearest){
					dSqNearest = dSq;
					player = p;
				}
			}
			if(dSqNearest < 200*200){
				final double CHUNK_GEN_COST = chunkGenCosts.getOrDefault(evt.getWorld().getName(), DEFAULT_COST);
				final UUID uuid = player.getUniqueId();
				if(Economy.getEconomy().playerToServer(uuid, CHUNK_GEN_COST)){
					Double has = currentDebt.get(uuid);
					currentDebt.put(uuid, (has == null ? 0 : has) + CHUNK_GEN_COST);
					if(!noSpam.contains(uuid)){
						noSpam.add(uuid);
						pl.getServer().getScheduler().scheduleSyncDelayedTask(pl,
							new Runnable(){@Override public void run(){noSpam.remove(uuid);
						}}, 4*20);

						double amt = trimZeros(currentDebt.remove(uuid));
						int numGen = (int)(amt/CHUNK_GEN_COST);
						player.sendMessage(""+ChatColor.GRAY+ChatColor.BOLD+"Notice: "+
								ChatColor.YELLOW+Math.abs(amt)+ChatColor.DARK_GREEN+'L'+
								ChatColor.GRAY+" was transferred "+
								(amt < 0 ? "to you from" : "from you to")+
								ChatColor.GOLD+" Server Bank\n"+
								ChatColor.GRAY+ChatColor.ITALIC+"This fee was from you generating "+
								ChatColor.YELLOW+ChatColor.ITALIC+numGen+
								ChatColor.GRAY+ChatColor.ITALIC+" new chunk"+(numGen == 1 ? "" : "s"));
					}

				}
				else{
					player.sendMessage(ChatColor.RED+"You do not have enough money to generate new chunks");
					switch(result){
						case TP_SPAWN:
							player.teleport(new Location(player.getWorld(), 0, 65, 0));//TODO: specify spawn more generally
							break;
						case NOTHING:
							break;
					}
					evt.getChunk().unload(false, false);
				}
			}//endif must be within 200 blocks of chunk
		}//endif must be a new chunk
	}
}