package Eventials.custombows;

import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class Traveler{
	static void doPower(ProjectileSource shooter, ItemStack bow, final Arrow arrow){
		arrow.setGravity(false);
		arrow.setGlowing(true);
		arrow.setSilent(true);
		arrow.setVelocity(arrow.getVelocity().multiply(.25));
		arrow.setKnockbackStrength(arrow.getKnockbackStrength()*2);
	}
}
