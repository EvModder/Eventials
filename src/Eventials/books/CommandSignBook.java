package Eventials.books;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.plugin.java.JavaPlugin;
import Extras.Text;
import net.evmodder.EvLib.CommandBase;

public class CommandSignBook extends CommandBase{
	public CommandSignBook(JavaPlugin pl){super(pl);}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/signbook <title> [author:name]
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
			return true;
		}
		Player p = (Player) sender;
		ItemStack bookItem = p.getInventory().getItemInMainHand();
		//if(bookItem == null || bookItem.getType() != Material.WRITTEN_BOOK)
		//	bookItem = p.getInventory().getItemInOffHand();

		if(bookItem == null || bookItem.getType() != Material.WRITABLE_BOOK){
			sender.sendMessage(ChatColor.RED+"You must be holding an usigned book to do this!");
			return true;
		}
		if(args.length == 0){
			sender.sendMessage(ChatColor.RED+"Please provide a title");
			return true;
		}
		String title = Text.translateAlternateColorCodes('&', StringUtils.join(args, ' '));
		String author = sender.getName();
		if(sender.hasPermission("eventials.books.sign.other")){
			int i = title.toLowerCase().indexOf("author:");
			if(i != -1 ){
				author = title.substring(i + "author:".length()).trim();
				title = title.substring(0, i).trim();
			}
		}
		ItemStack signedBook = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) bookItem.getItemMeta();
		meta.setTitle(title);
		meta.setAuthor(author);
		meta.setGeneration(Generation.ORIGINAL);
		signedBook.setItemMeta(meta);

		p.getInventory().setItemInMainHand(signedBook);
		sender.sendMessage(ChatColor.GOLD+"The book has been signed");
		return true;
	}
}