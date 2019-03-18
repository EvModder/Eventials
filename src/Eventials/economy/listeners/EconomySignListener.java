package Eventials.economy.listeners;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import EvLib.VaultHook;
import Eventials.Eventials;
import Eventials.economy.Economy;
import Extras.Extras;

public class EconomySignListener implements Listener {
	String symbol;
	Economy economy;
	public EconomySignListener(){
		Eventials plugin = Eventials.getPlugin();
		economy = Economy.getEconomy();
		symbol = plugin.getConfig().getString("currency-symbol", "$");
	}
	
	@EventHandler
	public void onPlayerInteractBlock(PlayerInteractEvent evt){
		if(evt.getAction() == Action.RIGHT_CLICK_BLOCK && (
			evt.getClickedBlock().getType() == Material.WALL_SIGN || evt.getClickedBlock().getType() == Material.SIGN))
		{
			String[] lines = ((Sign)evt.getClickedBlock().getState()).getLines();
			if(ChatColor.stripColor(lines[0]).toLowerCase().contains("adminshop")){
				if(Extras.isAdminShop(evt.getClickedBlock())){
					if(evt.getPlayer().isSneaking() && evt.getPlayer().hasPermission("eventials.setadminshop")){
						Extras.setAdminShop(evt.getClickedBlock(), false);
						evt.getPlayer().sendMessage(ChatColor.GRAY+"Deactivated AdminShop sign");
					}
					else{
						//buy
						int amount = Integer.parseInt(lines[1]);
						Material item = Material.getMaterial(lines[2].toUpperCase());
						double price = Double.parseDouble(lines[3]);
						
						if(VaultHook.hasAtLeast(evt.getPlayer(), price)){
							if(economy.playerToServer(evt.getPlayer().getUniqueId(), price)){
								for(ItemStack extra : evt.getPlayer().getInventory().addItem(new ItemStack(item, amount)).values())
									evt.getPlayer().getWorld().dropItem(evt.getClickedBlock().getLocation(), extra);
								evt.getPlayer().sendMessage(ChatColor.GREEN+"Purchased "+ChatColor.GOLD+amount+ChatColor.GREEN
										+" of "+ChatColor.GOLD+lines[2]+" for "+ChatColor.YELLOW+price+symbol);
							}
							else evt.getPlayer().sendMessage(ChatColor.RED+"Unable to purchase item");
						}
						else evt.getPlayer().sendMessage(ChatColor.RED+"You cannot afford this item");
						
					}//if not a disable-click
				}//if clicked an enabled adminshop sign
				else if(evt.getPlayer().isSneaking() && evt.getPlayer().hasPermission("eventials.setadminshop")){
					if(StringUtils.isNumeric(lines[1]) && Material.getMaterial(lines[2].toUpperCase()) != null
						&& StringUtils.isNumeric(lines[3].replace(symbol, "")))
					Extras.setAdminShop(evt.getClickedBlock(), true);
					evt.getPlayer().sendMessage(ChatColor.GRAY+"Activated AdminShop sign");
				}
			}//if first line contains "adminshop"
		}//if clicked a sign
	}//func
}
