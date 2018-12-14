package Extras;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;


public class GhostFactory{
	private static final String VIEWER_TEAM_NAME = "CanSeeInvis";
	private static final String GHOST_TEAM_NAME = "Ghosts";
	private static final int INVIS_LVL = 1;//Used to be 15
	private Listener listener;
	private JavaPlugin plugin;
	
	/**
	 * Team of ghosts and people who can see ghosts.
	 */
	private Team viewerTeam;
	private Team ghostTeam;
	
	// Players that are actually ghosts
	private Set<String> ghosts = new HashSet<String>();
	private Set<String> trueInvis;

	/**
	 * Create a GhostFactory
	 * @param plugin - the plugin creating the ghostfactory
	 * @param ghostsVisibleToAll - true to have ghosts visible to all players on the server
	 */
	public GhostFactory(JavaPlugin pl){
		plugin = pl;
		Scoreboard board = pl.getServer().getScoreboardManager().getMainScoreboard();
		viewerTeam = board.getTeam(VIEWER_TEAM_NAME);
		if(viewerTeam == null) viewerTeam = board.registerNewTeam(VIEWER_TEAM_NAME);
		ghostTeam = board.getTeam(GHOST_TEAM_NAME);
		if(ghostTeam == null) ghostTeam = board.registerNewTeam(GHOST_TEAM_NAME);
		viewerTeam.setCanSeeFriendlyInvisibles(true);
		ghostTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.ALWAYS);
		ghostTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
		
	}

	/**
	 * Remove all existing player members and ghosts.
	 */
	public void clearMembers(){
		for(String player : viewerTeam.getEntries()) viewerTeam.removeEntry(player);
		ghosts.clear();
		trueInvis.clear();
	}
	
	/**
	 * Add the given player(s) to the true-invisibility list.
	 * @param player - the player to add.
	 */
	private void addTrueInvis(Player... player) {
		for(Player p : player){
			trueInvis.add(p.getName());
			for(Player p2 : p.getServer().getOnlinePlayers()){
				if(!p.getName().equals(p2.getName()) && viewerTeam.hasEntry(p2.getName())) p2.hidePlayer(p);
			}
		}
		if(!trueInvis.isEmpty()) runInvisDetector();
	}
	private boolean isRunning;
	private void runInvisDetector(){
		if(isRunning) return;
		isRunning = true;
		new BukkitRunnable(){@Override public void run(){
			for(Player p : plugin.getServer().getOnlinePlayers()){
				if(trueInvis.contains(p.getName()) && p.hasPotionEffect(PotionEffectType.INVISIBILITY) == false){
					trueInvis.remove(p.getName());
				}
			}
			isRunning = false;
			if(!trueInvis.isEmpty()) runInvisDetector();
		}}.runTaskLater(plugin, 5);
	}
	
	/**
	 * Remove the given player(s) from the true-invisibility list.
	 * They will now be able to see ghosts and appear as a ghost
	 * @param player - the player to remove.
	 */
	private void removeTrueInvis(Player... player){
		for(Player p : player){
			trueInvis.remove(p.getName());
			for(Player p2 : p.getServer().getOnlinePlayers()){
				if(viewerTeam.hasEntry(p2.getName())) p2.showPlayer(p);
			}
			viewerTeam.removeEntry(p.getName());
			if(ghosts.remove(p.getName())) p.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
	}
	
	/**
	 * Add a player to the ghost list
	 * @param player - the player to add to the ghost manager.
	 */
	public void addGhost(Player... players){
		if(players.length == 0) return;
		
		boolean wasEmpty = ghosts.isEmpty();
		
		//Add these new players as ghosts
		for(Player p : players){
			if(p.hasPotionEffect(PotionEffectType.INVISIBILITY)
					|| ghosts.contains(p.getName())) continue;//already invis, already ghost, not on ghostTeam
	
			ghosts.add(p.getName());
			ghostTeam.addEntry(p.getName());
			p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, INVIS_LVL));
		}
		
		//Previously empty.  Start up GhostManager
		if(wasEmpty || ghosts.isEmpty() == false){
			trueInvis = new HashSet<String>();
			for(Player p : players[0].getServer().getOnlinePlayers()){
				//Add all players as ghosts by default
				viewerTeam.addEntry(p.getName());
				
				//If invisible, hide them from everyone else so they do not appear as ghosts
				if(p.hasPotionEffect(PotionEffectType.INVISIBILITY)){
					for(PotionEffect effect : p.getActivePotionEffects()){
						if(effect.getType() == PotionEffectType.INVISIBILITY && effect.getAmplifier() != INVIS_LVL){
							removeGhost(p);
							if(!ghosts.isEmpty()) addTrueInvis(p);
						}
					}
				}
			}
			
			//register listeners
			players[0].getServer().getPluginManager().registerEvents(listener = new Listener(){
				@EventHandler public void onPlayerJoin(PlayerJoinEvent evt){
					//Add all players as ghosts by default
					viewerTeam.addEntry(evt.getPlayer().getName());
					
					if(evt.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)){for(PotionEffect effect : evt.getPlayer().getActivePotionEffects()){
							if(effect.getType() == PotionEffectType.INVISIBILITY && effect.getAmplifier() == INVIS_LVL){
								viewerTeam.addEntry(evt.getPlayer().getName());
								return;
							}
						}
						addTrueInvis(evt.getPlayer());
					}
				}
				@EventHandler public void onPlayerDrinkInvis(PlayerItemConsumeEvent evt){
					if(evt.getItem().getType() == Material.POTION){
						for(PotionEffect effect : ((PotionMeta)evt.getItem().getItemMeta()).getCustomEffects()){
							if(effect.getType() == PotionEffectType.INVISIBILITY){
								plugin.getLogger().info("[Ghost Factory] player drank invis potion");
								addTrueInvis(evt.getPlayer());
							}
						}
					}
				}
				@EventHandler public void onPlayerQuit(PlayerQuitEvent evt){
					viewerTeam.removeEntry(evt.getPlayer().getName());
					removeTrueInvis(evt.getPlayer());
				}
			}, plugin);
		}
	}
	
	/**
	 * Remove a player from the ghost list
	 * @param player - the player to remove from the ghost manager.
	 */
	public void removeGhost(Player... player){
		for(Player p : player){
			ghosts.remove(p.getName());
			ghostTeam.removeEntry(p.getName());
			p.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
		if(ghosts.isEmpty()){
			HandlerList.unregisterAll(listener);
		}
	}

	/**
	 * Determine if the given player is tracked by this ghost manager and is a ghost.
	 * @param player - the player to test.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isGhost(Player player) {
		return player != null && viewerTeam.hasEntry(player.getName()) && ghosts.contains(player.getName());
	}

	/**
	 * Retrieve every ghost currently tracked by this ghost factory
	 * @return Every tracked ghost.
	 */
	@SuppressWarnings("deprecation")
	public Set<OfflinePlayer> getGhosts() {
		Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();
		for(String ghostName : ghosts) players.add(Bukkit.getServer().getOfflinePlayer(ghostName));
		return players;
	}

	/**
	 * Retrieve every ghost and every player that can see ghosts.
	 * @return Every ghost and every observer.
	 */
	@SuppressWarnings("deprecation")
	public Set<OfflinePlayer> getGhostViewers() {
		Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();
		for(String ghostName : viewerTeam.getEntries()) players.add(Bukkit.getServer().getOfflinePlayer(ghostName));
		return players;
	}
}