package Eventials.custombows;

import java.util.Random;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class Ichaival {
	private static boolean tempIgnoreArrows;
	private static Random r = new Random();
	public static final int MAX_ARROWS = 9;

	static void doPower(ProjectileSource shooter, ItemStack bow, final Arrow arrow){
		if(tempIgnoreArrows) return;

		int extraArrows = MAX_ARROWS;

		if(shooter instanceof Player && !bow.containsEnchantment(Enchantment.ARROW_INFINITE)
				&& ((Player)shooter).getGameMode() != GameMode.CREATIVE)
		{
			Inventory inv = ((Player)shooter).getInventory();
			int nextArrowSlot = inv.first(Material.ARROW);
			int needed = MAX_ARROWS;
			while(nextArrowSlot != -1 && needed != 0){
				ItemStack arrows = inv.getItem(nextArrowSlot);
				if(arrows.getAmount() > needed){
					needed = 0;
					arrows.setAmount(arrows.getAmount()-needed);
					inv.setItem(nextArrowSlot, arrows);
				}
				else{
					needed -= arrows.getAmount();
					inv.setItem(nextArrowSlot, new ItemStack(Material.AIR));
				}
			}
			extraArrows = MAX_ARROWS-needed;
		}

		tempIgnoreArrows = true;
		for(int i=0; i<extraArrows; ++i){
			Vector variance = new Vector(r.nextFloat()*.1F-.05F,r.nextFloat()*.1F-.05F,r.nextFloat()*.1F-.05F);
			Vector velocity = arrow.getVelocity().clone().add(arrow.getVelocity().multiply(variance));

			Arrow e = shooter.launchProjectile(Arrow.class, velocity);
			e.setBounce(false);
			e.setCritical(arrow.isCritical());
			e.setKnockbackStrength(arrow.getKnockbackStrength());
		}
		tempIgnoreArrows = false;
	}
}