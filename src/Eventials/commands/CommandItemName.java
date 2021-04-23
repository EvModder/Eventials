package Eventials.commands;

import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.NBTTagUtils;
import net.evmodder.EvLib.extras.TellrawUtils;
import net.evmodder.EvLib.extras.TellrawUtils.Component;
import net.evmodder.EvLib.extras.TellrawUtils.ListComponent;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.extras.NBTTagUtils.RefNBTTag;

public class CommandItemName extends EvCommand{
	public CommandItemName(EvPlugin p){super(p);}

	public final static ItemStack setDisplayName(@Nonnull ItemStack item, @Nonnull Component name){
		RefNBTTag tag = NBTTagUtils.getTag(item);
		RefNBTTag display = tag.hasKey("display") ? (RefNBTTag)tag.get("display") : new RefNBTTag();
		display.setString("Name", name.toString());
		tag.set("display", display);
		return NBTTagUtils.setTag(item, tag);
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

		String nameStr = TextUtils.translateAlternateColorCodes('&', String.join(" ", args));
		ListComponent nameComp = TellrawUtils.convertHexColorsToComponents(nameStr);
		item = setDisplayName(item, nameComp);
		player.getInventory().setItemInMainHand(item);
		return true;
	}
}