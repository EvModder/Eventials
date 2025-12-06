package Eventials.commands;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.google.common.collect.ImmutableList;
import Eventials.CompConverter;
import Eventials.Eventials;
import net.evmodder.EvLib.bukkit.EvCommand;
import net.evmodder.EvLib.util.ReflectionUtils;
import net.evmodder.EvLib.bukkit.TellrawUtils;
import net.evmodder.EvLib.TextUtils;
import net.evmodder.EvLib.bukkit.TellrawUtils.Component;

public class CommandSetItemLore extends EvCommand {
	public CommandSetItemLore(Eventials pl){super(pl);}

	private static final Class<?> classCraftMetaItem = ReflectionUtils.getClass("{cb}.inventory.CraftMetaItem");
	private static final Field loreField = ReflectionUtils.getField(classCraftMetaItem, "lore");

	public static final ItemStack setLore(ItemStack item, Component... lore){
		Object lines = Stream.of(lore).map(line -> CompConverter.chatCompFromJsonStr(line.toString())).toList();
		ItemMeta meta = item.getItemMeta();
		ReflectionUtils.set(loreField, meta, lines);
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
		Player p = (Player) sender;
		ItemStack item = p.getInventory().getItemInMainHand();
		if(item == null || item.getItemMeta() == null){
			sender.sendMessage(ChatColor.RED+"Invalid item in hand");
			return true;
		}
		if(args.length == 0) args = new String[]{""}; // Remove item lore
		final StringBuilder builder = new StringBuilder(args[0]);
		if(args.length > 1) for(int i=1; i<args.length; ++i) builder.append(' ').append(args[i]);
		final String[] loreStrs = TextUtils.translateAlternateColorCodes('&', builder.toString()).split(">");
		final Component[] comps = new Component[loreStrs.length];
		boolean allEmpty = true;
		for(int i=0; i<comps.length; ++i){
			comps[i] = TellrawUtils.parseComponentFromString(loreStrs[i]);
			if(comps[i] == null) comps[i] = TellrawUtils.convertHexColorsToComponentsWithReset(loreStrs[i]);
			if(!comps[i].toPlainText().isEmpty()) allEmpty = false;
		}
		if(allEmpty){
			ItemMeta meta = item.getItemMeta();
			meta.setLore(null);
			item.setItemMeta(meta);
			p.getInventory().setItemInMainHand(item);
			p.sendMessage("Removed item lore");
			return true;
		}
		item = setLore(item, comps);
		p.getInventory().setItemInMainHand(item);
		return true;
	}
}
