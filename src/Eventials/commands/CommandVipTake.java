package Eventials.commands;

import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import Eventials.Eventials;
import net.evmodder.EvLib2.CommandBase;
import org.bukkit.ChatColor;

public class CommandVipTake extends CommandBase {
	Eventials plugin;
	final String prefix = ChatColor.DARK_AQUA+"["+ChatColor.GRAY+"AC"+ChatColor.DARK_AQUA+"]"+ChatColor.WHITE+" ";

	public CommandVipTake(Eventials pl) {
		super(pl);
		plugin = pl;
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@SuppressWarnings("deprecation") @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(args.length == 0) return false;

		OfflinePlayer p;
		try{p = plugin.getServer().getOfflinePlayer(UUID.fromString(args[0]));}
		catch(IllegalArgumentException ex){p = plugin.getServer().getOfflinePlayer(args[0]);}

		if(p == null || !p.hasPlayedBefore()){
			sender.sendMessage("Invalid player name/uuid");
			return true;
		}

		Eventials plugin = Eventials.getPlugin();
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
				"permissions player removegroup "+p.getUniqueId()+" vip");

		if(p.isOnline()) p.getPlayer().sendMessage(prefix+"Your VIP package has expired.\n"
												+prefix+"Thanks again for your contribution!");

		else plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mail send "+p.getName()+" "
					+prefix+"Your VIP package has expired.\nServer: "+prefix+" Thanks again for your contribution!");
		return true;
	}
}