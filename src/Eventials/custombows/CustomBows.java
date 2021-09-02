package Eventials.custombows;

import java.util.Iterator;
import java.util.Random;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.NBTTagUtils;
import net.evmodder.EvLib.extras.NBTTagUtils.RefNBTTagCompound;

public class CustomBows implements Listener{
	public enum BowType{FLINT, FINDER, FOLLOWER, GANDIVA, ICHAIVAL, DETERMINED, RAPIDFIRE, FORCE, TARGETFIRE, ICARUS, NONE};
	private Eventials plugin;
	final boolean PREVENT_NO_DAMAGE_TICKS;
	final Random rand;

	public CustomBows(Eventials pl){
		plugin = pl;
		rand = new Random();
		PREVENT_NO_DAMAGE_TICKS = plugin.getConfig().getBoolean("prevent-arrow-invulnerability", true);

		if(PREVENT_NO_DAMAGE_TICKS){
			plugin.getServer().getPluginManager().registerEvents(new Listener(){
				@EventHandler public void onProjectileHitEntity(EntityDamageByEntityEvent evt){
					if(evt.getCause() == DamageCause.PROJECTILE && evt.getEntity() instanceof LivingEntity){	
						LivingEntity entity = (LivingEntity)evt.getEntity();
						entity.setHealth(entity.getHealth()-evt.getFinalDamage());
//						evt.setCancelled(true);
						evt.setDamage(0);//TODO: test if this works
					}
				}
			}, plugin);
		}
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		new CommandMakeBow(plugin);
	}

	public BowType getBowType(ItemStack bow){
		if(bow == null || (bow.getType() != Material.BOW && bow.getType() != Material.CROSSBOW)) return BowType.NONE;
		RefNBTTagCompound tag = NBTTagUtils.getTag(bow);
		if(tag == null) return BowType.NONE;
		String bowTypeName = tag.getString("BowType");
		if(bowTypeName == null || bowTypeName.isEmpty()) return BowType.NONE;
		return BowType.valueOf(bowTypeName.toUpperCase());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBowShootEvent(EntityShootBowEvent evt){
		if(evt.isCancelled() || evt.getBow() == null || !evt.getBow().hasItemMeta()) return;
		BowType customBowType = getBowType(evt.getBow());
		if(customBowType == BowType.NONE){
			if(evt.getBow().getItemMeta().hasCustomModelData() && evt.getBow().getItemMeta().getCustomModelData() == 2020){
				plugin.getLogger().warning("CustomBow is missing a BowType! Shooter: "+evt.getEntity().getName());
			}
			return;
		}
		switch(customBowType){
			case TARGETFIRE://no random offset
				Vector trueVec = evt.getEntity().getEyeLocation().getDirection().normalize().multiply(evt.getProjectile().getVelocity().length());
				evt.getProjectile().setVelocity(trueVec);
				return;
			case DETERMINED://can fire non-arrow projectiles
				if(evt.getProjectile() instanceof Arrow == false) return;
				if(evt.getEntity() instanceof Player == false) return;
				ItemStack item = ((Player)evt.getEntity()).getInventory().getItemInOffHand();
				if(item == null) return;
				Class<? extends Projectile> projectileClass;
				switch(item.getType()){
					case ENDER_PEARL: projectileClass = EnderPearl.class; break;
					case SNOWBALL: projectileClass = Snowball.class; break;
					case EGG: projectileClass = Egg.class; break;
					case FIRE_CHARGE: projectileClass = SmallFireball.class; break;
					case EXPERIENCE_BOTTLE: projectileClass = ThrownExpBottle.class; break;
					case SPECTRAL_ARROW: projectileClass = SpectralArrow.class; break;
					case WITHER_SKELETON_SKULL: projectileClass = WitherSkull.class; break;
					case SPLASH_POTION: projectileClass = ThrownPotion.class; break;
					case ARROW: default: return;
				}
				evt.setCancelled(true);
				evt.getEntity().launchProjectile(projectileClass, evt.getProjectile().getVelocity());
				return;
			case FINDER://particles where arrows land
				return;
			case FLINT://up to 2x arrow velocity (TODO: require more drawback?)
				evt.getProjectile().setVelocity(evt.getProjectile().getVelocity().multiply(2));
				return;
			case FOLLOWER://arrows trace nearest entity
				return;
			case FORCE://travels through entities, hitting all
				return;
			case GANDIVA://regular god bow + attribute modifiers.
				return;
			case ICHAIVAL://shoots extra arrows
				if(evt.getEntity().getScoreboardTags().contains("temp_ignore_arrows")) return;
				int extraArrows = 9;
				if(!evt.getBow().containsEnchantment(Enchantment.ARROW_INFINITE)
						&& evt.getEntity() instanceof Player && ((Player)evt.getEntity()).getGameMode() != GameMode.CREATIVE){
					Iterator<ItemStack> missingArrows = ((Player)evt.getEntity()).getInventory()
							.removeItem(new ItemStack(Material.ARROW, extraArrows)).values().iterator();
					if(missingArrows.hasNext()) extraArrows -= missingArrows.next().getAmount();
				}
				evt.getEntity().addScoreboardTag("temp_ignore_arrows");
				for(int i=0; i<extraArrows; ++i){
					Vector variance = new Vector(rand.nextFloat()*.1F-.05F, rand.nextFloat()*.1F-.05F, rand.nextFloat()*.1F-.05F);
					Vector velocity = evt.getProjectile().getVelocity().clone().add(evt.getProjectile().getVelocity().multiply(variance));
					Arrow e = evt.getEntity().launchProjectile(Arrow.class, velocity);
					e.setBounce(false);
				}
				evt.getEntity().removeScoreboardTag("temp_ignore_arrows");
				return;
			case ICARUS:
				evt.getProjectile().setGravity(false);
				evt.getProjectile().setGlowing(true);
				evt.getProjectile().setSilent(true);
				evt.getProjectile().setVelocity(evt.getProjectile().getVelocity().multiply(.25));
				return;
			case RAPIDFIRE://faster firing rate, uses XP?
				return;
			case NONE:
				return;
		}
	}
}