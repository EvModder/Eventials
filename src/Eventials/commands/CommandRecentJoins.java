package Eventials.commands;

import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import Eventials.Eventials;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.extras.TellrawUtils.HoverEvent;
import net.evmodder.EvLib.extras.TellrawUtils.ListComponent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.extras.TellrawUtils.TextHoverAction;

public class CommandRecentJoins extends EvCommand {
	int maxRecents;
	Eventials pl;

	public CommandRecentJoins(Eventials pl) {
		super(pl);
		this.pl = pl;
		maxRecents = pl.getConfig().getInt("max-recent-joins-stored", 50);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		int num = maxRecents;
		if(args.length == 1){
			try{ num = Integer.parseInt(args[0]); }
			catch(NumberFormatException ex){}
		}
		List<String> names = pl.loginListener.getRecentJoins(num);
		if(names.size() < num) num = names.size();
		ListComponent listComp = new ListComponent(new RawTextComponent(new StringBuilder()
				.append(ChatColor.BLUE).append("Last ").append(ChatColor.YELLOW).append(num)
				.append(ChatColor.BLUE).append(" players to join: ").toString()));
		if(!names.isEmpty()){
			Iterator<String> iterator = names.iterator();
			String name = iterator.next();
			listComp.addComponent(new RawTextComponent(ChatColor.GRAY+name,
					new TextHoverAction(HoverEvent.SHOW_TEXT, pl.loginListener.getTimeOffline(name))));
			while(iterator.hasNext()){
				name = iterator.next();
				listComp.addComponent(new RawTextComponent(ChatColor.BLUE+", "));
				listComp.addComponent(new RawTextComponent(ChatColor.GRAY+name,
						new TextHoverAction(HoverEvent.SHOW_TEXT, pl.loginListener.getTimeOffline(name))));
			}
			listComp.addComponent(new RawTextComponent(ChatColor.BLUE+"."));
		}
		if(sender instanceof Player) Eventials.getPlugin().sendTellraw(sender.getName(), listComp.toString());
		else sender.sendMessage(listComp.toPlainText());
		return true;
	}
}