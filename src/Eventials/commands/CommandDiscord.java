package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import org.bukkit.ChatColor;
import Eventials.Eventials;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.extras.TellrawUtils.ClickEvent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.extras.TellrawUtils.TextClickAction;
import net.evmodder.EvLib.extras.TellrawUtils.ListComponent;

public class CommandDiscord extends EvCommand{
	final String linkLocation;

	public CommandDiscord(Eventials pl){
		super(pl);
		linkLocation = pl.getConfig().getString("discord-link");
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		String preText = ChatColor.GREEN+"Discord join link:"+ChatColor.WHITE+" ";
		String linkDisplay = linkLocation.replaceAll("https?://", "");
		if(sender instanceof Player){
			ListComponent comp = new ListComponent();
			comp.addComponent(preText);
			comp.addComponent(new RawTextComponent(linkDisplay, new TextClickAction(ClickEvent.OPEN_URL, linkLocation)));
			Eventials.getPlugin().sendTellraw((Player)sender, comp.toString());
		}
		else sender.sendMessage(preText+linkLocation);
		return true;
	}
}