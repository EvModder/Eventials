package Eventials.commands;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import org.bukkit.ChatColor;
import Eventials.Eventials;
import net.evmodder.EvLib.EvCommand;

public class CommandRegionTp extends EvCommand {
	public CommandRegionTp(Eventials pl){
		super(pl);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){
		//TODO: tab complete (available regions?)
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return false;
		}
		if(args.length != 1){
			sender.sendMessage(ChatColor.RED+"Invalid number of arguments");
			return false;
		}
		Player p = (Player) sender;

		String coords[] = args[0].replace("r.", "").replace(".mca", "").split("\\.");

		int localX=0, localZ=0;
		try{
			localX = Integer.parseInt(coords[0]);
			localZ = Integer.parseInt(coords[1]);
		}
		catch(NumberFormatException ex){p.sendMessage(ChatColor.RED+"The specified region file is invalid");}

		Chunk chunk = p.getWorld().getChunkAt(localX * 32, localZ * 32);
		p.teleport(chunk.getBlock(5, 150, 5).getLocation());//Teleport

		localX = (int)Math.floor(chunk.getX() / 32.0);
		localZ = (int)Math.floor(chunk.getZ() / 32.0);
		p.sendMessage("Currently in region r."+localX+'.'+localZ+".mca");

		return true;
	}
}