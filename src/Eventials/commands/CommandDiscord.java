package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.bukkit.ChatColor;
import Eventials.Eventials;
import net.evmodder.EvLib.bukkit.EvCommand;
import net.evmodder.EvLib.bukkit.TellrawUtils.ClickEvent;
import net.evmodder.EvLib.bukkit.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.bukkit.TellrawUtils.TextClickAction;
import net.evmodder.EvLib.bukkit.TellrawUtils.ListComponent;

public class CommandDiscord extends EvCommand{
	private final ListComponent tellrawComp;

	public CommandDiscord(Eventials pl){
		super(pl);
		final String linkLocation = pl.getConfig().getString("discord-link");
		final String preText = ChatColor.GREEN+"Discord join link:"+ChatColor.WHITE+" ";
		final String linkDisplay = linkLocation.replaceAll("https?://", "");
		tellrawComp = new ListComponent(
				new RawTextComponent(preText),
				new RawTextComponent(linkDisplay, new TextClickAction(ClickEvent.OPEN_URL, linkLocation))
		);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return ImmutableList.of();}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player) Eventials.getPlugin().sendTellraw(sender.getName(), tellrawComp.toString());
		else sender.sendMessage(tellrawComp.toPlainText());
		return true;
	}
}