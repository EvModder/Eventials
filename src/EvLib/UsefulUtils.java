package EvLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import EvLib.ReflectionUtils.RefClass;
import EvLib.ReflectionUtils.RefField;
import EvLib.ReflectionUtils.RefMethod;

public class UsefulUtils{
	public static float getBlockStrength(Material block){
		RefClass classBlock = ReflectionUtils.getRefClass("{nms}.Block");
		RefMethod methodGetByName = classBlock.getMethod("getByName");
		RefField field = classBlock.getField("strength");
		return (Float) field.of( methodGetByName.of(null).call(block.name()) ).get();
	}

	public static String getNormalizedName(EntityType entity){
		//TODO: improve this algorithm / test for errors
		switch(entity){
		case PIG_ZOMBIE:
			return "Zombie Pigman";
		case MUSHROOM_COW:
			return "Mooshroom";
		default:
			boolean wordStart = true;
			char[] arr = entity.name().toCharArray();
			for(int i=0; i<arr.length; ++i){
				if(wordStart) wordStart = false;
				else if(arr[i] == '_' || arr[i] == ' '){arr[i] = ' '; wordStart = true;}
				else arr[i] = Character.toLowerCase(arr[i]);
			}
			return new String(arr);
		}
	}

	static long[] scale = new long[]{31536000000L, /*2628000000L,*/ 604800000L, 86400000L, 3600000L, 60000L, 1000L};
	static char[] units = new char[]{'y', /*'m',*/ 'w', 'd', 'h', 'm', 's'};
	public static String formatTime(long time, ChatColor timeColor, ChatColor unitColor){
		return formatTime(time, timeColor, unitColor, scale, units);
	}
	public static String formatTime(long time, ChatColor timeColor, ChatColor unitColor, long[] scale, char[] units){
		int i = 0;
		while(time < scale[i]) ++i;
		StringBuilder builder = new StringBuilder("");
		for(; i < scale.length-1; ++i){
			builder.append(timeColor).append(time / scale[i]).append(unitColor).append(units[i]).append(", ");
			time %= scale[i];
		}
		return builder.append(timeColor).append(time / scale[scale.length-1])
					  .append(unitColor).append(units[units.length-1]).toString();
	}

	public static Collection<Advancement> getVanillaAdvancements(Player p){
		Vector<Advancement> advs = new Vector<Advancement>();
		Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
		while(it.hasNext()){
			Advancement adv = it.next();
			if(adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) 
					&& p.getAdvancementProgress(adv).isDone())
				advs.add(adv);
		}
		return advs;
	}
	public static Collection<Advancement> getVanillaAdvancements(Player p, Collection<String> include){
		Vector<Advancement> advs = new Vector<Advancement>();
		Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
		while(it.hasNext()){
			Advancement adv = it.next();
			int i = adv.getKey().getKey().indexOf('/');
			if(adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) && i != -1
					&& include.contains(adv.getKey().getKey().substring(0, i))
					&& p.getAdvancementProgress(adv).isDone())
				advs.add(adv);
		}
		return advs;
	}
	public static Collection<Advancement> getVanillaAdvancements(Collection<String> include){
		Vector<Advancement> advs = new Vector<Advancement>();
		Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
		while(it.hasNext()){
			Advancement adv = it.next();
			int i = adv.getKey().getKey().indexOf('/');
			if(adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) && i != -1
					&& include.contains(adv.getKey().getKey().substring(0, i)))
				advs.add(adv);
		}
		return advs;
	}

	public static boolean notFar(Location from, Location to){
		int x1 = from.getBlockX(), y1 = from.getBlockY(), z1 = from.getBlockZ(),
			x2 = to.getBlockX(), y2 = to.getBlockY(), z2 = to.getBlockZ();

		return (Math.abs(x1 - x2) < 20 &&
				Math.abs(y1 - y2) < 15 &&
				Math.abs(z1 - z2) < 20 &&
				from.getWorld().getName().equals(to.getWorld().getName()));
	}

	public static String executePost(String post){
		URLConnection connection = null;
		try{
			connection = new URL(post).openConnection();
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Get response
//			Scanner s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
//			String response = s.hasNext() ? s.next() : null;
//			s.close();
//			return response;
			BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = rd.readLine();
			rd.close();
			return line;
		}
		catch(IOException e){
			System.out.println(e.getStackTrace());
			return null;
		}
	}

	static HashMap<String, Boolean> exists = new HashMap<String, Boolean>();
	public static boolean checkExists(String player){
		if(!exists.containsKey(player)){
			//Sample data (braces included): {"id":"34471e8dd0c547b9b8e1b5b9472affa4","name":"EvDoc"}
			String data = executePost("https://api.mojang.com/users/profiles/minecraft/"+player);
			exists.put(player, data != null);
		}
		return exists.get(player);
	}
}