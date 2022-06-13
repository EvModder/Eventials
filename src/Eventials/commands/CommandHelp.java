package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.bukkit.ChatColor;
import Eventials.Eventials;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.CommandUtils;

public class CommandHelp extends EvCommand{
	private final EvPlugin pl;
	private final boolean SHOW_ALIASES_IN_TAB_COMPLETE = true;

	public CommandHelp(Eventials p){
		super(p);
		pl = p;
	}

	private Command findCommand(String label){
		//TODO: non-plugin commands
		//TODO: command aliases
		return pl.getServer().getPluginCommand(label);
	}

	private boolean canAccess(CommandSender s, String cmd){
		//TODO: non-plugin commands
		PluginCommand pc = pl.getServer().getPluginCommand(cmd);
		if(pc == null) return true;//TODO: default true/false?
		if(pc.getPermission() != null) return s.hasPermission(pc.getPermission());
		return s.hasPermission(pc.getPlugin().getName()+"."+cmd) || s.hasPermission(pc.getPlugin().getName()+".command."+cmd);
	}

	@SuppressWarnings("unchecked")
	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){
		if(args.length != 1) return ImmutableList.of();
		List<String> results = new ArrayList<>();
		if(s.hasPermission("eventials.help.plugin")){
			results.addAll(Stream.of(pl.getServer().getPluginManager().getPlugins())
					.map(p -> p.getName()).filter(pName -> pName.startsWith(args[0])).toList());
		}
		if(s.hasPermission("eventials.help.command")){
			results.addAll(Stream.of(pl.getServer().getPluginManager().getPlugins())
					.flatMap(p -> p.getDescription().getCommands().keySet().stream())
					.filter(cmdName -> cmdName.startsWith(args[0]) && canAccess(s, cmdName)).toList());
			if(SHOW_ALIASES_IN_TAB_COMPLETE){
				results.addAll(Stream.of(pl.getServer().getPluginManager().getPlugins())
						.flatMap(p -> p.getDescription().getCommands().entrySet().stream())
						.filter(cmdEntry -> canAccess(s, cmdEntry.getKey()))
						.flatMap(cmdEntry -> {
							Object aliases = cmdEntry.getValue().get("aliases");
							return aliases == null
									? Stream.of()
									: aliases instanceof String
										? Stream.of((String)aliases)
										: aliases instanceof List
											? ((List<String>)aliases).stream()
											: Stream.of("ERROR - unknown alias type in eventials:help");
						})
						.filter(aliasName -> aliasName.startsWith(args[0])).toList());
			}
			//TODO: non-plugin commands
		}
		return results;
	}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		int targetPage = 1;
		Command targetCmd = null;
		Plugin targetPlugin = null;
		if(args.length > 1){
			sender.sendMessage(ChatColor.RED+"Too many arguments");
			return false;
		}
		if(args.length == 1){
			try{targetPage = Math.max(Integer.parseInt(args[0]), 1);}
			catch(IllegalArgumentException ex){
				targetCmd = findCommand(args[0]);
				if(targetCmd == null) targetPlugin = pl.getServer().getPluginManager().getPlugin(args[0]);
			}
		}
		if(targetCmd != null){
			CommandUtils.showCommandHelp(sender, targetCmd);
		}
		else if(targetPlugin != null){
			//TODO: show "about" & commands for a plugin
		}
		else{
			CommandUtils.showFancyHelp(sender, targetPage);
		}
		return true;
	}
}