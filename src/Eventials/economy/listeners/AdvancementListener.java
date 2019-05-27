package Eventials.economy.listeners;

import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import Eventials.Eventials;
import Eventials.economy.Economy;
import net.evmodder.EvLib.EvUtils;

public class AdvancementListener implements Listener{
//	Given n=34 (total achs), exp=1.1, and c=18.5, a player will get ~5000 from completing all achs
//	final double c = 18.5;
	final int FINAL_PRIZE, MIN_ADV, MAX_PRIZE;
	final double MULT, BASE;
	HashSet<String> included;
	Eventials pl;

	public AdvancementListener(){
		pl = Eventials.getPlugin();
		FINAL_PRIZE = pl.getConfig().getInt("advancement-completion-prize");
		MULT = pl.getConfig().getDouble("advancement-constant-multiplier");
		BASE = pl.getConfig().getDouble("advancement-exponential-base");
		MIN_ADV = pl.getConfig().getInt("advancement-min-before-reward");//min
		MAX_PRIZE = pl.getConfig().getInt("advancement-max-reward");
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
		if((advancements-=MIN_ADV) <= 0) return;

		long payoff = Math.round(Math.pow(BASE, advancements)*MULT);
		if(payoff > MAX_PRIZE){
			payoff = MAX_PRIZE;
			pl.getLogger().warning("Advancement payoff was higher than max reward setting!\n"
					+ "Consider adjusting advancement rewards in your config");
		}

		int totalAdvs = EvUtils.getVanillaAdvancements(included).size();
		if(advancements == totalAdvs){
			payoff += FINAL_PRIZE;
			pl.getServer().broadcastMessage(new StringBuilder()
					.append(ChatColor.GOLD).append(evt.getPlayer().getName()).append(ChatColor.GREEN)
					.append(" has completed all of Minecraft's advancements!").toString());
		}
		if(Economy.getEconomy().serverToPlayer(evt.getPlayer().getUniqueId(), payoff)){
			pl.getLogger().info(evt.getPlayer().getName()+" was awarded "+payoff);
			pl.runCommand("title "+evt.getPlayer().getName() +
					" actionbar [{\"text\":\"You were awarded \",\"color\":\"green\"}," +
					"{\"text\":\""+payoff+"\",\"color\":\"yellow\"}," +
					"{\"text\":\"L\",\"color\":\"dark_green\"}]");
		}
		else
			pl.getLogger().info("Unable to pay for advancement: "+payoff);
	}
}