package net.evmodder.EvLib;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CommandBase implements TabExecutor{
//	protected EvPlugin plugin;
	String commandName;
	final static CommandExecutor disabledCmdExecutor = new CommandExecutor(){
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
			sender.sendMessage(ChatColor.RED+"This command is currently unavailable");
			return true;
		}
	};

//	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){return null;}

	public CommandBase(JavaPlugin pl, boolean enabled){
//		plugin = p;
		commandName = getClass().getSimpleName().substring(7).toLowerCase();
		PluginCommand cmd = pl.getCommand(commandName);
		if(enabled){cmd.setExecutor(this); cmd.setTabCompleter(this);}
		else cmd.setExecutor(disabledCmdExecutor);
	}

	public CommandBase(JavaPlugin pl){
		this(pl, true);
	}
}