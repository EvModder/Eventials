package Eventials.commands;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import Eventials.Eventials;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.extras.TypeUtils;

public class CommandSigntool extends EvCommand implements Listener{
	private final Eventials plugin;
	public CommandSigntool(Eventials pl, boolean enabled){
		super(pl, enabled);
		plugin = pl;
		if(enabled) pl.getServer().getPluginManager().registerEvents(this, pl);
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

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

		String signText;
		
		if(args.length < 1) signText = "";
		else signText = TextUtils.translateAlternateColorCodes('&', StringUtils.join(args, ' '));

		ItemMeta meta = item.getItemMeta();

		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.BLUE+"Sign Setter");
		if(!signText.isEmpty()){
			signText = signText.replaceAll("(?<=(?:^|[^\\\\]))(\\\\{2})*\\\\n", "$1\n");
			signText = signText.replaceAll("(?<=(?:^|[^\\\\]))(\\\\{2})*>", "$1\n");
			signText = signText.replaceAll("(?<=(?:^|[^\\\\]))(\\\\{2})*\\\\s", "$1 ");
			signText = signText.replaceAll("(?<=(?:^|[^\\\\]))(\\\\{2})*\\\\>", "$1>");
			signText = signText.replaceAll("\\\\\\\\", "\\\\");
			for(String loreLine : signText.split("\n")) lore.add(ChatColor.GRAY+"Line: "+loreLine);
		}

		meta.setLore(lore);
		item.setItemMeta(meta);
		p.getInventory().setItemInMainHand(item);

		return true;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteractBlock(PlayerInteractEvent evt){
		if(evt.getClickedBlock() != null && evt.useInteractedBlock() != Result.DENY
				&& evt.getAction() == Action.RIGHT_CLICK_BLOCK
				&& (TypeUtils.isSign(evt.getClickedBlock().getType())
				|| TypeUtils.isWallSign(evt.getClickedBlock().getType()))
				&& evt.getItem() != null && evt.getItem().hasItemMeta() && evt.getItem().getItemMeta().hasLore()
				&& evt.getItem().getItemMeta().getLore().contains(ChatColor.BLUE+"Sign Setter")
				&& evt.getPlayer().isSneaking())
		{
			BlockPlaceEvent event = new BlockPlaceEvent(evt.getClickedBlock(), evt.getClickedBlock().getState(),
					evt.getClickedBlock(), evt.getItem(), evt.getPlayer(), true, evt.getHand());
			plugin.getServer().getPluginManager().callEvent(event);

			if(event.isCancelled()){
				plugin.getLogger().info(evt.getPlayer().getName()+" tried to use a SignTool, but failed BlockPlaceEvent");
			}
			else{
				List<String> lore = evt.getItem().getItemMeta().getLore();
				if(lore.size() == 1){
					Sign sign = (Sign) evt.getClickedBlock().getState();
					for(String line : sign.getLines()) lore.add(ChatColor.RESET+"Line: "+line);
					ItemMeta meta = evt.getItem().getItemMeta();
					meta.setLore(lore);
					evt.getItem().setItemMeta(meta);
				}
				else{
					String[] lines = new String[]{"","","",""};

					for(int li=0, i=1; i<lore.size() && li<4; ++i){
						int prefixI = lore.get(i).indexOf("Line: ");
						if(prefixI != -1) lines[li++] = lore.get(i).substring(prefixI+6);
					}

					SignChangeEvent updateEvent = new SignChangeEvent(evt.getClickedBlock(), evt.getPlayer(), lines);
					plugin.getServer().getPluginManager().callEvent(updateEvent);
					if(updateEvent.isCancelled()){
						plugin.getLogger().info(evt.getPlayer().getName()+" tried to use a SignTool, but failed SignChangeEvent");
					}
					else{
						plugin.getLogger().info(evt.getPlayer().getName()+" used a SignTool: "+String.join(">", lines));
						Sign sign = (Sign) evt.getClickedBlock().getState();
						for(int i=0; i<4; ++i) if(!lines[i].isEmpty()) sign.setLine(i, lines[i]);
						sign.update();
						evt.setUseItemInHand(Result.DENY);
						evt.setUseInteractedBlock(Result.DENY);
					}
				}//there is data to write to sign
			}//allowed to edit blocks
		}//if holding a sign setter
	}//PlayerInteractEvent
}