package EvLib;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CommandBase2 implements TabExecutor{
//	protected EvPlugin plugin;
	String commandName;
	final static CommandExecutor disabledCmdExecutor = new CommandExecutor(){
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
			sender.sendMessage(ChatColor.RED+"This command is currently unavailable");
			return true;
		}
	};

	// So subclasses aren't forced implement onTabComplete()
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String Label, String[] args){return null;}

	public CommandBase2(JavaPlugin pl, boolean enabled){
//		plugin = p;
		commandName = getClass().getSimpleName().substring(7).toLowerCase();
		pl.getCommand(commandName).setExecutor(enabled ? this : disabledCmdExecutor);//TODO: fix? test?
	}

	public CommandBase2(JavaPlugin pl){
		this(pl, true);
	}
}