package Eventials.splitworlds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.ReflectionUtils;
import net.evmodder.EvLib.extras.ReflectionUtils.RefClass;
import net.evmodder.EvLib.extras.ReflectionUtils.RefMethod;

public final class SplitWorldUtils{
	// WARNING: Doesn't work with multiple '*' in the same string!
	public static Collection<List<String>> findMatchGroups(List<String> strs, List<String> search){
		HashSet<String> validSubs = null;
		final List<String> staticTerms = new ArrayList<String>();
		final List<String> complexTerms = new ArrayList<String>();

		for(String m : search){
			int wild = m.indexOf('*');
			if(wild == -1){
				if(!strs.contains(m)){
					Eventials.getPlugin().getLogger().warning("Unknown world: "+m);
					continue;// return new ArrayList<>();
				}
				staticTerms.add(m);
			}
			else{
				complexTerms.add(m);
				String preM = m.substring(0, wild), postM = m.substring(wild+1);
				final HashSet<String> encounteredSubs = new HashSet<String>();
				for(String s : strs){
					if(s.startsWith(preM) && s.endsWith(postM)){
						String sub = s.substring(preM.length(), s.length()-postM.length());
						encounteredSubs.add(sub);
					}
				}
				if(validSubs == null) validSubs = encounteredSubs;
				else validSubs.retainAll(encounteredSubs);
			}
		}
		if(validSubs == null) return Arrays.asList(staticTerms);
		final List<List<String>> matchGroups = new ArrayList<List<String>>();
		for(String sub : validSubs){
			final List<String> mGroup = new ArrayList<String>(staticTerms);
			for(String term : complexTerms) mGroup.add(term.replace("*", sub));
			matchGroups.add(mGroup);
		}
		return matchGroups;
	}

	/*public static void scheduleUntrackedTeleport(final SplitWorlds sw,
			final UUID playerUUID, final Location destination, final long delay, boolean loadInv){
		new BukkitRunnable(){@Override public void run(){
			Player player = org.bukkit.Bukkit.getServer().getPlayer(playerUUID);
			if(player != null){
				if(loadInv) sw.loadProfile(player, destination.getWorld().getName());

				untrackedTeleport(player, destination);
			}
		}}.runTaskLater(Eventials.getPlugin(), delay);
	}*/

	//Reflection
	static final RefClass classCraftPlayer = ReflectionUtils.getRefClass("{cb}.entity.CraftPlayer");
	static final RefClass classCraftWorld = ReflectionUtils.getRefClass("{cb}.CraftWorld");
	static final RefClass classWorldServer = ReflectionUtils.getRefClass("{nms}.WorldServer", "{nms}.level.WorldServer");
	static final RefClass classEntity = ReflectionUtils.getRefClass("{nms}.Entity", "{nm}.world.entity.Entity");
	static RefMethod methodGetPlayerHandle = classCraftPlayer.getMethod("getHandle");
	static RefMethod methodGetWorldHandle = classCraftWorld.getMethod("getHandle");
	static RefMethod methodUnregisterEntity;// = classWorldServer.getMethod("unregisterEntity", classEntity);
	private static void untrackPlayer(Player player, org.bukkit.World world){
		try{methodUnregisterEntity = classWorldServer.getMethod("unregisterEntity", classEntity);}
		catch(Exception e){
			try{methodUnregisterEntity = classWorldServer.getMethod("f", classEntity);}
			catch(Exception e2){methodUnregisterEntity = null;}
		}
		Object playerHandle = methodGetPlayerHandle.of(player).call();
		Object worldHandle = methodGetWorldHandle.of(world).call();
		if(methodUnregisterEntity != null) methodUnregisterEntity.of(worldHandle).call(playerHandle);
	}
	private static void retrackPlayer(Player player, org.bukkit.World world){
		//((CraftWorld)destination.getWorld()).getHandle().addEntity(playerHandle);
		//tracker.updatePlayer(playerHandle);
	}

	public static boolean untrackedTeleport(final Player player, final Location destination, boolean skipInvCheck){
		if(skipInvCheck){
			player.setMetadata(SplitWorlds.SKIP_TP_INV_CHECK_TAG, new FixedMetadataValue(Eventials.getPlugin(), ""));
		}
		untrackPlayer(player, destination.getWorld());
		boolean success = player.teleport(destination);
		retrackPlayer(player, destination.getWorld());
		return success;
	}

	public static void resetPlayer(Player player){
		player.setGameMode(GameMode.SURVIVAL);
//		player.setHealth(20D);

//		player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20D);
		for(AttributeModifier modifier : player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getModifiers()){
			player.getAttribute(Attribute.GENERIC_MAX_HEALTH).removeModifier(modifier);
		}
//		player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(0);
		for(AttributeModifier modifier : player.getAttribute(Attribute.GENERIC_ARMOR).getModifiers()){
			player.getAttribute(Attribute.GENERIC_ARMOR).removeModifier(modifier);
		}
//		player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(0);
//		for(AttributeModifier modifier : player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getModifiers()){
//			player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).removeModifier(modifier);
//		}
		for(AttributeModifier modifier : player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getModifiers()){
			player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).removeModifier(modifier);
		}

//		player.setFoodLevel(20);
//		player.setExhaustion(0F);
//		player.setLevel(0);
//		player.setExp(0F);
//		for(PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
//		player.getInventory().clear();
//		player.getEnderChest().clear();
		
//		player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
//		for(ItemStack item : player.getInventory().getArmorContents()){
//			if(item == null || item.getType() == Material.AIR){
//				item.setType(Material.DIAMOND_CHESTPLATE);
//				item.setType(Material.AIR);
//			}
//		}
//		ItemStack[] armor = player.getInventory().getArmorContents();
//		player.getInventory().setHelmet(new ItemStack(Material.AIR));
//		player.getInventory().setChestplate(new ItemStack(Material.AIR));
//		player.getInventory().setLeggings(new ItemStack(Material.AIR));
//		player.getInventory().setBoots(new ItemStack(Material.AIR));
//		player.getInventory().setArmorContents(armor);
	}
}