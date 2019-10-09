package Eventials;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.economy.EvEconomy;
import Eventials.economy.commands.CommandAdvertise;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.extras.ButcherUtils;
import net.evmodder.EvLib.extras.ButcherUtils.KillFlag;
import com.earth2me.essentials.IEssentials;

public final class Scheduler{
	private Eventials plugin;
	private int cycleCount, automsg_index;
	private boolean magicDay, serverIsSilent, playerSinceButcher, playerSinceSave;
	private HolidayListener currentHoliday;
	final String[] autoMsgs;
	final String msgC, msgP, escapedMsgP;
	final int period, cAutomsg, cWorldsave, cDelete, cButcher, cMagic, cHoliday, cEventialsSave;
	final long MILLIS_PER_TICK = 50, MILLIS_PER_DAY = 1000L*60*60*24;
	final boolean cSkipAutomsg, skipAutomsgIfSilent, AUTOBUMP_PMC;
	final String PMC_LOGIN_TOKEN, PMC_RESOURCE_ID;
	final HashMap<KillFlag, Boolean> butcherFlags;
	private static Scheduler sch; public static Scheduler getScheduler(){return sch;}

	public Scheduler(Eventials pl){
		sch = this;
		plugin = pl;

		List<String> list = plugin.getConfig().getStringList("auto-messages");
		autoMsgs = list.toArray(new String[list.size()]);
		msgC = TextUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("message-color", "&e"));
		msgP = TextUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("message-prefix", "&e"));
		escapedMsgP = TextUtils.escapeTextActionCodes(msgP);
		for(int i = 0; i < autoMsgs.length; ++i){
			autoMsgs[i] = TextUtils.translateAlternateColorCodes('&', autoMsgs[i].replaceAll("&r", msgC)
					.replaceAll("\\\\n", "\\n"));
		}
		period = plugin.getConfig().getInt("clock-period", 60)*20;
		cycleCount = FileIO.loadYaml("scheduler-data.txt", "current-cycle: 0").getInt("current-cycle");

		cAutomsg = plugin.getConfig().getInt("cycles-per-automessage", 3);
		cButcher = plugin.getConfig().getInt("cycles-per-mob-clear", 5);
		cWorldsave = plugin.getConfig().getInt("cycles-per-worldsave", 5);
		cDelete = plugin.getConfig().getInt("cycles-per-player-delete", 2880);
		cMagic = plugin.getConfig().getInt("cycles-per-magic-day-reward", 60);
		cHoliday = plugin.getConfig().getInt("cycles-per-holiday-action", 10);
		cEventialsSave = plugin.getConfig().getInt("cycles-per-eventials-data-save", 2);
		cSkipAutomsg = plugin.getConfig().getBoolean("skip-automessage-if-other-event", true);
		skipAutomsgIfSilent = plugin.getConfig().getBoolean("skip-automessage-if-no-chats", true);

		PMC_RESOURCE_ID = ""+plugin.getConfig().getInt("pmc-resource-id", 0);
		PMC_LOGIN_TOKEN = plugin.getConfig().getString("pmc-login-token", "");
		AUTOBUMP_PMC = plugin.getConfig().getBoolean("pmc-auto-bump", false)
					&& !PMC_LOGIN_TOKEN.isEmpty() && !PMC_RESOURCE_ID.equals("0");

		//TODO: load from config file
		butcherFlags = new HashMap<KillFlag, Boolean>();
		butcherFlags.put(KillFlag.ANIMALS, false);
		butcherFlags.put(KillFlag.EQUIPPED, false);
		butcherFlags.put(KillFlag.NAMED, false);
		butcherFlags.put(KillFlag.NEARBY, false);
		butcherFlags.put(KillFlag.TILE, false);
		butcherFlags.put(KillFlag.UNIQUE, false);

		new BukkitRunnable(){@Override public void run(){
			runCycle();
		}}.runTaskTimer(plugin, period, period);
	}

	private void runCycle(){
		//check if new day
		long now = new GregorianCalendar().getTimeInMillis();
		long last = now - period*MILLIS_PER_TICK;
		if((last/MILLIS_PER_DAY) < (now/MILLIS_PER_DAY)){
			// Get date
			GregorianCalendar date = new GregorianCalendar();
			int day = date.get(Calendar.DAY_OF_MONTH), month = date.get(Calendar.MONTH)+1, year = date.get(Calendar.YEAR);

			// Broadcast new day
			plugin.getServer().broadcastMessage(""+ChatColor.DARK_GREEN+ChatColor.BOLD+ChatColor.ITALIC+
					"Another day has passed on Alternatecraft!");
			plugin.getLogger().info("Day: "+day+", Month: "+month+", Year: "+year+", Since epoch: "+(now / 86400000));

			// Expire old ads
			long DURATION = plugin.getConfig().getLong("ad-duration")*86400000L;
			long EXPIRES = plugin.getConfig().getLong("ad-expires-on");
			if(EXPIRES != 0 && now > EXPIRES + DURATION){//If 2x past the purchase date
				CommandAdvertise.setAdvertisement(plugin.getConfig().getString("ad-default"));
				plugin.getConfig().set("ad-expires-on", 0);
			}

			// Pay daily money
			long ticksSinceNewDay = (now % MILLIS_PER_DAY)/MILLIS_PER_TICK;
			List<Player> sendTo = new ArrayList<Player>();
			for(Player p : plugin.getServer().getOnlinePlayers())
				if(p.getStatistic(Statistic.PLAY_ONE_MINUTE)*60*20 > ticksSinceNewDay) sendTo.add(p);
			if(!sendTo.isEmpty()) payDailyMoney(sendTo);

			// Trigger magic day 
			magicDay = day*month == year%100;

			// Bump server on PlanetMinecraft
			if(AUTOBUMP_PMC) bumpOnPMC(PMC_LOGIN_TOKEN, PMC_RESOURCE_ID);

			// Reset current cycle
			plugin.getConfig().set("current-cycle", cycleCount=1);
		}
		else plugin.getConfig().set("current-cycle", ++cycleCount);

		boolean event = false;
		if(cDelete != 0 && cycleCount % cDelete == 0){
			event = true;
			Extras.runPlayerDelete();
		}
		if(cButcher != 0 && cycleCount % cButcher == 0 && playerSinceButcher){
			event = true;
			int numKilled = ButcherUtils.clearEntitiesByWorld(null, butcherFlags);
			plugin.getLogger().info("Butchered "+numKilled+" mobs");

			playerSinceButcher = !plugin.getServer().getOnlinePlayers().isEmpty();
			if(!playerSinceButcher) plugin.getServer().getPluginManager().registerEvents(new Listener(){
				@EventHandler public void onJoin(PlayerJoinEvent evt){
					HandlerList.unregisterAll(this);
					playerSinceButcher = true;
				}
			}, plugin);
		}
		if(cWorldsave != 0 && cycleCount % cWorldsave == 0 && playerSinceSave){
			event = true;
			plugin.getServer().broadcastMessage(ChatColor.RED+"Saving server files...");
			plugin.runCommand("save-all");
			plugin.getServer().broadcastMessage(ChatColor.GREEN+"Save Complete!");
			playerSinceSave = !plugin.getServer().getOnlinePlayers().isEmpty();
			if(!playerSinceSave) plugin.getServer().getPluginManager().registerEvents(new Listener(){
				@EventHandler public void onJoin(PlayerJoinEvent evt){
					HandlerList.unregisterAll(this);
					playerSinceSave = true;
				}
			}, plugin);
		}
		if(magicDay && cMagic != 0 && cycleCount % cMagic == 0){
			event = true;
			Random rand = new Random();

			for(Player p : plugin.getServer().getOnlinePlayers()){
				ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
				EnchantmentStorageMeta bookmeta = (EnchantmentStorageMeta)book.getItemMeta();
				Enchantment enchant = Enchantment.values()[Enchantment.values().length];
				bookmeta.addStoredEnchant(enchant, rand.nextInt(enchant.getMaxLevel())+1, false);
				p.getInventory().addItem(book);
			}
		}
		if(cHoliday != 0 && cycleCount % cHoliday == 0){
			if(currentHoliday != null) currentHoliday.RunHolidayEvent();
		}
		if(cAutomsg != 0 && cycleCount % cAutomsg == 0 && !(cSkipAutomsg && event)
				&& autoMsgs.length != 0 && !serverIsSilent){
			IEssentials ess = (IEssentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
			List<Player> sendTo = new ArrayList<Player>();

			for(Player p : plugin.getServer().getOnlinePlayers()) if(!ess.getUser(p).isAfk()) sendTo.add(p);
			if(!sendTo.isEmpty()){
				sendAutomessage(sendTo.toArray(new Player[sendTo.size()]));
				if(skipAutomsgIfSilent){
					serverIsSilent = true;

					plugin.getServer().getPluginManager().registerEvents(new Listener(){
						@EventHandler public void onChat(AsyncPlayerChatEvent evt){
							HandlerList.unregisterAll(this);
							serverIsSilent = false;
						}
					}, plugin);
				}
			}
		}
		if(cEventialsSave != 0 && cycleCount % cEventialsSave == 0){
			plugin.saveData();
			//plugin.onEvDisable();//Disabling involves writing to file :)
		}
	}

	public void sendAutomessage(Player... ppl){
		for(String line : autoMsgs[automsg_index].split("\n")){
			sendHyperMessage(line, ppl);
		}
		if(++automsg_index == autoMsgs.length) automsg_index = 0;
	}

	public void sendHyperMessage(String msg, Player... ppl){
		if(TextUtils.TextAction.countNodes(msg) == 0){
			plugin.getServer().broadcastMessage(msgP+msg);
		}
		else{
			plugin.getServer().getConsoleSender().sendMessage(msgP+msg);

			String raw = TextUtils.TextAction.parseToRaw(escapedMsgP+msg, msgC);

			for(Player p : ppl){
//				p.sendRawMessage(raw);//Doesn't work! (last checked: 1.12.1)
				plugin.runCommand("minecraft:tellraw "+p.getName()+' '+raw);//p.sendRawMessage(raw);
			}
		}
	}

	public void payDailyMoney(Collection<Player> ppl){
		double dailyMoney = plugin.getConfig().getBoolean("economy-enabled") ?
					(plugin.getConfig().getDouble("login-daily-money", 0) +
					plugin.getConfig().getDouble("online-when-daily-money-bonus", 0)) : 0;
		if(dailyMoney == 0) return;
		String curSymbol = TextUtils.translateAlternateColorCodes('&',
				plugin.getConfig().getString("currency-symbol", "&2L"));
		boolean announce = plugin.getConfig().getBoolean("announce-daily-money");
		for(Player p : ppl){
			if(EvEconomy.getEconomy().serverToPlayer(p.getUniqueId(), dailyMoney) && announce){
				plugin.getServer().broadcastMessage(ChatColor.DARK_AQUA + 
					p.getPlayer().getDisplayName() + ChatColor.GREEN + " received " + ChatColor.YELLOW +
					dailyMoney + curSymbol + ChatColor.GREEN + " for logging on today!");
			}
		}
	}

	public static void bumpOnPMC(String autoLoginToken, String rssId){
		try{
			String url = 
					"https://www.planetminecraft.com/ajax.php" +
					"?module=public%2Fresource%2Fmanage" +//TODO: try with '/' instead of '%2'
					"&module_plugin=log" +
					"&module_plugin_task=bump" +
					"&resource_id="+rssId;
			HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
			conn.setRequestMethod("GET");

			conn.setRequestProperty("Host", "www.planetminecraft.com");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:70.0) Gecko/20100101 Firefox/70.0");
			conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Referer", "https://www.planetminecraft.com/account/manage/servers/"+rssId+"/");
			conn.setRequestProperty("Cookie",
					//"PHPSESSID=92663eeb9ee2bbb870fcbfe4058bd0f8; "
					//+ "_ga=GA1.2.1507515249.1523593069; "
					//+ "__cfduid=d6b4adf4ac887cf0224df10b6a20292581555195284; "
					//+ "__gads=ID=5bd0fad04673ae98:T=1556962714:S=ALNI_MZaTWiCcK2ZEtE3EGqI_f2NhJKt_g; "
					//+ "__qca=P0-1125965822-1557462131204; OX_plg=swf|shk|pm; "
					//+ "_gid=GA1.2.229742935.1569489269; "
				"pmc_autologin="+autoLoginToken+"; "
					//+ "_gat=1"
			);
			conn.setRequestProperty("TE", "Trailers");
			int responseCode = conn.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
		}catch(IOException e){e.printStackTrace();}
	}
	public static void main(String... args){
		String autoLoginToken =
				"a61f2bd3aa2d3ecdc0f3e26e51d0ca406b17e998712949380a35e1d44de5239588cbacc64b185c48126" +
				"bf9800ab4c4ea16016c13a8fc380de18621abb64a2fd95e43c884847641db247273f5eb1ccb0ed63a77";
		bumpOnPMC(autoLoginToken, "4368271");
	}
}