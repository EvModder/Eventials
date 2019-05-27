package Eventials.books;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.evmodder.EvLib.CommandBase;

public class CommandFixBook extends CommandBase{
	public CommandFixBook(JavaPlugin pl){super(pl);}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

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