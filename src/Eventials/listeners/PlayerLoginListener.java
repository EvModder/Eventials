package Eventials.listeners;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;
import Eventials.economy.Economy;
import net.evmodder.EvLib.hooks.EssEcoHook;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.extras.TextUtils;

public class PlayerLoginListener implements Listener{
	private Eventials plugin;
	private Economy eco;
	public static HashMap<String, UUID> addressMap;
	private LinkedList<String> recentJoins;
	final boolean playNote, showRecentJoins, saveIps, serverFundsNoobs, trackGlobalBal, announceDailyMoney;
	final int dailyMoney;
	final double startingBal;
	final String curSymbol;

	public PlayerLoginListener(){
		plugin = Eventials.getPlugin();
		boolean ecoEnabled = plugin.getConfig().getBoolean("economy-enabled", true);
		eco = Economy.getEconomy();
		playNote = plugin.getConfig().getBoolean("login-noteblock");
		showRecentJoins = plugin.getConfig().getBoolean("show-recent-joins", true);
		saveIps = plugin.getConfig().getBoolean("save-ips", true);
		curSymbol = TextUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("currency-symbol", "&2L"));
		trackGlobalBal = plugin.getConfig().getBoolean("track-global-balance", true);
		announceDailyMoney = plugin.getConfig().getBoolean("online-when-daily-money-bonus", true);
		if(ecoEnabled){
			dailyMoney = plugin.getConfig().getInt("login-daily-money");
			startingBal = plugin.getConfig().getInt("starting-balance");
			serverFundsNoobs = plugin.getConfig().getBoolean("server-pays-starting-balance", true);
		}
		else{
			dailyMoney = 0;
			startingBal = 0;
			serverFundsNoobs = false;
		}
		if(showRecentJoins){
			String joinsFile = FileIO.loadFile("recent-joins.txt", "");
			if(!joinsFile.isEmpty()){
				recentJoins = TextUtils.toListFromString(joinsFile);
				recentJoins.remove("");
				int maxLength = plugin.getConfig().getInt("max-recent-joins", 20);
				while(recentJoins.size() > maxLength) recentJoins.removeFirst();
			}
			else recentJoins = new LinkedList<String>();
		}
		if(saveIps){
			addressMap = new HashMap<String, UUID>();
			String addressFile = FileIO.loadFile("player-addresses.txt", "");
			if(!addressFile.isEmpty()){
				int maxLength = plugin.getConfig().getInt("max-saved-ips", 200);
				String[] entries = addressFile.substring(1, addressFile.lastIndexOf('}')).split(", ");
				if(entries.length < maxLength)
					for(String pair : entries)
						if(pair.contains("="))
							addressMap.put(pair.split("=")[0], UUID.fromString(pair.split("=")[1]));
			}
		}
	}

	public void onDisable(){
		if(showRecentJoins) FileIO.saveFile("recent-joins.txt", recentJoins.toString());
		if(saveIps) FileIO.saveFile("player-addresses.txt", addressMap.toString());
	}

	public List<String> getRecentJoins(int num){//last element is oldest
		List<String> joins = new LinkedList<String>();
		Iterator<String> iterator = recentJoins.descendingIterator();
		while(joins.size() < num && iterator.hasNext()) joins.add(iterator.next());
		return joins;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent login){
		final UUID uuid = login.getPlayer().getUniqueId();
		String name = login.getPlayer().getName();
		OfflinePlayer offP = plugin.getServer().getOfflinePlayer(uuid);

		//--- Messenger -----------------------------------------------
		if(saveIps) addressMap.put(login.getAddress().toString(), uuid);

		//--- Misc ----------------------------------------------------
		if(login.getPlayer().hasPermission("eventials.sneakjoin")){
			new BukkitRunnable(){@Override public void run(){
				Player hider = plugin.getServer().getPlayer(uuid);
				if(hider == null) return;
				else if(hider.isSneaking())
					hider.sendMessage(ChatColor.AQUA+"Login message cancelled");
				else{
					for(Player p : plugin.getServer().getOnlinePlayers()){
						if(!p.getUniqueId().equals(uuid)){
							p.sendMessage(ChatColor.GOLD + hider.getName() + " joined the game");
							if(playNote) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 50.0F, 0.75F);
						}
					}
				}
			}}.runTaskLater(plugin, 100);//5 seconds
		}
		else if(playNote) for(Player p : plugin.getServer().getOnlinePlayers())
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 50.0F, 0.75F);

		if(showRecentJoins && offP.hasPlayedBefore()){
			if(recentJoins.isEmpty()) recentJoins.add(name);
			else if(!recentJoins.peekLast().equals(name)){
				Iterator<String> iterator = recentJoins.descendingIterator();
				StringBuilder builder = new StringBuilder("")
						.append(ChatColor.BLUE).append("Players since last join: ")
						.append(ChatColor.GRAY).append(iterator.next());
	
				String pName = null;
				while(iterator.hasNext() && !name.equals(pName=iterator.next())){
					builder.append(ChatColor.BLUE).append(", ").append(ChatColor.GRAY).append(pName);
				}
				builder.append(ChatColor.BLUE);
				if(name.equals(pName)){
					iterator.remove();
					builder.append('.');
				}
				else builder.append(", ").append(ChatColor.GRAY).append("...");
	
				final String message = builder.toString();
				new BukkitRunnable(){@Override public void run(){
					Player player = plugin.getServer().getPlayer(uuid);
					if(player != null) player.sendMessage(message);
				}}.runTaskLater(plugin, 5); //5 ticks
				recentJoins.add(name);
			}
		}

		//--- Economy -------------------------------------------------
		if(offP.hasPlayedBefore()){
			if(dailyMoney != 0){
				long lastLogin = offP.getLastPlayed() / 86400000;
				long lastMidnight = new GregorianCalendar().getTimeInMillis() / 86400000;

				if(lastLogin < lastMidnight && eco.serverToPlayer(uuid, dailyMoney)){
					//tell them about giving money
					new BukkitRunnable(){@Override public void run(){
						OfflinePlayer p = plugin.getServer().getPlayer(uuid);
						if(announceDailyMoney) plugin.getServer().broadcastMessage(ChatColor.DARK_AQUA + 
								(p.isOnline() ? p.getPlayer().getDisplayName() : p.getName())
								+ ChatColor.GREEN + " received " + ChatColor.YELLOW + dailyMoney+curSymbol
								+ ChatColor.GREEN + " for logging on today!");
					}}.runTaskLater(plugin, 10);//.5s
				}
			}
		}
		else if(startingBal > 0){//new player
			plugin.getLogger().info("Giving starting cash: "+startingBal);
			if(serverFundsNoobs) eco.serverToPlayer(uuid, startingBal);
			else if(EssEcoHook.giveMoney(offP, startingBal) && trackGlobalBal){
				eco.addGlobalBal(startingBal);
			}
		}
	}
}