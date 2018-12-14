package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import EvLib.CommandBase2;
import EvLib.EvPlugin;

public class CommandItemSuffix extends CommandBase2{

	public CommandItemSuffix(EvPlugin p) {
		super(p);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();

		if(item == null) return false;

		if(args.length < 1){
			sender.sendMessage("Too few arguments!");
			return false;
		}

		ItemMeta meta = item.getItemMeta();
		if(meta == null || meta.getDisplayName() == null){
			player.sendMessage(ChatColor.RED+"Item needs to already have a name!");
			return false;
		}

		StringBuilder builder = new StringBuilder(meta.getDisplayName()).append(args[0]);
		for(int i=1; i<args.length; ++i) builder.append(' ').append(args[i]);

		meta.setDisplayName(Extras.Text.translateAlternateColorCodes('&', builder.toString()));
		item.setItemMeta(meta);

		player.getInventory().setItemInMainHand(item);
		return true;
	}
}