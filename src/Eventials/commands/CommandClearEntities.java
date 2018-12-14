package Eventials.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import EvLib.CommandBase2;
import EvLib.EvPlugin;
import Eventials.Eventials;
import Extras.Extras;
import net.md_5.bungee.api.ChatColor;

public class CommandClearEntities extends CommandBase2 {

	public CommandClearEntities(EvPlugin p){
		super(p);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		World world = null;
		if(sender instanceof Player)world = ((Player) sender).getWorld();
		boolean badWorld = false;
		boolean hostile = false, animal = false, complex = false, nonliving = false, named = false;
		boolean ignoreNearby = !(sender instanceof Player);//<-- Do not butcher entities that are "nearby" to players

		for(String arg : args){
			arg = arg.toLowerCase();

			if((world = Eventials.getPlugin().getServer().getWorld(arg)) == null){
				arg = arg.replace("-", "");

				if(arg.equals("a") || arg.equals("animals")) animal = true;
				else if(arg.equals("h") || arg.equals("hostile") ||
						arg.equals("m") || arg.contains("monster")) hostile = true;
				else if(arg.equals("c") || arg.equalsIgnoreCase("complex")) complex = true;
				else if(arg.equals("e") || arg.equals("enviroment")) nonliving = true;
				else if(arg.equals("n") || arg.equals("named")) named = true;
				else if(arg.length() > 1) badWorld = true;
			}
		}
		if(!animal && !complex && !nonliving) hostile = true;

		if(world == null && badWorld){
			sender.sendMessage(ChatColor.RED+"Could not find the specified world!");
			return true;
		}

		int numKilled = Extras.clearEntitiesByWorld(world, hostile, animal, complex, nonliving, named, ignoreNearby);
		if(world != null) sender.sendMessage(ChatColor.GRAY+"Brutally murdered "+numKilled
											+" entities in world: "+world.getName());
		else sender.sendMessage(ChatColor.GRAY+"Brutally murdered "+ChatColor.ITALIC+ChatColor.BOLD
											+"all"+ChatColor.GRAY+" specificed entities! ("+numKilled+")");

		return true;
	}
}
