package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import org.bukkit.ChatColor;
import Eventials.Eventials;
import net.evmodder.EvLib.CommandBase;

public class CommandRegionPos extends CommandBase {

	public CommandRegionPos(Eventials pl) {
		super(pl);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		Player p = (Player) sender;

		int localX = (int)Math.floor(p.getLocation().getChunk().getX() / 32.0);
		int localZ = (int)Math.floor(p.getLocation().getChunk().getZ() / 32.0);

		p.sendMessage("Currently in region r."+localX+'.'+localZ+".mca");
		return true;
	}
}