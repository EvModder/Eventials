package EventAndMisc;

import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import Eventials.Eventials;
import net.evmodder.Renewable.Renewable;
import net.evmodder.Renewable.RenewableAPI;

public class PlayerDeathListener implements Listener {
	//Keep unrenewables - for winners of the Renewable Event
	final RenewableAPI api;
	public PlayerDeathListener(){
		api = ((Renewable)Eventials.getPlugin().getServer().getPluginManager().getPlugin("Renewable")).getAPI();
	}
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent evt){
		//
		if(isSpecial(evt.getEntity().getUniqueId()))
		//
		for(ItemStack item : evt.getDrops()){
			if(api.isUnrenewable(item)){
				evt.setKeepInventory(true);
				ArrayList<ItemStack> drops = new ArrayList<ItemStack>();

				// Regular inventory
				for(ItemStack i : evt.getEntity().getInventory().getContents()){
					if(i != null && i.getType() != Material.AIR && !api.isUnrenewable(i)){
						drops.add(i);
						evt.getEntity().getInventory().remove(i);
					}
				}
				// Armor contents
				ItemStack helm = evt.getEntity().getInventory().getHelmet();
				if(helm != null && helm.getType() != Material.AIR && !api.isUnrenewable(helm)){
//					drops.add(helm);
					evt.getEntity().getInventory().setHelmet(null);
				}
				ItemStack chst = evt.getEntity().getInventory().getChestplate();
				if(chst != null && chst.getType() != Material.AIR && !api.isUnrenewable(chst)){
//					drops.add(chst);
					evt.getEntity().getInventory().setChestplate(null);
				}
				ItemStack legg = evt.getEntity().getInventory().getLeggings();
				if(legg != null && legg.getType() != Material.AIR && !api.isUnrenewable(legg)){
//					drops.add(legg);
					evt.getEntity().getInventory().setLeggings(null);
				}
				ItemStack boot = evt.getEntity().getInventory().getBoots();
				if(boot != null && boot.getType() != Material.AIR && !api.isUnrenewable(boot)){
//					drops.add(helm);
					evt.getEntity().getInventory().setBoots(null);
				}
				ItemStack offh = evt.getEntity().getInventory().getItemInOffHand();
				if(offh != null && offh.getType() != Material.AIR && !api.isUnrenewable(offh)){
//					drops.add(offh);
					evt.getEntity().getInventory().setItemInOffHand(null);
				}
				for(ItemStack drop : drops){
					evt.getEntity().getWorld().dropItemNaturally(evt.getEntity().getLocation(), drop);
				}
			}
		}
	}

	public boolean isSpecial(UUID uuid){
		String str = uuid.toString();
		//EvDoc, Kapurai
		//Kamekichi9, Kai_Be
		//Setteal, Enteal
		//Foofy, De_taco
		return	str.equals("34471e8d-d0c5-47b9-b8e1-b5b9472affa4") || str.equals("457d81b3-3332-48bf-96c4-121b2c76fbc5") ||
				str.equals("d81e5031-67d4-459a-b200-45584ccff5b0") || str.equals("c6a72e0b-3a13-483f-96a8-a729a9d02747") ||
				str.equals("90ca5c33-31a4-4453-aadb-5ea024d683bb") || str.equals("5d2fad32-cb20-46f7-ab87-b272bca9dd5a") ||
				str.equals("60550d2c-3e4d-40fd-9d54-e197972ead3d") || str.equals("e3e3ada7-bdf6-4218-9e58-2a16ddb453da");
	}
}