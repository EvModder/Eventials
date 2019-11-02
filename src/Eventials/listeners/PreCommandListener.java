package Eventials.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import net.evmodder.EvLib.extras.CommandUtils;
import net.evmodder.EvLib.extras.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import com.earth2me.essentials.Essentials;
import Eventials.Eventials;
import Eventials.Extras;
import Eventials.economy.EvEconomy;

public class PreCommandListener implements Listener {
	final Eventials plugin;
	final boolean fancyHelp, cmdHelp, commandAliases,
					customBaltop, watchBalances, hyperWarps;
	final HashSet<String> balanceWatchCommands, quickWarps;
	final HashMap<String, Integer> cooldownCommands;
	final HashMap<String, String> getCommandFromAlias;
	final HashMap<String, Long> recentCooldownCommands;
	final String curSymbol;

	public PreCommandListener(){
		plugin = Eventials.getPlugin();

		fancyHelp = plugin.getConfig().getBoolean("fancy-help", true);
		cmdHelp = plugin.getConfig().getBoolean("detailed-command-help", true);
		commandAliases = plugin.getConfig().getBoolean("command-aliases", true);
		quickWarps = new HashSet<String>();
		quickWarps.addAll(plugin.getConfig().getStringList("quick-warps"));
		customBaltop = plugin.getConfig().getBoolean("custom-baltop", true);
		watchBalances = plugin.getConfig().getBoolean("update-balance-watch", false);

		balanceWatchCommands = new HashSet<String>();
		if(watchBalances) balanceWatchCommands.addAll(plugin.getConfig().getStringList("update-balance-commands"));

		curSymbol = TextUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("currency-symbol", "&2L"));
		getCommandFromAlias = new HashMap<String, String>();
		cooldownCommands = new HashMap<String, Integer>();
		recentCooldownCommands = new HashMap<String, Long>();
		ConfigurationSection commandCooldowns = plugin.getConfig().getConfigurationSection("global-command-cooldowns");
		if(commandCooldowns != null) for(Entry<String, Object> e : commandCooldowns.getValues(false).entrySet()){
			PluginCommand cmd = plugin.getServer().getPluginCommand(e.getKey());
			if(cmd != null){
				String lowerCmd = cmd.getName().toLowerCase();
				cooldownCommands.put(lowerCmd, (Integer)e.getValue());
				getCommandFromAlias.put(lowerCmd, lowerCmd);
				for(String alias : cmd.getAliases()){
					cooldownCommands.put(alias.toLowerCase(), (Integer)e.getValue());
					getCommandFromAlias.put(alias.toLowerCase(), lowerCmd);
				}
			}
		}
		hyperWarps = plugin.getConfig().getBoolean("use-hyperwarps", true);
	}

	@SuppressWarnings("deprecation") @EventHandler(priority = EventPriority.LOWEST)
	public void onPreCommand(PlayerCommandPreprocessEvent evt){
		if(evt.isAsynchronous() || evt.isCancelled()) return;

		String message = evt.getMessage().toLowerCase();
		String command = message.split(" ")[0];//.replace("-", "");
		String noSlash = command.substring(1);

		if(cooldownCommands.containsKey(noSlash) &&
				!evt.getPlayer().hasPermission("eventials.bypass.waitcommands")){
			String cmd = getCommandFromAlias.get(noSlash);
			long timeInSeconds = System.currentTimeMillis()/1000;
			if(recentCooldownCommands.containsKey(cmd) &&
					!evt.getPlayer().hasPermission("eventials.bypass.waitcommands."+cmd)){
				long timeSince = timeInSeconds - recentCooldownCommands.get(cmd);
				if(timeSince < cooldownCommands.get(cmd)){
					evt.getPlayer().sendMessage(ChatColor.RED
							+"Sorry, the cooldown for that command has not yet ended.");
					evt.getPlayer().sendMessage(ChatColor.GRAY+"Please wait another "+ChatColor.GOLD
							+(cooldownCommands.get(cmd)-timeSince)+ChatColor.GRAY+" seconds");
					evt.setCancelled(true);
					return;
				}
			}
			else recentCooldownCommands.put(cmd, timeInSeconds);
		}

		if(watchBalances && balanceWatchCommands.contains(noSlash)){
			EvEconomy.getEconomy().updateBalance(evt.getPlayer().getUniqueId(), true);
			for(String arg : message.split(" ")){
				OfflinePlayer p = plugin.getServer().getOfflinePlayer(arg);
				if(p != null && p.hasPlayedBefore()) EvEconomy.getEconomy().updateBalance(p.getUniqueId(), true);
			}
		}

		if(commandAliases){
			if(command.equals("/f") || command.equals("/faction")){
				//Lowercase the first two words (space-deliminated) in the command string
				int space = 0;
				char[] msgArr = evt.getMessage().toCharArray();
				for(int i=0; i < msgArr.length && space < 2; ++i){
					if(msgArr[i] == ' ') ++space;
					else msgArr[i] = Character.toLowerCase(msgArr[i]);
				}
				evt.setMessage(new String(msgArr));

				if(message.startsWith(command+" access ")){
					message = message.replace(":", " ").replace(" f ", " faction ").replace(" p ", " player ");
					String[] args = message.split(" ");
					if(args.length == 3 && !args[2].equals("faction") && !args[2].equals("player")){
						OfflinePlayer p = plugin.getServer().getOfflinePlayer(args[2]);
						if(p != null && p.hasPlayedBefore()) evt.setMessage("/f access player "+p.getName());
						else evt.setMessage("/f access faction "+args[2]);
					}
				}
				else evt.setMessage((evt.getMessage()+' ')
									.replace(" f ", " show ").replace(" p ", " pow ")
									.replace(" name ", " tag ").replace(" h ", " home ")
									.replace(" description ", " desc ")
									.replace(" c ", " claim ").replace(" l ", " list ").trim());
			}
			else if(message.equals("/home") || message.equals("/h")){
				Essentials ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
				List<String> homes = ess == null ? null : ess.getUser(evt.getPlayer()).getHomes();
				if(ess != null && !homes.isEmpty()){
					if(homes.contains("home")) evt.setMessage("/home home");
					else if(homes.size() == 1 || evt.getPlayer().getBedSpawnLocation() == null)
						evt.setMessage("/home "+homes.get(0));
					else evt.setMessage("/home bed");
				}
			}

			else if(message.equals("/b") && !evt.getPlayer().hasPermission("eventials.clearentities"))
				evt.setMessage("/back");
			
			else if(command.equals("/mail")){
				if(message.startsWith("/mail send admin ")) message = evt.getMessage().substring(17);
				else if(message.startsWith("/mail admin ")) message = evt.getMessage().substring(12);
				else return;
				evt.setMessage("/mail send "+plugin.getServer().getOperators().iterator().next().getName()+' '+message);
			}
		}
		if(message.equals("/warps") || message.equals("/warp")){
			if(hyperWarps){
				Extras.displayHyperWarps(evt.getPlayer());
				evt.setCancelled(true);
			}
		}
		else if(quickWarps.contains(noSlash)){
			evt.setMessage("/warp "+noSlash);
		}
		else if(command.equals("/butcher")){
			evt.setMessage(message.replace(command, "/clearentities"));
			evt.getPlayer().sendMessage("Use /butcher2 for worldedit version");
		}
		else if(command.equals("/butcher2")){
			evt.setMessage(message.replace(command, "/butcher"));
		}
		else if(command.equals("/help") || command.equals("/?")){
			int space = message.indexOf(' ');
			if(space != -1 && cmdHelp){
				Command cmd = plugin.getServer().getPluginCommand(message.substring(space+1));
				if(cmd != null){
					evt.setCancelled(true);
					CommandUtils.showCommandHelp(evt.getPlayer(), cmd);
				}
			}
			if(fancyHelp){
				int pageNum = 1;
				if(space != -1){
					try{pageNum = Math.max(Integer.parseInt(message.substring(space+1)), 1);}
					catch(IllegalArgumentException ex){return;}
				}
				evt.setCancelled(true);
				CommandUtils.showFancyHelp(evt.getPlayer(), pageNum);
			}
		}
		else if(customBaltop && ((message=message.replaceFirst(" top", "top")).startsWith("/baltop") ||
								message.startsWith("/balancetop") || message.startsWith("/moneytop"))){
			int page = -1;
			if(message.contains(" ")){
				String arg = message.split(" ")[1];
				if(StringUtils.isNumeric(arg)) page = Integer.parseInt(arg);
			}
			EvEconomy.getEconomy().showBaltop(evt.getPlayer(), page);
			evt.setCancelled(true);
		}
	}
}