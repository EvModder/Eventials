package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.google.common.collect.ImmutableList;
import net.evmodder.EvLib.bukkit.EvCommand;
import net.evmodder.EvLib.bukkit.EvPlugin;
import net.evmodder.EvLib.bukkit.TypeUtils;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;

public class CommandEditSign extends EvCommand{
	final Plugin pl;
	public CommandEditSign(EvPlugin p) {
		super(p);
		pl = p;
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return ImmutableList.of();}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		final Player player = (Player)sender;
		final Block block = player.getTargetBlockExact(/*maxDistance=*/7);
		if(!TypeUtils.isSign(block.getType()) && !TypeUtils.isWallSign(block.getType())){
			sender.sendMessage(ChatColor.RED+"Target sign not found");
			return true;
		}
		final Block attachedBlock = block.getBlockData() instanceof Directional
				? block.getRelative(((Directional)block.getBlockData()).getFacing().getOppositeFace())
				: block.getRelative(BlockFace.DOWN);
		final BlockPlaceEvent placeEvent = new BlockPlaceEvent(block, block.getState(), attachedBlock,
				new ItemStack(block.getType()), player, /*canBuild=*/true, EquipmentSlot.HAND);
		pl.getServer().getPluginManager().callEvent(placeEvent);
		if(placeEvent.isCancelled()) {
//			sender.sendMessage(ChatColor.RED+"You do not have permission to edit signs here");
			return true;
		}
		final Sign sign = (Sign)block.getState();
		//sign.setWaxed(false);//sign.setEditable(true)
		sign.update();
		pl.getServer().getScheduler().runTaskLater(pl, () -> player.openSign(sign), 1);
		return true;
	}
}
