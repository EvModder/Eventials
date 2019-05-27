package Eventials.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import Eventials.Eventials;
import net.evmodder.EvLib.CommandBase;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.ButcherUtils;
import net.evmodder.EvLib.extras.ButcherUtils.KillFlag;

public class CommandClearEntities extends CommandBase {

	public CommandClearEntities(EvPlugin p){
		super(p);
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
		final List<String> tabCompletes = new ArrayList<String>();
		String arg = args[args.length-1].toLowerCase();
		if("animals".startsWith(arg)) tabCompletes.add("animals");
		if("tiles".startsWith(arg)) tabCompletes.add("tiles");
		if("unique".startsWith(arg)) tabCompletes.add("unique");
		if("equipped".startsWith(arg)) tabCompletes.add("equipped");
		if("named".startsWith(arg)) tabCompletes.add("named");
		return tabCompletes;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		World world = null;
		if(sender instanceof Player)world = ((Player) sender).getWorld();
		boolean badWorld = false;
		HashMap<KillFlag, Boolean> flags = new HashMap<KillFlag, Boolean>();

		for(String arg : args){
			arg = arg.toLowerCase();

			if((world = Eventials.getPlugin().getServer().getWorld(arg)) == null){
				arg = arg.replace("-", "");
				if(arg.equals("a") || arg.contains("animal")) flags.put(KillFlag.ANIMALS, true);
				else if(arg.equals("t") || arg.contains("tile")) flags.put(KillFlag.TILE, true);
				else if(arg.equals("n") || arg.contains("name")) flags.put(KillFlag.NAMED, true);
				else if(arg.equals("c") || arg.contains("close") || arg.contains("nearby"))
					flags.put(KillFlag.NEARBY, true);
				else if(arg.equals("e") || arg.contains("equip")) flags.put(KillFlag.EQUIPPED, true);
				else if(arg.equals("u") || arg.contains("unique")) flags.put(KillFlag.UNIQUE, true);
				else if(arg.length() > 1) badWorld = true;
			}
		}
		if(world == null && badWorld){
			sender.sendMessage(ChatColor.RED+"Could not find the specified world!");
			return true;
		}

		int numKilled = ButcherUtils.clearEntitiesByWorld(world, flags);
		if(world != null) sender.sendMessage(ChatColor.GRAY+"Brutally murdered "+numKilled
											+" entities in world: "+world.getName());
		else sender.sendMessage(ChatColor.GRAY+"Brutally murdered "+ChatColor.ITALIC+ChatColor.BOLD
											+"all"+ChatColor.GRAY+" specificed entities! ("+numKilled+")");

		return true;
	}
}
