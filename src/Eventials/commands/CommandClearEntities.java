package Eventials.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import Eventials.Eventials;
import Extras.Extras;
import net.evmodder.EvLib.CommandBase;
import net.evmodder.EvLib.EvPlugin;
import net.md_5.bungee.api.ChatColor;

public class CommandClearEntities extends CommandBase {

	public CommandClearEntities(EvPlugin p){
		super(p);
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
		final List<String> tabCompletes = new ArrayList<String>();
		String arg = args[args.length-1].toLowerCase();
		if("animals".startsWith(arg)) tabCompletes.add("animals");
		if("hostile".startsWith(arg)) tabCompletes.add("hostile");
		if("monsters".startsWith(arg)) tabCompletes.add("monsters");
		if("complex".startsWith(arg)) tabCompletes.add("complex");
		if("enviroment".startsWith(arg)) tabCompletes.add("enviroment");
		if("named".startsWith(arg)) tabCompletes.add("named");
		return tabCompletes;
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

				if(arg.equals("a") || arg.startsWith("animal")) animal = true;
				else if(arg.equals("h") || arg.startsWith("hostile") ||
						arg.equals("m") || arg.startsWith("monster")) hostile = true;
				else if(arg.equals("c") || arg.startsWith("complex")) complex = true;
				else if(arg.equals("e") || arg.startsWith("enviroment")) nonliving = true;
				else if(arg.equals("n") || arg.startsWith("name")) named = true;
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
