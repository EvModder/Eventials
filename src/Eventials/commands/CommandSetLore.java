package Eventials.commands;

import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import Eventials.Eventials;
import net.evmodder.EvLib2.CommandBase;

public class CommandSetLore extends CommandBase {
	public CommandSetLore(Eventials pl){
		super(pl);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		Player p = (Player) sender;
		ItemStack item = p.getInventory().getItemInMainHand();
		
		if(item == null || item.getItemMeta() == null){
			sender.sendMessage(ChatColor.RED+"Invalid item in hand");
			return true;
		}
		if(args.length < 1){
			p.sendMessage("Too few arguments! "); return false;
		}
		StringBuilder input = new StringBuilder(args[0]);
		if(args.length > 1) for(int i = 1; i < args.length; ++i){
			input.append(' '); input.append(args[i]);
		}
		
		String loreString = Extras.Text.translateAlternateColorCodes('&', input.toString());
		ItemMeta meta = item.getItemMeta();
		meta.setLore(Arrays.asList(loreString.split(">")));
		item.setItemMeta(meta);
		p.getInventory().setItemInMainHand(item);
		return true;
	}
}
