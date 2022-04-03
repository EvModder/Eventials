package Eventials.commands;

import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.NBTTagUtils;
import net.evmodder.EvLib.extras.TellrawUtils;
import net.evmodder.EvLib.extras.TellrawUtils.Component;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.extras.NBTTagUtils.RefNBTTagCompound;

public class CommandSetItemName extends EvCommand{
	public CommandSetItemName(EvPlugin p){super(p);}

	public final static ItemStack setDisplayName(@Nonnull ItemStack item, @Nonnull Component name){
		RefNBTTagCompound tag = NBTTagUtils.getTag(item);
		RefNBTTagCompound display = tag.hasKey("display") ? (RefNBTTagCompound)tag.get("display") : new RefNBTTagCompound();
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

		String nameStr = TextUtils.translateAlternateColorCodes('&', String.join(" ", args));
		Component comp = TellrawUtils.parseComponentFromString(nameStr);
		if(comp == null) comp = TellrawUtils.convertHexColorsToComponentsWithReset(nameStr);

		if(comp.toPlainText().isEmpty()){
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(null);
			item.setItemMeta(meta);
			player.getInventory().setItemInMainHand(item);
			player.sendMessage("Removed item name");
			return true;
		}
		item = setDisplayName(item, comp);
		player.getInventory().setItemInMainHand(item);
		return true;
	}
}