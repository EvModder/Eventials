package Eventials.commands;

import java.lang.reflect.Field;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.google.common.collect.ImmutableList;
import Eventials.CompConverter;
import net.evmodder.EvLib.bukkit.EvCommand;
import net.evmodder.EvLib.bukkit.EvPlugin;
import net.evmodder.EvLib.util.ReflectionUtils;
import net.evmodder.EvLib.bukkit.TellrawUtils;
import net.evmodder.EvLib.bukkit.TellrawUtils.Component;
import net.evmodder.EvLib.TextUtils;

public class CommandSetItemName extends EvCommand{
	public CommandSetItemName(EvPlugin p){super(p);}

	private static final Class<?> classCraftMetaItem = ReflectionUtils.getClass("{cb}.inventory.CraftMetaItem");
	private static final Field displayNameField = ReflectionUtils.getField(classCraftMetaItem, "displayName");

	public static final ItemStack setDisplayName(ItemStack item, Component name){
		final ItemMeta meta = item.getItemMeta();
		//TODO: nicer way to share this with CommandSetItemLore
		ReflectionUtils.set(displayNameField, meta, CompConverter.chatCompFromJsonStr(name.toString()));
		item.setItemMeta(meta);
		return item;
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return ImmutableList.of();}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if(item == null || item.getItemMeta() == null){
			sender.sendMessage(ChatColor.RED+"Invalid item in hand");
			return true;
		}

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