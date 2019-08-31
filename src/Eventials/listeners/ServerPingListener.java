package Eventials.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import Eventials.Eventials;
import net.evmodder.EvLib.extras.TextUtils;

public class ServerPingListener implements Listener{
	private Eventials plugin;
	private int today;
	private String todaysMsg;
	private String customMOTD = "";
	public String getMOTD(){return customMOTD;} public void setMOTD(String newMotd){customMOTD = newMotd;}

	private Map<HolidayDate, String> holidays = new HashMap<HolidayDate, String>();
	private Map<String, Short> ping_idxs = new HashMap<String, Short>();
	private String[] pingMsgs;
	private Set<String> blacklistIPs;
	final String pingPrefix;

	public ServerPingListener(){
		plugin = Eventials.getPlugin();

		pingPrefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ping-prefix"));
		List<String> msgs = plugin.getConfig().getStringList("ping-messages");
		pingMsgs = new String[msgs.size()];
		int i=-1;
		String pingMsgColor = TextUtils.getCurrentColorAndFormat(pingPrefix);
		for(String msg : msgs) pingMsgs[++i] = TextUtils.translateAlternateColorCodes('&', msg, pingMsgColor);

		blacklistIPs = new HashSet<String>();
		blacklistIPs.addAll(plugin.getConfig().getStringList("blacklisted-ips"));

		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getClass().getResourceAsStream("/date_specific_ping_msgs.txt")));

			String line;
			while((line = reader.readLine()) != null){
				line = line.trim();
				if(!line.startsWith("#") && !line.startsWith("//") && line.contains("/") && line.contains("->")){
					String[] data = line.split("->")[0].replace(" ", "").split("/");
					HolidayDate date = new HolidayDate(Integer.parseInt(data[1]), Integer.parseInt(data[0]));
					if(data.length == 3) date.dayOfWeek = getDayOfWeek(data[2]);

					holidays.put(date, ChatColor.translateAlternateColorCodes('&', line.split("->")[1].trim()));
				}
			}
			reader.close();
		}
		catch(IOException ex){ex.printStackTrace();}
	}

	public String getDateSpecificPing(){
		if(holidays.isEmpty()) return null;

		GregorianCalendar date = new GregorianCalendar();
		if(today == date.get(Calendar.DAY_OF_MONTH)) return todaysMsg;
		today = date.get(Calendar.DAY_OF_MONTH);

		int month = date.get(Calendar.MONTH)+1;

		for(HolidayDate holiday : holidays.keySet()){
			if(holiday.dayOfWeek != -1){
				if(holiday.dayOfWeek == date.get(Calendar.DAY_OF_WEEK) && Math.abs(holiday.month - month) <= 1){

//					plugin.getLogger().info("Month: "+month);
//					plugin.getLogger().info("HMonth: "+holiday.month);
//					plugin.getLogger().info("Day: "+today);
//					plugin.getLogger().info("HDay: "+holiday.day);
					long diffInMillis = Math.abs(System.currentTimeMillis() -
						new GregorianCalendar(date.get(Calendar.YEAR), holiday.month-1, holiday.day).getTimeInMillis());
					int diffInDays = (int) (diffInMillis/(1000*60*60*24));
					if(diffInDays <= 3){
						return (todaysMsg = holidays.get(holiday));
					}
//					plugin.getLogger().info("Diff: "+diffInDays);
				}
			}
			else if(month == holiday.month && today == holiday.day) return (todaysMsg = holidays.get(holiday));
		}
		return todaysMsg = null;
	}

	@EventHandler
	public void onServerPing(ServerListPingEvent evt){
		String playerIP = evt.getAddress().getHostAddress();
		if(blacklistIPs.contains(playerIP)){
			evt.setMotd(ChatColor.DARK_RED+"Can't connect to server.");
			return;
		}
		String title = evt.getMotd().split("\n")[0];
		String motd = customMOTD;

		// If no custom MOTD is set, then use the one in server.properties
		if(motd == null || motd.isEmpty()){
			String todaysMotd = getDateSpecificPing();
			if(todaysMotd != null) motd = todaysMotd;
			else{
				short pingI = ping_idxs.containsKey(playerIP) ? ping_idxs.get(playerIP) : 0;

				if(!pingMsgs[pingI].isEmpty()){
					motd = pingMsgs[pingI];

					if(motd.contains("%name%")){
						UUID uuid = PlayerLoginListener.addressMap.get(evt.getAddress().toString());
						if(uuid != null){
							OfflinePlayer offlineP = plugin.getServer().getOfflinePlayer(uuid);
							String name = (offlineP == null ? "dude" : offlineP.getName());
							motd = motd.replace("%name%", name);
						}
						motd = motd.replace("%name%", "dude");
					}
				}
				else{
					/*
					//=============================== TEMPORARY (Event Countdown) ===============================
					long endMilli = 1566802800000L;
					long millisLeft = endMilli - System.currentTimeMillis();
					if(millisLeft < 0) millisLeft = 0;
					long secondsLeft = millisLeft / 1000;
					long minutesLeft = secondsLeft / 60;
					long hoursLeft = minutesLeft / 60;
					secondsLeft %= 60;
					minutesLeft %= 60;
					//hoursLeft %= 24;
					String timeLeft = new StringBuilder("").append(ChatColor.DARK_GRAY).append('[')
							.append(ChatColor.RED).append(hoursLeft).append(ChatColor.GRAY).append("h ")
							.append(ChatColor.RED).append(minutesLeft).append(ChatColor.GRAY).append("m ")
							.append(ChatColor.RED).append(secondsLeft).append(ChatColor.GRAY).append('s')
							.append(ChatColor.DARK_GRAY).append(']').toString();
					motd = "§7Time left until contest ends: "+(millisLeft > 0 ? timeLeft : "§cENDED");//*/
				}
				++pingI; if(pingI == pingMsgs.length) pingI = 0;
				ping_idxs.put(playerIP, pingI);
			}
		}

		if(!motd.isEmpty()){
			motd = new StringBuilder(title).append('\n').append(pingPrefix).append(motd).toString();
			evt.setMotd(motd);
		}
		/*
		//==================================== TEMPORARY (Event Countdown) ====================================
		int scheduledDay = 30;
		int scheduledHour = 18;

		GregorianCalendar date = new GregorianCalendar();
		int hour = scheduledHour - date.get(Calendar.HOUR_OF_DAY);		//Important Times: 5 PM (17 for Military time)
		hour += (scheduledDay - date.get(Calendar.DAY_OF_MONTH)) *24;	//Important Times: 25 [day of month]
		int second = 60 - date.get(Calendar.SECOND);
		int minute = 60 - date.get(Calendar.MINUTE);
		if(minute > 0)hour--;
		if(second > 0)minute--;

		String timeLeft = "§8[§7§c"+hour+"§7h §c"+minute+"§7m §c"+second+"§7s§8]";
		if(hour < 0 || (hour == 0 && (minute < 0 || second < 0))) timeLeft = "§8[§c0§7s§8]";
		String spaces = "                                ";
		// 1 letter length = 2 spaces length
		// the extra '-1' is just for extra space reasons and should generally be removed
		int charsInMsg = ChatColor.stripColor(timeLeft).length();
		if(charsInMsg < 13) for(int i = 0; i < 13-charsInMsg*1.35; ++i) spaces += ' ';

		evt.setMotd(title + spaces + timeLeft +
		// Event Description here, comment it out to use normal MOTD set in server.properties
				"\n §7§m§l-§a Mafia Event when timer hits 0!");
*/		//=====================================================================================================
	}

	public int getDayOfWeek(String day){
		day = day.toLowerCase().replaceAll("[^a-z]", "");
		if(day.isEmpty()) return -1;

		else if("sunday".contains(day)) return 1;
		else if("monday".contains(day)) return 2;
		else if("tuesday".contains(day)) return 3;
		else if("wednesday".contains(day)) return 4;
		else if("thursday".contains(day)) return 5;
		else if("friday".contains(day)) return 6;
		else if("saturday".contains(day)) return 7;
		else return -1;
	}

	public class HolidayDate {
		public int day, month, dayOfWeek=-1;
		public HolidayDate(int day, int month){
			this.day = day; this.month = month;
		}
		public HolidayDate(int day, int month, int dayOfWeek){
			this.day = day; this.month = month; this.dayOfWeek = dayOfWeek;
		}

		@Override
		public boolean equals(Object obj){
			if(obj instanceof HolidayDate){
				HolidayDate date = (HolidayDate) obj;
				return date.day == day && date.month == month &&
						(date.dayOfWeek == dayOfWeek || (dayOfWeek == -1 && date.dayOfWeek == -1));
			}
			return false;
		}

		@Override
		public String toString(){
			return new StringBuilder().append(month).append(',').append(day).append(',').append(dayOfWeek).toString();
		}
	}
}