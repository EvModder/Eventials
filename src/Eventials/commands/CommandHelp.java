package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.ChatColor;
import Eventials.Eventials;
import net.evmodder.EvLib.bukkit.EvCommand;
import net.evmodder.EvLib.bukkit.EvPlugin;
import net.evmodder.EvLib.bukkit.ReflectionUtils;
import net.evmodder.EvLib.bukkit.ReflectionUtils.RefClass;
import net.evmodder.EvLib.bukkit.ReflectionUtils.RefMethod;

public class CommandHelp extends EvCommand{
	private final EvPlugin pl;
	private final boolean SHOW_ALIASES_IN_TAB_COMPLETE = true;
	private static boolean SHOW_PERMISSIONLESS_CMDS = true;
	private HashMap<String, Set<Command>> commandsByName;

	public CommandHelp(Eventials p){
		super(p);
		pl = p;
		SHOW_PERMISSIONLESS_CMDS = p.getConfig().getBoolean("show-permissionless-commands-in-plugin-help", true);
	}
	
	private HashMap<String, Set<Command>> getCommandsByNameMap(){
		if(commandsByName == null){
			RefClass classCraftServer = ReflectionUtils.getRefClass("{cb}.CraftServer");
			RefMethod methodGetCommandMap = classCraftServer.getMethod("getCommandMap");
			commandsByName = new HashMap<>();
			((SimpleCommandMap)methodGetCommandMap.of(pl.getServer()).call()).getCommands().forEach(cmd -> {
				Set<Command> sameNameCmds = commandsByName.get(cmd.getName());
				if(sameNameCmds != null) sameNameCmds.add(cmd);
				else commandsByName.put(cmd.getName().toLowerCase(), new HashSet<>(Arrays.asList(cmd)));
			});
		}
		return commandsByName;
	}

//	private Command findCommand(String label){
//		return ((SimpleCommandMap)methodGetCommandMap.of(pl.getServer()).call()).getCommand(label);
//	}

//	private boolean canAccess(CommandSender s, String cmdName){
//		Command cmd = findCommand(cmdName);
//		if(cmd == null) return true;//TODO: default true/false?
//		if(cmd.getPermission() != null) return s.hasPermission(cmd.getPermission());
//		PluginCommand pc = pl.getServer().getPluginCommand(cmdName);
//		if(pc == null) return true;//TODO: default true/false?
////		if(pc.getPermission() != null) return s.hasPermission(pc.getPermission());
//		return s.hasPermission(pc.getPlugin().getName()+"."+cmd) || s.hasPermission(pc.getPlugin().getName()+".command."+cmd);
//	}
	public boolean canAccess(CommandSender s, Command cmd){
		if(cmd.getPermission() != null) return s.hasPermission(cmd.getPermission());
		if(cmd instanceof PluginCommand){
			String pluginName = ((PluginCommand)cmd).getPlugin().getName();
			if(s.hasPermission(pluginName+"."+cmd.getName()) || s.hasPermission(pluginName+".command."+cmd.getName())) return true;
			return false;//TODO: default true/false?
		}
		return true;//TODO: default true/false?
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){
		if(args.length != 1) return ImmutableList.of();
		List<String> results = new ArrayList<>();
		if(s.hasPermission("eventials.help.plugin")){
			results.addAll(Stream.of(pl.getServer().getPluginManager().getPlugins())
					.map(p -> p.getName()).filter(pName -> pName.startsWith(args[0])).toList());
		}
		if(s.hasPermission("eventials.help.command")){
			results.addAll(
					getCommandsByNameMap().entrySet().stream()
					.filter(e -> e.getKey().startsWith(args[0]))
					.filter(e -> e.getValue().stream().anyMatch(cmd -> canAccess(s, cmd)))
					.map(e -> e.getKey())
//1					((SimpleCommandMap)methodGetCommandMap.of(pl.getServer()).call()).getCommands().stream()
//1					.map(cmd -> cmd.getName())
//2					Stream.of(pl.getServer().getPluginManager().getPlugins())
//2					.flatMap(p -> p.getDescription().getCommands().keySet().stream())
//					.filter(cmdName -> cmdName.startsWith(args[0]) && canAccess(s, cmdName))
					.toList());
			if(SHOW_ALIASES_IN_TAB_COMPLETE){
				results.addAll(
						getCommandsByNameMap().values().stream()
						.flatMap(cmdSet -> cmdSet.stream())
						.filter(cmd -> canAccess(s, cmd))
						.flatMap(cmd -> cmd.getAliases().stream())
//						Stream.of(pl.getServer().getPluginManager().getPlugins())
//						.flatMap(p -> p.getDescription().getCommands().entrySet().stream())
//						.filter(cmdEntry -> canAccess(s, cmdEntry.getKey()))
//						.flatMap(cmd -> {
//							Object aliases = cmdEntry.getValue().get("aliases");
//							return aliases == null
//									? Stream.of()
//									: aliases instanceof String
//										? Stream.of((String)aliases)
//										: aliases instanceof List
//											? ((List<String>)aliases).stream()
//											: Stream.of("ERROR - unknown alias type in eventials:help");
//						})
						.filter(alias -> alias.startsWith(args[0])).toList());
			}
		}
		return results;
	}

	private static String getNamespace(Command cmd){
		//TODO: something more precise?
		if(cmd instanceof PluginCommand) return ((PluginCommand)cmd).getPlugin().getName().toLowerCase();
		if(cmd.getPermission() != null) return cmd.getPermission().substring(0,  cmd.getPermission().indexOf('.'));
		return "<?>";
	}
	private static final String graySep = ChatColor.GOLD+", "+ChatColor.GRAY;
	private static final String helpHeader = ChatColor.YELLOW+" ---- "+ChatColor.GOLD+"Help"+ChatColor.YELLOW+" ----------------\n";
	private static void showCommandHelp(CommandSender sender, Command cmd, boolean useNamespace){
		StringBuilder builder = new StringBuilder(helpHeader)
			.append(ChatColor.GOLD).append("Command ").append(ChatColor.RED).append('/')
			.append(useNamespace ? getNamespace(cmd)+":" : "")
			.append(cmd.getName()).append(ChatColor.GOLD).append(":\n")
			.append("Description: ").append(ChatColor.WHITE).append(cmd.getDescription()).append('\n').append(ChatColor.GOLD)
			.append("Usage: ").append(ChatColor.WHITE).append(cmd.getUsage()).append('\n').append(ChatColor.GOLD);
		if(cmd.getAliases() != null && !cmd.getAliases().isEmpty())
			builder.append("Aliases: ").append(ChatColor.WHITE).append(cmd.getAliases()).append('\n').append(ChatColor.GOLD);
		builder.append("Permission: ").append(ChatColor.WHITE).append(cmd.getPermission());
		sender.sendMessage(builder.toString());
	}

	private void showCommandHelpList(CommandSender sender, int pageNum){
		List<Command> commands =
				getCommandsByNameMap().values().stream()
				.flatMap(cmdSet -> cmdSet.stream())
				// Both 'tell' and 'msg' use 'minecraft.command.msg'
				.filter(cmd -> !cmd.getName().equals("icanhasbukkit") && !cmd.getName().equals("tell") && !cmd.getName().equals("w"))
				.filter(cmd -> canAccess(sender, cmd))
//				((SimpleCommandMap)methodGetCommandMap.of(pl.getServer()).call()).getCommands().stream()
//				.filter(cmd -> cmd.getPermission() == null || sender.hasPermission(cmd.getPermission()))
//				.map(cmd -> cmd.getName())
//				.filter(cmdName -> canAccess(sender, cmdName))
				.sorted((c1,c2) -> c1.getName().compareTo(c2.getName()))
				.toList();

		final int totalPages = (commands.size()-2)/9 + 1;
		if(pageNum > totalPages) pageNum = totalPages;

		//essentials-style help, minus the plugins.
		StringBuilder helpPage = new StringBuilder("").append(ChatColor.YELLOW).append(" ---- ")
				.append(ChatColor.GOLD).append("Help").append(ChatColor.YELLOW).append(" -- ").append(ChatColor.GOLD)
				.append("Page").append(ChatColor.RED).append(" ").append(pageNum).append(ChatColor.GOLD).append("/")
				.append(ChatColor.RED).append(totalPages).append(ChatColor.YELLOW).append(" ----");
		
		int i, startingVal = (pageNum-1)*9;
		for(i = startingVal; i < startingVal+9 && i < commands.size(); ++i){
			helpPage.append("\n").append(ChatColor.GOLD).append("/").append(commands.get(i).getName()).append(ChatColor.WHITE)
					.append(": ").append(commands.get(i).getDescription());
		}
		if(pageNum != totalPages){
			helpPage.append("\n").append(ChatColor.GOLD).append("Type ").append(ChatColor.RED).append("/help ")
					.append(pageNum+1).append(ChatColor.GOLD).append(" to read the next page.");
		}
		else if(i < commands.size()){
			helpPage.append("\n").append(ChatColor.GOLD).append("/").append(commands.get(i).getName()).append(ChatColor.WHITE)
					.append(": ").append(commands.get(i).getDescription());
		}
		sender.sendMessage(helpPage.toString());
	}

	private static void showPluginHelp(CommandSender sender, Plugin pl){
		pl.getLogger().info("yee");
		PluginDescriptionFile desc = pl.getDescription();
		StringBuilder builder = new StringBuilder(helpHeader)
			.append(ChatColor.GOLD).append("Plugin ").append(pl.isEnabled() ? ChatColor.GREEN : ChatColor.RED).append(pl.getName())
			.append(ChatColor.GOLD).append(" version ").append(ChatColor.GRAY).append(desc.getVersion()).append(ChatColor.GOLD).append(":\n")
			.append("Authors: ").append(ChatColor.GRAY).append(String.join(graySep, desc.getAuthors())).append(ChatColor.GOLD);
		if(desc.getWebsite() != null && !desc.getWebsite().isEmpty())
			builder.append("\nWebsite: ").append(ChatColor.AQUA).append(desc.getWebsite()).append(ChatColor.GOLD);
		builder.append("\nDescription: ").append(ChatColor.WHITE).append(desc.getDescription()).append(ChatColor.GOLD);
		if(desc.getCommands() != null && !desc.getCommands().isEmpty()){
			builder.append("\nCommands: ");
			boolean not1st = false;
			for(Entry<String, Map<String, Object>> entry : desc.getCommands().entrySet()){
				final boolean hasPerm = sender.hasPermission((String)entry.getValue().getOrDefault("permission", "*"));
				if(!SHOW_PERMISSIONLESS_CMDS && !hasPerm) continue;
				if(not1st) builder.append(graySep);
				else not1st = true;
				builder.append(hasPerm ? ChatColor.GRAY : ChatColor.RED).append('/').append(entry.getKey());
			};
		}
		sender.sendMessage(builder.toString());
	}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		int targetPage = 1;
		Set<Command> targetCmds = null;
		Plugin targetPlugin = null;
		if(args.length > 1){
			sender.sendMessage(ChatColor.RED+"Too many arguments");
			return false;
		}
		if(args.length == 1){
			try{targetPage = Math.max(Integer.parseInt(args[0]), 1);}
			catch(IllegalArgumentException ex){
				targetPlugin = pl.getServer().getPluginManager().getPlugin(args[0]);
				if(targetPlugin == null){
					targetCmds = getCommandsByNameMap().get(args[0].toLowerCase());
					if(targetCmds == null || targetCmds.isEmpty()){
						Command targetCmd = pl.getServer().getPluginCommand(args[0]);
						if(targetCmd == null){
							sender.sendMessage(ChatColor.RED+"Unknown Plugin/Command/Page#: "+args[0]);
							return false;
						}
						targetCmds = ImmutableSet.of(targetCmd);
					}
					else{
						pl.getLogger().warning("found cmds: "+targetCmds.size());
						Set<Command> filteredCmds = targetCmds.stream().filter(cmd -> canAccess(sender, cmd)).collect(Collectors.toSet());
						if(!filteredCmds.isEmpty()) targetCmds = filteredCmds;
						else{
							sender.sendMessage(ChatColor.RED+"Missing permission to view help for that command");
							return false;
						}
					}
				}
			}
		}
		if(targetCmds != null){
			for(Command cmd : targetCmds) showCommandHelp(sender, cmd, /*useNamespace=*/targetCmds.size() > 1);
		}
		else if(targetPlugin != null){
			showPluginHelp(sender, targetPlugin);
		}
		else{
			showCommandHelpList(sender, targetPage);
		}
		return true;
	}
}