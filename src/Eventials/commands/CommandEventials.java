package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import EvLib.CommandBase2;
import EvLib.EvPlugin;
import Eventials.Eventials;

public class CommandEventials extends CommandBase2{

	public CommandEventials(EvPlugin p){
		super(p);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//eventials reload
		//in plugin.yml: This command ONLY reloads settings! Does not reload .jar updates!
		if(args.length == 0){
			sender.sendMessage("Do /eventials reload :D");
			return false;
		}

		Eventials plugin = Eventials.getPlugin();
		HandlerList.unregisterAll(plugin);
		plugin.onDisable();
		plugin.onEnable();

		return true;
	}
}