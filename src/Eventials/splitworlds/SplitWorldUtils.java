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
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import Eventials.Eventials;
import net.minecraft.server.v1_14_R1.EntityPlayer;

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

	//Reflection
/*	static final RefClass classEntityTracker = ReflectionUtils.getRefClass("{nms}.PlayerChunkMap.EntityTracker");
	static final RefClass classCraftWorld = ReflectionUtils.getRefClass("{cb}.CraftWorld");
	static final RefClass classCraftPlayer = ReflectionUtils.getRefClass("{cb}.entity.CraftPlayer");
	static final RefClass classEntity = ReflectionUtils.getRefClass("{nms}.Entity");
	static final RefClass classWorldServer = ReflectionUtils.getRefClass("{nms}.WorldServer");
	static RefMethod methodGetEntityHandle = classCraftPlayer.getMethod("getHandle");
	static RefMethod methodGetWorldHandle = classCraftWorld.getMethod("getHandle");
	static RefMethod methodGetTracker = classWorldServer.getMethod("getTracker");
	static RefMethod methodTrack = classEntityTracker.getMethod("track", classEntity);
	static RefMethod methodUntrackEntity = classEntityTracker.getMethod("untrackEntity", classEntity);*/

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

	public static boolean untrackedTeleport(final Player player, final Location destination, boolean skipInvCheck){
		if(skipInvCheck){
			player.setMetadata(SplitWorlds.SKIP_TP_INV_CHECK_TAG, new FixedMetadataValue(Eventials.getPlugin(), ""));
		}
		/*Object playerHandle = methodGetEntityHandle.of(player).call();
		Object tracker = methodGetTracker.of(methodGetWorldHandle.of(destination.getWorld()).call()).call();
		methodUntrackEntity.of(tracker).call(playerHandle);
		boolean success = player.teleport(destination);
		methodTrack.of(tracker).call(playerHandle);
		return success;*/
		EntityPlayer playerHandle = ((CraftPlayer)player).getHandle();
		//EntityTracker tracker = ((CraftWorld)destination.getWorld()).getHandle().?
		//tracker.clear(playerHandle);
		((CraftWorld)destination.getWorld()).getHandle().unregisterEntity(playerHandle);
		boolean success = player.teleport(destination);
		((CraftWorld)destination.getWorld()).getHandle().addEntity(playerHandle);
		//tracker.updatePlayer(playerHandle);
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