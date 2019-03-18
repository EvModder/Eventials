package Eventials.custombows;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.util.Vector;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.ChatColor;
import Eventials.Eventials;
import Eventials.custombows.CustomBows.BowType;

public class Determined implements Listener{
	public static final int MAX_ITEMS = 6;

	Determined(){
		Eventials.getPlugin().getServer().getPluginManager().registerEvents(this, Eventials.getPlugin());
	}

	boolean canLoadItem(ItemStack item){//can this item be loaded into BowType.Determined?
		if((item.hasItemMeta() && item.getItemMeta().hasLore() || !item.getEnchantments().isEmpty())) return false;
		switch(item.getType()){
			case ARROW:
			case ENDER_PEARL:
			case SNOWBALL:
			case EGG:
			case FIRE_CHARGE:
			case SPLASH_POTION:
			case EXPERIENCE_BOTTLE:
			case SPECTRAL_ARROW:
			case WITHER_SKELETON_SKULL:
			case BAT_SPAWN_EGG:
			case BLAZE_SPAWN_EGG:
			case CAVE_SPIDER_SPAWN_EGG:
			case CHICKEN_SPAWN_EGG:
			case COD_SPAWN_EGG:
			case COW_SPAWN_EGG://TODO: ... all the other spawn eggs
				return true;
			default:
				return false;
		}
	}

	static void doPower(ProjectileSource shooter, ItemStack bow, Arrow arrow){
		//shoot whatever the bow is loaded with (max: 6 lines of items loaded. Cannot use infinity.)
		//Note: will need to have an arrow in inv for minecraft to do the bow drawback,
		//so maybe load items into that arrow? (Perhaps give arrow instead of the bow? Or both together in a pair)
		ItemMeta meta = bow.getItemMeta();
		List<String> loadedItems = meta.getLore();
		if(loadedItems == null || loadedItems.isEmpty()) return;
		String[] firstItem = ChatColor.stripColor(loadedItems.get(0)).split(" x ");
		int amountLeft = Integer.parseInt(firstItem[0])-1;
		if(amountLeft == 0) loadedItems.remove(0);
		else{
			StringBuilder builder = new StringBuilder("").append(ChatColor.GREEN).append(amountLeft)
					.append(ChatColor.GRAY).append(" x ").append(ChatColor.GOLD).append(firstItem[1]);
			loadedItems.set(0, builder.toString());
		}
		meta.setLore(loadedItems);
		bow.setItemMeta(meta);
		
		String[] itemData = firstItem[1].split(":");
		Material arrowType = Material.getMaterial(itemData[0]);
//		byte data = itemData.length == 2 ? Byte.parseByte(itemData[1]) : 0;

		Vector velocity = arrow.getVelocity();
		switch(arrowType){
		//SPLASH_POTION MONSTER_EGG, special arrows
			case ARROW:
				return;
			case ENDER_PEARL:
				shooter.launchProjectile(EnderPearl.class, velocity);
				break;
			case SNOWBALL:
				shooter.launchProjectile(Snowball.class, velocity);
				break;
			case EGG:
				shooter.launchProjectile(Egg.class, velocity);
				break;
			case FIRE_CHARGE:
				SmallFireball p = shooter.launchProjectile(SmallFireball.class, velocity);
				p.setIsIncendiary(true);
				break;
			case EXPERIENCE_BOTTLE:
				shooter.launchProjectile(ThrownExpBottle.class, velocity);
				break;
			case SPECTRAL_ARROW:
				shooter.launchProjectile(SpectralArrow.class, velocity);
				break;
			case WITHER_SKELETON_SKULL:
				WitherSkull w = (WitherSkull)shooter.launchProjectile(WitherSkull.class, velocity.multiply(2));
				w.setCharged(true);
				w.setGlowing(true);
				w.setYield(2);
				break;
			case SPLASH_POTION:
				shooter.launchProjectile(ThrownPotion.class, velocity);
				break;
			default:
				break;
		}
		arrow.remove();
	}

	@SuppressWarnings("deprecation")
	@EventHandler public void onInventoryItemMoveEvent(InventoryClickEvent evt){
		if(evt.getAction() == InventoryAction.SWAP_WITH_CURSOR){
			if(CustomBows.getBowType(evt.getCurrentItem()) == BowType.Determined){
				//load item into bow!
				if(canLoadItem(evt.getCursor())){
					evt.setCancelled(true);
					ItemMeta meta = evt.getCurrentItem().getItemMeta();
					List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
					if(lore.size() >= MAX_ITEMS) return;
					
					ItemStack item = evt.getCursor();
					StringBuilder addItemBuilder = new StringBuilder().append(ChatColor.GREEN)
							.append(item.getAmount()).append(ChatColor.GRAY).append(" x ")
							.append(ChatColor.GOLD).append(item.getType().toString());
					if(item.getData().getData() != 0) addItemBuilder
						.append(ChatColor.GRAY).append(':').append(ChatColor.GOLD)
						.append(item.getData().getData());
					lore.add(addItemBuilder.toString());
					meta.setLore(lore);
					evt.getCurrentItem().setItemMeta(meta);
					evt.getWhoClicked().setItemOnCursor(null);
				}
			}
		}
		else if(evt.getAction() == InventoryAction.PICKUP_HALF){
			if(CustomBows.getBowType(evt.getCurrentItem()) == BowType.Determined){
				evt.setCancelled(true);

				ItemMeta meta = evt.getCurrentItem().getItemMeta();
				List<String> loadedItems = meta.getLore();
				if(loadedItems == null || loadedItems.isEmpty()) return;

				String[] firstItem = ChatColor.stripColor(loadedItems.remove(0)).split(" x ");
				int amount = Integer.parseInt(firstItem[0]);
				String[] typeData = firstItem[1].split(":");
				String name = typeData[0], damage = typeData.length > 1 ? typeData[1] : "";

				meta.setLore(loadedItems);
				evt.getCurrentItem().setItemMeta(meta);
				
				evt.setCursor(damage.isEmpty() ? new ItemStack(Material.getMaterial(name), amount) :
					new ItemStack(Material.getMaterial(name), amount, Short.parseShort(damage)));
			}
		}
	}
}
