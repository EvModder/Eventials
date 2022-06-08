package Eventials.listeners;

import java.util.Arrays;
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
import Eventials.economy.EvEconomy;
import net.evmodder.EvLib.hooks.EssEcoHook;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.extras.TellrawUtils.HoverEvent;
import net.evmodder.EvLib.extras.TellrawUtils.ListComponent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.extras.TellrawUtils.TextHoverAction;

public class PlayerLoginListener implements Listener{
	private Eventials plugin;
	private EvEconomy eco;
	public static HashMap<String, UUID> addressMap;
	private LinkedList<String> recentJoins;
	final boolean playNote, showRecentJoins, saveIps, serverFundsNoobs, trackGlobalBal, announceDailyMoney;
	final int DAILY_LOGIN_MONEY, MAX_RECENT_JOINS_SHOWN;
	final double startingBal;
	final String curSymbol;

	public PlayerLoginListener(){
		plugin = Eventials.getPlugin();
		boolean ecoEnabled = plugin.getConfig().getBoolean("economy-enabled", true);
		eco = EvEconomy.getEconomy();
		playNote = plugin.getConfig().getBoolean("login-noteblock");
		showRecentJoins = plugin.getConfig().getBoolean("login-show-recent-joins", true);
		MAX_RECENT_JOINS_SHOWN = plugin.getConfig().getInt("max-recent-joins-shown", 25);
		saveIps = plugin.getConfig().getBoolean("save-ips", true);
		curSymbol = TextUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("currency-symbol", "&2L"));
		trackGlobalBal = plugin.getConfig().getBoolean("track-global-balance", true);
		announceDailyMoney = plugin.getConfig().getBoolean("online-when-daily-money-bonus", true);
		if(ecoEnabled){
			DAILY_LOGIN_MONEY = plugin.getConfig().getInt("login-daily-money");
			startingBal = plugin.getConfig().getInt("starting-balance");
			serverFundsNoobs = plugin.getConfig().getBoolean("server-pays-starting-balance", true);
		}
		else{
			DAILY_LOGIN_MONEY = 0;
			startingBal = 0;
			serverFundsNoobs = false;
		}
		if(showRecentJoins){
			String joinsFile = FileIO.loadFile("recent-joins.txt", "");
			if(!joinsFile.isEmpty()){
				recentJoins = new LinkedList<>();
				recentJoins.addAll(Arrays.asList(joinsFile.substring(1, joinsFile.lastIndexOf(']')).split(", ")));
				recentJoins.remove("");
				int maxLength = plugin.getConfig().getInt("max-recent-joins-stored", 50);
				while(recentJoins.size() > maxLength) recentJoins.removeFirst();
			}
			else recentJoins = new LinkedList<>();
		}
		if(saveIps){
			addressMap = new HashMap<>();
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
		if(!showRecentJoins) return Arrays.asList("showRecentJoins=false");
		List<String> joins = new LinkedList<>();
		Iterator<String> iterator = recentJoins.descendingIterator();
		while(joins.size() < num && iterator.hasNext()) joins.add(iterator.next());
		return joins;
	}

	public String getTimeOffline(String name){
		@SuppressWarnings("deprecation")
		OfflinePlayer p = plugin.getServer().getOfflinePlayer(name);
		if(p == null || !p.hasPlayedBefore()) return "unknown";
		long timeSinceLastJoin = System.currentTimeMillis() - p.getLastPlayed();
		return TextUtils.formatTime(timeSinceLastJoin, /*show0s=*/false, /*timeColor=*/ChatColor.WHITE, /*unitColor=*/ChatColor.GRAY, /*sigUnits=*/2);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent login){
		final UUID uuid = login.getPlayer().getUniqueId();
		final String name = login.getPlayer().getName();
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

		if(showRecentJoins){
			if(recentJoins.isEmpty() || !offP.hasPlayedBefore()){
				if(!recentJoins.contains(name)) recentJoins.add(name);
			}
			else if(!recentJoins.peekLast().equals(name)){
				Iterator<String> iterator = recentJoins.descendingIterator();
				String pName = iterator.next();

				if(showRecentJoins){
					ListComponent listComp = new ListComponent(new RawTextComponent(new StringBuilder()
							.append(ChatColor.BLUE).append("Players since last join: ").toString()));
					listComp.addComponent(new RawTextComponent(ChatColor.GRAY+pName, new TextHoverAction(HoverEvent.SHOW_TEXT, getTimeOffline(pName))));
		
					int numShown = 1;
					while(iterator.hasNext() && !name.equals(pName=iterator.next())){
						if(numShown == MAX_RECENT_JOINS_SHOWN) break;
						listComp.addComponent(new RawTextComponent(ChatColor.BLUE+", "));
						listComp.addComponent(new RawTextComponent(ChatColor.GRAY+pName, new TextHoverAction(HoverEvent.SHOW_TEXT, getTimeOffline(pName))));
						++numShown;
					}
					if(name.equals(pName)){
						iterator.remove();
						listComp.addComponent(new RawTextComponent(ChatColor.BLUE+"."));
					}
					else listComp.addComponent(new RawTextComponent(ChatColor.BLUE+", "+ChatColor.GRAY+"..."));
		
					final String message = listComp.toString();
					new BukkitRunnable(){@Override public void run(){plugin.sendTellraw(name, message);}}.runTaskLater(plugin, 5); //5 ticks
				}
				// Remove lingering duplicates
				while(iterator.hasNext()){
					pName = iterator.next();
					if(name.equals(pName)) iterator.remove();
				}
				// Add to very end
				recentJoins.addLast(name);
			}
		}

		//--- Economy -------------------------------------------------
		if(offP.hasPlayedBefore()){
			if(DAILY_LOGIN_MONEY != 0){
				long lastLogin = offP.getLastPlayed() / 86400000;
				long lastMidnight = new GregorianCalendar().getTimeInMillis() / 86400000;

				if(lastLogin < lastMidnight && eco.serverToPlayer(uuid, DAILY_LOGIN_MONEY)){
					//tell them about giving money
					new BukkitRunnable(){@Override public void run(){
						OfflinePlayer p = plugin.getServer().getPlayer(uuid);
						if(announceDailyMoney) plugin.getServer().broadcastMessage(ChatColor.DARK_AQUA + 
								(p.isOnline() ? p.getPlayer().getDisplayName() : p.getName())
								+ ChatColor.GREEN + " received " + ChatColor.YELLOW + DAILY_LOGIN_MONEY+curSymbol
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