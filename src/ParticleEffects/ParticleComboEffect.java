package ParticleEffects;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class ParticleComboEffect {

	public void display(Location loc, long range){
		range *= range;
		List<Player> pplInRange = new ArrayList<Player>();
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			if(p.getWorld().getName().endsWith(loc.getWorld().getName()) && p.getLocation().distanceSquared(loc) <= range
					&& p.getWorld().getName().equals(loc.getWorld().getName())) pplInRange.add(p);
		}
		display(loc, pplInRange.toArray(new Player[]{}));
	}
	public abstract void display(Location loc, Player... ppl);
}