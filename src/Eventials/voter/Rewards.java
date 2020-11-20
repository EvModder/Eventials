package Eventials.voter;

import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Rewards {
	static Random rand = new Random();
	static Sound[] voteSounds = new Sound[]{
			Sound.ENTITY_HORSE_DEATH, Sound.ENTITY_CAT_DEATH, Sound.ENTITY_GHAST_DEATH,
			Sound.ENTITY_BLAZE_DEATH, Sound.ENTITY_PLAYER_DEATH, Sound.ENTITY_PIG_DEATH,
			Sound.BLOCK_ANVIL_LAND, Sound.BLOCK_GLASS_BREAK, Sound.BLOCK_DISPENSER_FAIL,
			Sound.BLOCK_END_GATEWAY_SPAWN, Sound.BLOCK_ENDER_CHEST_CLOSE,
			Sound.ENTITY_GHAST_SCREAM, Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
			Sound.ENTITY_PLAYER_BREATH, Sound.ENTITY_PLAYER_BURP,
			Sound.MUSIC_CREATIVE, Sound.MUSIC_CREDITS, Sound.MUSIC_MENU, Sound.MUSIC_DRAGON
	};

	public static void giveFirework(Player p){
		//Firework launch!   ----------------------------------------------------
		boolean flicker = rand.nextBoolean(), trail = rand.nextBoolean();

		FireworkMeta meta = (FireworkMeta) new ItemStack(Material.FIREWORK_ROCKET).getItemMeta();
		meta.setPower(rand.nextInt(3));//returns either 0, 1 or 2

		Type type = Type.values()[rand.nextInt(5)];

		ArrayList<Color> color = new ArrayList<Color>(), fade = new ArrayList<Color>();
		int randColors = rand.nextInt(6)+1, randFades = rand.nextInt(3);
		for(int i = 0; i < randColors; ++i) color.add(Color.fromRGB(rand.nextInt(16777216)));
		for(int i = 0; i < randFades; ++i) fade.add(Color.fromRGB(rand.nextInt(16777216)));
		FireworkEffect effect = FireworkEffect.builder()
				.flicker(flicker).withColor(color).withFade(fade).with(type).trail(trail).build();

		meta.addEffect(effect);

//		Firework firework = p.getWorld().spawn(p.getLocation(), Firework.class);
//		firework.setFireworkMeta(meta);
		ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
		firework.setItemMeta(meta);
		p.getInventory().addItem(firework);
	}

	public static void give(Player p){
		//give random items
		p.playSound(p.getLocation(), voteSounds[rand.nextInt(voteSounds.length)], 5, 2);//volume, speed
		p.giveExp(170);//10 levels when default
		//p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 0));//2 mins speed
		p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 12000, 0));//10 mins haste
	}
}