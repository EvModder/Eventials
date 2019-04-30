package Eventials.commands;

import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import Eventials.Eventials;
import net.evmodder.EvLib2.CommandBase;

public class CommandRecentJoins extends CommandBase {
	int maxRecents;

	public CommandRecentJoins(Eventials pl) {
		super(pl);
		maxRecents = pl.getConfig().getInt("max-recent-joins", 20);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		int num = maxRecents;
		if(args.length == 1){
			try{ num = Integer.parseInt(args[0]); }
			catch(NumberFormatException ex) {}
		}
		List<String> names = Eventials.getPlugin().loginListener.getRecentJoins(num);
		if(names.size() < num) num = names.size();
		StringBuilder builder = new StringBuilder("")
				.append(ChatColor.BLUE).append("Last ").append(ChatColor.YELLOW).append(num)
				.append(ChatColor.BLUE).append(" players to join: ").append(ChatColor.GRAY);
		if(!names.isEmpty()){
			Iterator<String> iterator = names.iterator();
			builder.append(iterator.next());
			while(iterator.hasNext()) builder.append(ChatColor.BLUE).append(", ")
										.append(ChatColor.GRAY).append(iterator.next());
			builder.append(ChatColor.BLUE).append('.');
		}
		sender.sendMessage(builder.toString());
		return true;
	}
}