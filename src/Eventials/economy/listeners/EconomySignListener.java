package Eventials.economy.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import Eventials.Eventials;
import Eventials.Extras;
import Eventials.economy.EvEconomy;
import net.evmodder.EvLib.extras.TypeUtils;
import net.evmodder.EvLib.hooks.EssEcoHook;

public class EconomySignListener implements Listener {
	String symbol;
	EvEconomy economy;
	public EconomySignListener(){
		Eventials plugin = Eventials.getPlugin();
		economy = EvEconomy.getEconomy();
		symbol = plugin.getConfig().getString("currency-symbol", "$");
	}
	
	@EventHandler
	public void onPlayerInteractBlock(PlayerInteractEvent evt){
		if(evt.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Material type = evt.getClickedBlock().getType();
		if(!TypeUtils.isSign(type) && !TypeUtils.isWallSign(type)) return;

		@SuppressWarnings("deprecation")
		String[] lines = ((Sign)evt.getClickedBlock().getState()).getLines();
		if(ChatColor.stripColor(lines[0]).toLowerCase().contains("adminshop")) {
			if(Extras.isAdminShop(evt.getClickedBlock())) {
				if(evt.getPlayer().isSneaking() && evt.getPlayer().hasPermission("eventials.setadminshop")) {
					Extras.setAdminShop(evt.getClickedBlock(), false);
					evt.getPlayer().sendMessage(ChatColor.GRAY + "Deactivated AdminShop sign");
				}
				else{
					//buy
					int amount = Integer.parseInt(lines[1]);
					Material item = Material.getMaterial(lines[2].toUpperCase());
					double price = Double.parseDouble(lines[3]);

					if(EssEcoHook.hasAtLeast(evt.getPlayer(), price)) {
						if(economy.playerToServer(evt.getPlayer().getUniqueId(), price)) {
							for(ItemStack extra : evt.getPlayer().getInventory().addItem(new ItemStack(item, amount))
									.values())
								evt.getPlayer().getWorld().dropItem(evt.getClickedBlock().getLocation(), extra);
							evt.getPlayer()
									.sendMessage(ChatColor.GREEN + "Purchased " + ChatColor.GOLD + amount
											+ ChatColor.GREEN + " of " + ChatColor.GOLD + lines[2] + " for "
											+ ChatColor.YELLOW + price + symbol);
						}
						else evt.getPlayer().sendMessage(ChatColor.RED + "Unable to purchase item");
					}
					else evt.getPlayer().sendMessage(ChatColor.RED + "You cannot afford this item");

				}//if not a disable-click
			}//if clicked an enabled adminshop sign
			else if(evt.getPlayer().isSneaking() && evt.getPlayer().hasPermission("eventials.setadminshop")) {
				if(lines[1].matches("^[0-9.]+$") && Material.getMaterial(lines[2].toUpperCase()) != null && lines[3].replace(symbol, "").matches("^[0-9.]+$")){
					Extras.setAdminShop(evt.getClickedBlock(), true);
				}
				evt.getPlayer().sendMessage(ChatColor.GRAY + "Activated AdminShop sign");
			}
		}//if first line contains "adminshop"
	}//func
}
