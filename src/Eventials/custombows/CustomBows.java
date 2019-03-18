package Eventials.custombows;

import net.minecraft.server.v1_13_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import Eventials.Eventials;

public class CustomBows implements Listener{
	private Eventials plugin;
	public enum BowType{Flint,Finder,Follower,Gandiva,Ichaival,Determined,
						Rapidfire,Force,None};
	private boolean preventInvulnerable;

	Determined d;//TODO: temp eww
	public CustomBows(Eventials pl){
		plugin = pl;
		d=new Determined();

		if(plugin.getConfig().getBoolean("prevent-arrow-invulnerability", true)){
			plugin.getServer().getPluginManager().registerEvents(new Listener(){
				@EventHandler public void onProjectileHitEntity(EntityDamageByEntityEvent evt){
					if(preventInvulnerable && evt.getCause() == DamageCause.PROJECTILE
							&& evt.getEntity() instanceof LivingEntity)
					{	
						LivingEntity entity = (LivingEntity)evt.getEntity();
						entity.setHealth(entity.getHealth()-evt.getFinalDamage());
						
//						evt.setCancelled(true);
						evt.setDamage(0);//TODO: test if this works
					}
				}
			}, plugin);
		}
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		new CommandMakeBow(plugin, this);
	}

	public static BowType getBowType(ItemStack bow){
		if(bow == null || bow.getType() != Material.BOW) return BowType.None;
		NBTTagCompound bowTag = CraftItemStack.asNMSCopy(bow).getTag();
		if(bowTag != null) for(BowType type : BowType.values()){
			if(bowTag.hasKey(type.name())) return type;
		}
		return BowType.None;
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent evt){
		if(evt.isCancelled() || !(evt.getEntity() instanceof Arrow)
				|| !(evt.getEntity().getShooter() instanceof LivingEntity)) return;

		ProjectileSource shooter = evt.getEntity().getShooter();
		ItemStack bow = ((LivingEntity)evt.getEntity().getShooter()).getEquipment().getItemInMainHand();
		Arrow arrow = (Arrow) evt.getEntity();

		BowType type = getBowType(bow);
		if(type == BowType.None){
			return;
		}
		else if(type == BowType.Ichaival){//shoots multiple arrows
			Ichaival.doPower(shooter, bow, arrow);
		}
		else if(type == BowType.Determined){//can shoot random stuff
			Determined.doPower(shooter, bow, arrow);
		}
		else if(type == BowType.Gandiva){//regular op bow. Add power?
		}
		else if(type == BowType.Flint){//up to 2x arrow velocity (with more drawback?)
		}
		else if(type == BowType.Finder){//particles where arrows land
		}
		else if(type == BowType.Follower){//arrows trace nearest entity
		}
		else if(type == BowType.Rapidfire){//fast fire, uses XP?
		}
		else if(type == BowType.Force){//travels through entities, hitting all
		}
	}

	public ItemStack makeBow(BowType type){
		//basic NBT tags
		//tags based on bowtype
		//item meta based on bowtype
		switch(type){
			case Gandiva:
				return Gandiva.makeBow();
			case Ichaival:
				//return;
			case Determined:
				//return;
			case Flint:
				//return;
			case Finder:
				//return;
			case Follower:
				//return;
			case Rapidfire:
				//return;
			case Force:
				//return;
			default:
				return null;
		}
	}
}