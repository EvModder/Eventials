package Eventials.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import Eventials.Eventials;
import net.evmodder.EvLib.CommandBase;
import net.evmodder.EvLib.EvPlugin;

public class CommandEventials extends CommandBase{

	public CommandEventials(EvPlugin p){
		super(p);
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String Label, String[] args){
		if(args.length == 1){
			final List<String> tabCompletes = new ArrayList<String>();
			args[0] = args[0].toLowerCase();
			if("reload".startsWith(args[0])) tabCompletes.add("reload");
			return tabCompletes;
		}
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//eventials <reload>
		//in plugin.yml: This command ONLY reloads settings! Does not reload .jar updates!
		if(args.length == 0){
			sender.sendMessage("Try /eventials reload :D");
			return true;
		}

		Eventials plugin = Eventials.getPlugin();
		HandlerList.unregisterAll(plugin);
		plugin.onDisable();
		plugin.onEnable();

		return true;
	}
}