package Eventials.books;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import EvLib.CommandBase2;

public class CommandFixBook extends CommandBase2{
	public CommandFixBook(JavaPlugin pl){super(pl);}

	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/fixbook
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
			return true;
		}
		Player p = (Player) sender;
		ItemStack bookItem = p.getInventory().getItemInMainHand();

		if(bookItem == null || (bookItem.getType() != Material.WRITABLE_BOOK
				&& bookItem.getType() != Material.WRITTEN_BOOK)){
			sender.sendMessage(ChatColor.RED+"You must be holding an writable book to do this!");
			return true;
		}
		BookMeta meta = (BookMeta) bookItem.getItemMeta();
		//TODO: fix the book
		bookItem.setItemMeta(meta);
		p.getInventory().setItemInMainHand(bookItem);

		sender.sendMessage(ChatColor.GOLD+"An attempt has been made to correct book errors");
		return true;
	}
}