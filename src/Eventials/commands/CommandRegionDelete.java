package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import EvLib.CommandBase2;
import Eventials.Eventials;

public class CommandRegionDelete extends CommandBase2 {

	public CommandRegionDelete(Eventials pl) {
		super(pl);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		int localX, localZ;
		World world;
		if((sender instanceof Player && args.length == 2) || args.length == 3){
			if(args.length == 3){
				world = sender.getServer().getWorld(args[2]);
				if(world == null){
					world = sender.getServer().getWorld(args[0]);
					if(world == null){
						sender.sendMessage(ChatColor.RED+"Unknown world: "+ChatColor.GRAY+args[0]);
						return false;
					}
					args[0] = args[1];
					args[1] = args[2];
				}
			}
			else world = ((Player)sender).getWorld();
			try{
				localX = Integer.parseInt(args[0]);
				localZ = Integer.parseInt(args[1]);
			}catch(NumberFormatException ex){
				sender.sendMessage(ChatColor.RED+"Invalid coordinates! Please check for typos");
				return false;
			}
		}
/* How on earth are you supposed to delete the region you are standing in?
		else if(args.length == 0 && sender instanceof Player){
			world = ((Player)sender).getWorld(); 
			localX = ((Player)sender).getLocation().getChunk().getX() / 32;
			localZ = ((Player)sender).getLocation().getChunk().getZ() / 32;
		}*/
		else{
			sender.sendMessage(ChatColor.RED+"Please specify X and Z arguments");
			return false;
		}

		for(Chunk chunk : world.getLoadedChunks()){
			if(chunk.getX()/32 == localX && chunk.getZ()/32 == localZ){
				sender.sendMessage("Unable to delete this region while chunks from it are loaded!");
				return false;
			}
		}

		String filename = "./"+world.getName()+"/region/r."+localX+'.'+localZ+".mca";
		if(new File(filename).delete() == false){
			sender.sendMessage(ChatColor.RED+"Failed to delete the file: "+filename);
			sender.sendMessage(ChatColor.RED+"Perhaps is was already deleted or hasn't been generated yet?");
			return false;
		}
		sender.sendMessage(ChatColor.GOLD+"Deleted region file: "+filename);
		return true;
	}
}