package Eventials.splitworlds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.ReflectionUtils;
import net.evmodder.EvLib.extras.ReflectionUtils.RefClass;
import net.evmodder.EvLib.extras.ReflectionUtils.RefMethod;

public final class SplitWorldUtils{
	// WARNING: Doesn't work with multiple '*' in the same string!
	public static Collection<List<String>> findMatchGroups(List<String> strs, List<String> group_members){
		HashSet<String> validSubs = null;
		final List<String> staticTerms = new ArrayList<>();
		final List<String> complexTerms = new ArrayList<>();

		for(String m : group_members){
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
				final HashSet<String> encounteredSubs = new HashSet<>();
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
		final List<List<String>> matchGroups = new ArrayList<>();
		for(String sub : validSubs){
			final List<String> mGroup = new ArrayList<>(staticTerms);
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

	// List of things that get lost / messed up when switching player data file
	public static class PlayerState{
		GameMode gm;
		ItemStack helm, chest, leg, boot;
		List<PotionEffect> effects = new ArrayList<>();
		// TODO: handle attributes gained/lost from armor/handheld item(s) added/removed in /invsee
		HashMap<Attribute, Double> attributeBaseValues = new HashMap<>();
		HashMap<Attribute, Collection<AttributeModifier>> attributeModifiers = new HashMap<>();
	};
	public static PlayerState getPlayerState(Player player){
		PlayerState state = new PlayerState();
		state.gm = player.getGameMode();
		state.helm = player.getInventory().getHelmet();
		state.chest = player.getInventory().getChestplate();
		state.leg = player.getInventory().getLeggings();
		state.boot = player.getInventory().getBoots();
		state.effects.addAll(player.getActivePotionEffects());
		for(Attribute attribute : Registry.ATTRIBUTE){
			AttributeInstance inst = player.getAttribute(attribute);
			if(inst != null){
				state.attributeBaseValues.put(attribute, inst.getBaseValue());
				state.attributeModifiers.put(attribute, inst.getModifiers());
			}
		}
		return state;
	}
	public static void resetPlayerState(Player player){
		player.setGameMode(player.getServer().getDefaultGameMode());// TODO: something more elegant (per-world) like Multiverse API?
//		player.setHealth(20D);
//		player.setFoodLevel(20);
//		player.setExhaustion(0F);
//		player.setLevel(0);
//		player.setExp(0F);
//		player.resetTitle();
//		player.getInventory().clear();
//		player.getEnderChest().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);

//		player.getActivePotionEffects().clear();
		for(PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
		for(Attribute attribute : Registry.ATTRIBUTE){
			AttributeInstance inst = player.getAttribute(attribute);
			if(inst != null){
				inst.setBaseValue(inst.getDefaultValue());
//				inst.getModifiers().clear();
				for(AttributeModifier modifier : inst.getModifiers()) inst.removeModifier(modifier);
			}
		}
	}
	public static PlayerState loadPlayerState(Player player, PlayerState newState){
		PlayerState oldState = getPlayerState(player);
		resetPlayerState(player);

		if(newState != null){
			newState.attributeBaseValues.forEach((attribute, value) -> player.getAttribute(attribute).setBaseValue(value));
//			newState.attributeModifiers.forEach((attribute, list) -> player.getAttribute(attribute).addAll(list));
			newState.attributeModifiers.forEach((attribute, list) -> list.forEach(modifier -> player.getAttribute(attribute).addModifier(modifier)));
			player.addPotionEffects(newState.effects);
			player.getInventory().setHelmet(newState.helm);
			player.getInventory().setChestplate(newState.chest);
			player.getInventory().setLeggings(newState.leg);
			player.getInventory().setBoots(newState.boot);
			player.setGameMode(newState.gm);
		}
		return oldState;
	}
}