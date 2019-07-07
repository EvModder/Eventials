package EventAndMisc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.ActionBarUtils;

public class HC_SpectatorListener implements Listener{
	final Eventials pl;
	final HashSet<UUID> spectators;
	final int MAX_DIST_SQ = 32*32;
	final float FLY_SPEED = 0.1f;

	public HC_SpectatorListener(){
		pl = Eventials.getPlugin();
		spectators = new HashSet<UUID>();
		pl.getServer().getPluginManager().registerEvents(this, pl);
		runSpecatorLoop();
	}

	static boolean isSpectator(Player player){
		return !player.isOp() && (
				player.getGameMode() == GameMode.SPECTATOR ||
				player.isDead()
		);
	}

	static Location getClosest(Location loc, HashSet<Location> points){
		Location closest = null;
		double cDist = Double.MAX_VALUE;
		for(Location point : points){
			double pDist = loc.distanceSquared(point);
			if(pDist < cDist){
				closest = point;
				cDist = pDist;
			}
		}
		return closest != null ? closest : (points.isEmpty() ? null : points.iterator().next());
	}

	boolean loopActive = false;
	void runSpecatorLoop(){
		if(loopActive) return;
		loopActive = true;
		new BukkitRunnable(){@Override public void run(){
			HashSet<Location> nonSpecLocs = new HashSet<Location>();
			for(Player p : pl.getServer().getOnlinePlayers()){
				if(isSpectator(p)) addSpectator(p);
				else if(p.getGameMode() == GameMode.SURVIVAL) nonSpecLocs.add(p.getLocation());
			}
			if(nonSpecLocs.isEmpty()){
				for(UUID uuid : spectators){
					removeSpectator(uuid, false);
					OfflinePlayer p = pl.getServer().getPlayer(uuid);
					if(p != null && p.isOnline()) p.getPlayer().kickPlayer(
							ChatColor.RED+"There is nobody online to spectate right now");
				}
				spectators.clear();
			}
			else{
				Iterator<UUID> it = spectators.iterator();
				while(it.hasNext()){
					UUID uuid = it.next();
					OfflinePlayer p = pl.getServer().getPlayer(uuid);
					if(p == null || !p.isOnline() || !isSpectator(p.getPlayer())) it.remove();
				}
			}
			if(spectators.isEmpty()){
				pl.getLogger().info("No spectators remaining, setting loopAtive=false");
				//HandlerList.unregisterAll(SpectatorListener.this);
				cancel();
				loopActive = false;
			}
			else{
				for(UUID uuid : spectators){
					Player specP = pl.getServer().getPlayer(uuid).getPlayer();
					Location aliveP = getClosest(specP.getLocation(), nonSpecLocs);
					if(specP.getLocation().distanceSquared(aliveP) > MAX_DIST_SQ){
						specP.teleport(aliveP);
					}
					int SECONDS_UNTIL_RESPAWN = 60*60*24; //1 day
					int secondsSinceDeath = specP.getStatistic(Statistic.TIME_SINCE_DEATH) / 20;
					int secondsLeft = SECONDS_UNTIL_RESPAWN - secondsSinceDeath;
					if(secondsLeft <= 0){
						specP.kickPlayer(ChatColor.GREEN+"You may now respawn!");
						secondsLeft = 0;
					}
					int minutesLeft = secondsLeft / 60, hoursLeft = minutesLeft / 60, daysLeft = hoursLeft / 60;
					secondsLeft %= 60; minutesLeft %= 60; hoursLeft %= 24;
					//Too spammy
					/*String respawnDisplayCmd
							= "title "+specP.getName()+" actionbar [\"\","
							+ "{\"text\":\"Respawn in: \",\"color\":\"gray\"},"
							+ "{\"text\":\""+hoursLeft+"\",\"color\":\"yellow\"},"
							+ "{\"text\":\":\",\"color\":\"gray\"},"
							+ "{\"text\":\""+minutesLeft+"\",\"color\":\"yellow\"},"
							+ "{\"text\":\":\",\"color\":\"gray\"},"
							+ "{\"text\":\""+secondsLeft+"\",\"color\":\"yellow\"}]";
					pl.runCommand(respawnDisplayCmd);*/
					StringBuilder builder = new StringBuilder("")
							.append(ChatColor.GRAY).append("Respawn in: ").append(ChatColor.GOLD);
					if(daysLeft > 0) builder.append(daysLeft)
							.append(ChatColor.GRAY).append('d').append(ChatColor.GOLD);
					if(hoursLeft > 0) builder.append(hoursLeft < 10 ? "0"+hoursLeft : hoursLeft)
							.append(ChatColor.GRAY).append('h').append(ChatColor.GOLD);
					if(minutesLeft > 0) builder.append(minutesLeft < 10 ? "0"+minutesLeft : minutesLeft)
							.append(ChatColor.GRAY).append('m').append(ChatColor.GOLD);
					if(secondsLeft > 0) builder.append(secondsLeft < 10 ? "0"+secondsLeft : secondsLeft)
							.append(ChatColor.GRAY).append('s');
					ActionBarUtils.sendToPlayer(builder.toString(), specP);
				}
			}
		}}.runTaskTimer(pl, 20, 20);
	}

	public void addSpectator(Player player){
		if(spectators.add(player.getUniqueId())){
			pl.getLogger().info("Added spectator: "+player.getName());
			player.setFlySpeed(FLY_SPEED);
			player.getScoreboard().getTeam("Spectators").addEntry(player.getName());
			AC_Hardcore.setPermission(player, "essentials.tpa", false);
			AC_Hardcore.setPermission(player, "essentials.tpahere", false);
			AC_Hardcore.setPermission(player, "essentials.tpaccept", false);
			player.getScoreboard().resetScores(player.getName());
			player.getScoreboard().getTeam("Spectators").addEntry(player.getName());
			runSpecatorLoop();
		}
	}
	public boolean removeSpectator(UUID uuid, boolean removeFromSet){
		OfflinePlayer player = pl.getServer().getOfflinePlayer(uuid);
		if(player != null){
			pl.getServer().getScoreboardManager().getMainScoreboard()
					.getTeam("Spectators").removeEntry(player.getName());
			player.getPlayer().setFlySpeed(0.2f);
		}
		if(removeFromSet && spectators.remove(uuid)){
			pl.getLogger().info("Removed spectator: "+uuid);
			return true;
		}
		return false;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent evt){
		if(removeSpectator(evt.getPlayer().getUniqueId(), true)
				&& evt.getPlayer().getScoreboardTags().contains("dead")){
			evt.getPlayer().getScoreboard().resetScores(evt.getPlayer().getName());
			int ticksSinceDeath = evt.getPlayer().getStatistic(Statistic.TIME_SINCE_DEATH);
			int hrsSinceDeath = ticksSinceDeath/(20*60*60);
			pl.getLogger().info("Ticks since death: "+ticksSinceDeath);
			pl.getLogger().info("Hours since death: "+(((double)ticksSinceDeath)/(20*60*60)));
			if(hrsSinceDeath >= 24){
				//Reset playerdata & stats so next time they log in they will respawn :)
				final UUID uuid = evt.getPlayer().getUniqueId();
				new BukkitRunnable(){@Override public void run(){
					AC_Hardcore.deletePlayerdata(uuid);
				}}.runTaskLater(pl, 5);
			}
		}
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent evt){
		final UUID uuid = evt.getPlayer().getUniqueId();
		OfflinePlayer offP = pl.getServer().getOfflinePlayer(uuid);
		final long millisSinceLastLogin = System.currentTimeMillis() - offP.getLastPlayed();
		final int ticksSinceLastLogin = (int)(millisSinceLastLogin/50);
		if(ticksSinceLastLogin > 0){
			new BukkitRunnable(){@Override public void run(){
				Player p = pl.getServer().getPlayer(uuid);
				if(p != null && isSpectator(p)){
					addSpectator(evt.getPlayer());
					final double inHrs = ((double)ticksSinceLastLogin)/(20*60*60);
					pl.getLogger().info("Adding: "+inHrs+"h to SinceLastDeath (ticks="+ticksSinceLastLogin+")");
					p.incrementStatistic(Statistic.TIME_SINCE_DEATH, ticksSinceLastLogin);
				}
			}}.runTaskLater(pl, 20);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent evt){
		if(isSpectator(evt.getPlayer())) addSpectator(evt.getPlayer());
	}

	@EventHandler
	public void onGameModeChange(PlayerGameModeChangeEvent evt){
		if(evt.getNewGameMode() == GameMode.SPECTATOR){
			if(isSpectator(evt.getPlayer())) addSpectator(evt.getPlayer());
			evt.getPlayer().getScoreboard().getTeam("Spectators").addEntry(evt.getPlayer().getName());
		}
		else removeSpectator(evt.getPlayer().getUniqueId(), true);
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent evt){
		final UUID uuid = evt.getPlayer().getUniqueId();
		new BukkitRunnable(){@Override public void run(){
			Player p = pl.getServer().getPlayer(uuid);
			if(p != null) addSpectator(p);
		}}.runTaskLater(pl, 20*5);
	}
}