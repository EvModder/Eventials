package Eventials.books;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.plugin.java.JavaPlugin;
import net.evmodder.EvLib.CommandBase;

public class CommandUnsignBook extends CommandBase{
	public CommandUnsignBook(JavaPlugin pl){super(pl);}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/unsign
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
			return true;
		}
		Player p = (Player) sender;
		ItemStack bookItem = p.getInventory().getItemInMainHand();
		//if(bookItem == null || bookItem.getType() != Material.WRITTEN_BOOK)
		//	bookItem = p.getInventory().getItemInOffHand();

		if(bookItem == null || bookItem.getType() != Material.WRITTEN_BOOK){
			sender.sendMessage(ChatColor.RED+"You must be holding a signed book to do this!");
			return true;
		}
		BookMeta meta = (BookMeta)bookItem.getItemMeta();
		if(meta.getGeneration() != Generation.ORIGINAL && !p.hasPermission("eventials.books.unsign.copy")){
			sender.sendMessage(ChatColor.RED+"Error: Non-original copies cannot be unsigned!");
			return true;
		}
		if(!meta.getAuthor().equals(sender.getName()) && !p.hasPermission("eventials.books.unsign.other")){
			sender.sendMessage(ChatColor.RED+"Error: Author mismatch\n"
					+ChatColor.GRAY+"You may not unsign "+meta.getAuthor()+"'s book for them!");
			return true;
		}
		ItemStack openBook = new ItemStack(Material.WRITABLE_BOOK);
		openBook.setItemMeta(meta);
		if(bookItem.getAmount() == 1) p.getInventory().setItemInMainHand(openBook);
		else{
			bookItem.setAmount(bookItem.getAmount()-1);
			p.getInventory().setItemInMainHand(bookItem);
			if(p.getInventory().addItem(openBook).isEmpty() == false){
				p.getWorld().dropItemNaturally(p.getLocation(), openBook);
			}
		}
		sender.sendMessage(ChatColor.GOLD+"Book successfully unsigned");
		return true;
	}
}