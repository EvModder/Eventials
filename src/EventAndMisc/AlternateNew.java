package EventAndMisc;

import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;
import net.evmodder.EvLib.FileIO;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;

public class AlternateNew implements Listener{
	class Pair<T, R>{
		T a; R b;
		Pair(T t, R r){a=t; b=r;}
		@Override public boolean equals(Object p){
			return p != null && p instanceof Pair && a.equals(((Pair<?, ?>)p).a) && b.equals(((Pair<?, ?>)p).b);
		}
		@Override public int hashCode(){
			return a.hashCode() + b.hashCode();
		}
	};
	private final HashSet<Pair<Integer,Integer>> modChunks = new HashSet<Pair<Integer, Integer>>();
	private final Eventials pl;

	public AlternateNew(){
		pl = Eventials.getPlugin();
		pl.getServer().getPluginManager().registerEvents(this, pl);
		pl.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), pl);
		pl.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), pl);
		loadModChunks();
	}

	void loadModChunks(){
		for(String chunk : FileIO.loadFile("leafdecaychunks.txt", "").split(" ")){
			int i = chunk.indexOf(',');
			if(i != -1){
				try{
					int x = Integer.parseInt(chunk.substring(0, i)), y = Integer.parseInt(chunk.substring(i+1));
					modChunks.add(new Pair<Integer, Integer>(x, y));
				}
				catch(NumberFormatException ex){}
			}
		}
	}
	void saveModChunks(){
		StringBuilder builder = new StringBuilder();
		for(Pair<Integer, Integer> chunk : modChunks)
			builder.append(chunk.a).append(',').append(chunk.b).append(' ');
		FileIO.saveFile("leafdecaychunks.txt", builder.toString());
		updateFile = false;
	}

	private boolean updateFile = false;
	public void addModChunk(Chunk c){
		if(modChunks.add(new Pair<Integer, Integer>(c.getX(), c.getZ()))){
			if(!updateFile){
				updateFile = true;
				new BukkitRunnable(){
					@Override public void run(){
						saveModChunks();
					}
				}.runTaskLater(pl, 20*60*10);//every 10 minutes
			}
		}
	}

	@EventHandler
	public void onLeafDecay(LeavesDecayEvent evt){
		if(!evt.isCancelled()){
			int x = evt.getBlock().getChunk().getX(), z = evt.getBlock().getChunk().getZ();
			if(!modChunks.contains(new Pair<Integer, Integer>(x, z))) evt.setCancelled(true);
		}
	}

	@EventHandler
	public void onTreeGrow(StructureGrowEvent evt){
		if(isWoodTree(evt.getSpecies())){
			addModChunk(evt.getBlocks().iterator().next().getChunk());
		}
	}

	@EventHandler
	public void onVinePlant(BlockPlaceEvent evt){
		if(evt.getBlockPlaced().getType() == Material.VINE){
			addModChunk(evt.getBlockPlaced().getChunk());
		}
	}

	public boolean isWoodTree(TreeType tree){
		switch(tree){
			case CHORUS_PLANT:
			case BROWN_MUSHROOM:
			case RED_MUSHROOM:
				return false;
			default:
				return true;
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent evt){
		FPlayer fplayer = (FPlayer) FPlayers.i.get(evt.getPlayer());
		if(!fplayer.hasFaction()) evt.setFormat(ChatColor.GRAY+"%s"+ChatColor.RESET+" %s");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerBreakBlock(BlockBreakEvent evt){
		if(evt.isCancelled() &&
				Math.abs(evt.getBlock().getLocation().getBlockX()) < 240 &&
				Math.abs(evt.getBlock().getLocation().getBlockZ()) < 240){
			evt.getPlayer().sendMessage(ChatColor.GRAY+"> "+ChatColor.AQUA+"Travel out "
				+ChatColor.GRAY+"240"+ChatColor.AQUA+" blocks to edit terrain "+ChatColor.GRAY+":)");
		}
	}

	@EventHandler
	public void onPreCommand(PlayerCommandPreprocessEvent evt){
		String command = evt.getMessage().toLowerCase();
		command = command.substring(0, command.indexOf(' '));

		if(command.contains("sethome") && !evt.getPlayer().hasPermission("essentials.sethome")){
			evt.getPlayer().sendMessage(ChatColor.AQUA+"/sethome"+ChatColor.WHITE+" isn't enabled.");
			evt.getPlayer().sendMessage("Instead, use a bed to set your "+ChatColor.AQUA+"/home");
			evt.setCancelled(true);
		}
	}
}