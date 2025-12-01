package Eventials.economy.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import Eventials.economy.ServerEconomy;
import net.evmodder.EvLib.bukkit.EvCommand;

public class CommandDonateTop extends EvCommand{
	final ServerEconomy economy;

	public CommandDonateTop(JavaPlugin pl, ServerEconomy eco){
		super(pl);
		economy = eco;
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String Label, String[] args){
		if(args.length == 1 && sender instanceof Player){
			final List<String> tabCompletes = new ArrayList<>();
			args[0] = args[0].toLowerCase();
			int pages = economy.numDonatetopPages();
			for(int i=Math.min(pages, 100); i>=0; --i) if((""+i).startsWith(args[0])) tabCompletes.add(""+i);
			if((""+pages).startsWith(args[0])) tabCompletes.add(""+pages);
			return tabCompletes;
		}
		return null;
	}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/donateserver [amt]

		int page = -1;
		if(args.length != 0){
			try{page = Integer.parseInt(args[0]);} catch(NumberFormatException ex){}
		}
		economy.showDonatetop(sender, page);
		return true;
	}
}