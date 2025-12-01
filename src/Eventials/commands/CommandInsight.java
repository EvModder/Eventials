package Eventials.commands;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.google.common.collect.ImmutableList;
import Eventials.Eventials;
import net.evmodder.EvLib.bukkit.EvCommand;
import net.evmodder.EvLib.bukkit.EvPlugin;

public class CommandInsight extends EvCommand{

	public CommandInsight(EvPlugin p){
		super(p);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){
		return args.length <= 1 ? null : ImmutableList.of();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(args.length != 1){
			sender.sendMessage(ChatColor.RED+"Invalid number of arguments");
			return true;
		}
		@SuppressWarnings("deprecation")
		OfflinePlayer target2, target1 = Eventials.getPlugin().getServer().getOfflinePlayer(args[0]);
		try{target2 = Eventials.getPlugin().getServer().getOfflinePlayer(UUID.fromString(args[0]));}
		catch(IllegalArgumentException ex){target2 = null;}
		OfflinePlayer target = ((target1 != null && target1.hasPlayedBefore())
				|| target2 == null || !target2.hasPlayedBefore()) ? target1 : target2;

		if(target == null) sender.sendMessage(ChatColor.RED+"Unable to find the specified player");
		else{
			sender.sendMessage(ChatColor.GOLD + "Name: " + ChatColor.RED + target.getName()
					+ ChatColor.GOLD+"\nUUID: " + ChatColor.RED + target.getUniqueId());

			if(target.hasPlayedBefore()){
				long timeSince = new GregorianCalendar().getTime().getTime() - target.getLastPlayed();
				float daysSince = (float) timeSince/(24*60*60*1000);
				sender.sendMessage(ChatColor.GOLD + "Last Seen: " + ChatColor.RED + daysSince + ChatColor.GOLD + " days ago.");
			}
			else sender.sendMessage(ChatColor.GOLD + "Last Seen: " + ChatColor.RED + "N/A" + ChatColor.GOLD + ".");
		}
		return true;
	}//-3,3 -4,3 -5,3 -5,4
}