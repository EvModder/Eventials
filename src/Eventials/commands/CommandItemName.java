package Eventials.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.TextUtils;

public class CommandItemName extends EvCommand{

	public CommandItemName(EvPlugin p) {
		super(p);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();

		if(item == null) return false;

		if(args.length == 0){
			sender.sendMessage("Removed item name");
			if(item.hasItemMeta()) item.getItemMeta().setDisplayName(null);
			return true;
		}

		StringBuilder builder = new StringBuilder(args[0]);
		for(int i=1; i<args.length; ++i) builder.append(' ').append(args[i]);

		ItemMeta named = item.getItemMeta();
		named.setDisplayName(TextUtils.translateAlternateColorCodes('&', builder.toString()));
		item.setItemMeta(named);
		player.getInventory().setItemInMainHand(item);
		return true;
	}
}