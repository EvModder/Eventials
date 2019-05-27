package Eventials.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.evmodder.EvLib.CommandBase;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.GhostUtils;
import Eventials.Eventials;

public class CommandGhost extends CommandBase{
	GhostUtils ghostFactory;
	
	public CommandGhost(EvPlugin p){
		super(p);
		ghostFactory = new GhostUtils(p);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@SuppressWarnings("deprecation") @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		Player p;
		if(args.length > 0) p = Eventials.getPlugin().getServer().getPlayer(args[0]);
		else if(sender instanceof Player) p = (Player) sender;
		else{
			sender.sendMessage(ChatColor.RED+"Too few arguments!");
			return false;
		}
		if(p == null) sender.sendMessage(ChatColor.RED+"Could not find the specified player!");
		else{
			if(ghostFactory.isGhost(p)){
				ghostFactory.removeGhost(p);
				p.sendMessage(ChatColor.GRAY+"Ghost Mode disabled");
			}
			else{
				ghostFactory.addGhost(p);
				p.sendMessage(ChatColor.GRAY+"Ghost Mode enabled");
			}
			return true;
		}
		return true;
	}
}
