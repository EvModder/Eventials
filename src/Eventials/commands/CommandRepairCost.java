package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.bukkit.ChatColor;
import Eventials.Eventials;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.extras.NBTTagUtils;
import net.evmodder.EvLib.extras.NBTTagUtils.RefNBTTagCompound;

public class CommandRepairCost extends EvCommand{

	public CommandRepairCost(Eventials p){
		super(p);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){
		return args.length <= 1 ? null : ImmutableList.of();
	}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
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

		final RefNBTTagCompound tag = NBTTagUtils.getTag(item);
		final int rc = tag != null && tag.hasKey("RepairCost") ? tag.getInt("RepairCost") : 0;
		sender.sendMessage("RepairCost: "+ChatColor.AQUA+rc);
		return true;
	}
}