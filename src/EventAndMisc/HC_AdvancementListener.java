package EventAndMisc;

import java.util.HashSet;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import Eventials.Eventials;
import net.evmodder.EvLib.EvUtils;

public class HC_AdvancementListener implements Listener{
	final HashSet<String> included;
	final Eventials pl;

	public HC_AdvancementListener(){
		pl = Eventials.getPlugin();
		included = new HashSet<String>();
		included.addAll(pl.getConfig().getStringList("advancements-included"));
	}

	public boolean isPaidAdvancement(Advancement adv){
		int i = adv.getKey().getKey().indexOf('/');
		return adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) && i != -1
				&& included.contains(adv.getKey().getKey().substring(0, i));
	}

	@EventHandler
	public void onAchievementGet(PlayerAdvancementDoneEvent evt){
		if(!isPaidAdvancement(evt.getAdvancement())) return;

		int advancements = EvUtils.getVanillaAdvancements(evt.getPlayer(), included).size();
		evt.getPlayer().getScoreboard().getObjective("advancements")
			.getScore(evt.getPlayer().getName()).setScore(advancements);
	}
}