package Eventials.scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
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
import Eventials.Eventials;
import Eventials.economy.Economy;
import Eventials.economy.commands.CommandAdvertise;
import Extras.Extras;
import Extras.Text;
import net.evmodder.EvLib.FileIO;
import com.earth2me.essentials.IEssentials;

public final class Scheduler{
	private Eventials plugin;
	private int cycleCount, automsg_index;
	private boolean magicDay, serverIsSilent, playerSinceButcher, playerSinceSave;
	final String[] autoMsgs;
	final String msgC, msgP, escapedMsgP;
	final int period, cAutomsg, cWorldsave, cDelete, cButcher, cMagic, cEventialsSave;
	final boolean cSkipAutomsg, skipAutomsgIfSilent;

	private static Scheduler sch; public static Scheduler getScheduler(){return sch;}

	public Scheduler(Eventials pl){
		sch = this;
		plugin = pl;

		List<String> list = plugin.getConfig().getStringList("auto-messages");
		autoMsgs = list.toArray(new String[list.size()]);
		msgC = Text.translateAlternateColorCodes('&', plugin.getConfig().getString("message-color", "&e"));
		msgP = Text.translateAlternateColorCodes('&', plugin.getConfig().getString("message-prefix", "&e"));
		escapedMsgP = Text.escapeTextActionCodes(msgP);
		for(int i = 0; i < autoMsgs.length; ++i){
			autoMsgs[i] = Text.translateAlternateColorCodes('&', autoMsgs[i].replaceAll("&r", msgC)
					.replaceAll("\\\\n", "\\n"));
		}
		period = plugin.getConfig().getInt("clock-period", 60)*20;
		cycleCount = FileIO.loadYaml("scheduler-data.txt", "current-cycle: 0").getInt("current-cycle");

		cAutomsg = plugin.getConfig().getInt("cycles-per-automessage", 3);
		cButcher = plugin.getConfig().getInt("cycles-per-mob-clear", 5);
		cWorldsave = plugin.getConfig().getInt("cycles-per-worldsave", 5);
		cDelete = plugin.getConfig().getInt("cycles-per-player-delete", 2880);
		cMagic = plugin.getConfig().getInt("cycles-per-magic-day-reward", 60);
		cEventialsSave = plugin.getConfig().getInt("cycles-per-eventials-data-save", 2);
		cSkipAutomsg = plugin.getConfig().getBoolean("skip-automessage-if-other-event", true);
		skipAutomsgIfSilent = plugin.getConfig().getBoolean("skip-automessage-if-no-chats", true);

		new BukkitRunnable(){@Override public void run(){
			runCycle();
		}}.runTaskTimer(plugin, period, period);
	}

	private void runCycle(){
		//check if new day
		long now = new GregorianCalendar().getTimeInMillis();
		long last = now - period*50;//time since last cylce in millis, *50 to convert from ticks to millis
		if((int)(last / 86400000) < (int)(now / 86400000)){//if new day 1000*60*60*24
			plugin.getServer().broadcastMessage(""+ChatColor.DARK_GREEN+ChatColor.BOLD+ChatColor.ITALIC+
					"Another day has passed on Alternatecraft!");

			long DURATION = plugin.getConfig().getLong("ad-duration")*86400000L;
			long EXPIRES = plugin.getConfig().getLong("ad-expires-on");
			if(EXPIRES != 0 && now > EXPIRES + DURATION){//If 2x past the purchase date
				CommandAdvertise.setAdvertisement(plugin.getConfig().getString("ad-default"));
				plugin.getConfig().set("ad-expires-on", 0);
			}

			//------ Pay daily money ------------------------------
			long ticksSinceNewDay = (now % 86400000)/50;
			List<Player> sendTo = new ArrayList<Player>();
			for(Player p : plugin.getServer().getOnlinePlayers())
				if(p.getStatistic(Statistic.PLAY_ONE_MINUTE)*60*20 > ticksSinceNewDay) sendTo.add(p);
			
			if(!sendTo.isEmpty()) payDailyMoney(sendTo.toArray(new Player[sendTo.size()]));
			//-----------------------------------------------------

			GregorianCalendar date = new GregorianCalendar();
			int day = date.get(Calendar.DAY_OF_MONTH), month = date.get(Calendar.MONTH)+1, year = date.get(Calendar.YEAR);
			plugin.getLogger().info("Day: "+day+", Month: "+month+", Year: "+year+", Since epoch: "+(now / 86400000));
//			plugin.getLogger().info("Current Millis>Day: "+(int)(now / 86400000)+
			//						", Prev. Millis>Day: "+(int)(last/ 86400000));

			magicDay = day*month == year%100;
			int cyclesPerDay = 1728000/period;
			if(cycleCount > Integer.MAX_VALUE-cyclesPerDay)
				plugin.getConfig().set("current-cycle", cycleCount=-1);
		}
		plugin.getConfig().set("current-cycle", ++cycleCount);

		boolean event = false;
		if(cDelete != 0 && cycleCount % cDelete == 0){
			event = true;
			Extras.runPlayerDelete();
		}
		if(cButcher != 0 && cycleCount % cButcher == 0 && playerSinceButcher){
			event = true;
			for(World world : plugin.getServer().getWorlds()){
				Extras.clearEntitiesByWorld(world, true, false, false, false, false, true);
			}
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
			plugin.onDisable();//Disabling involves writing to file :)
		}
	}

	public void sendAutomessage(Player... ppl){
		for(String line : autoMsgs[automsg_index].split("\n")){
			sendHyperMessage(line, ppl);
		}
		if(++automsg_index == autoMsgs.length) automsg_index = 0;
	}

	public void sendHyperMessage(String msg, Player... ppl){
		if(Text.TextAction.countNodes(msg) == 0){
			plugin.getServer().broadcastMessage(msgP+msg);
		}
		else{
			plugin.getServer().getConsoleSender().sendMessage(msgP+msg);

			String raw = Text.TextAction.parseToRaw(escapedMsgP+msg, msgC);

			for(Player p : ppl){
//				p.sendRawMessage(raw);//Doesn't work! (last checked: 1.12.1)
				plugin.runCommand("minecraft:tellraw "+p.getName()+' '+raw);//p.sendRawMessage(raw);
			}
		}
	}

	public void payDailyMoney(Player... ppl){
		double dailyMoney = plugin.getConfig().getBoolean("economy-enabled") ?
					(plugin.getConfig().getDouble("login-daily-money", 0) +
					plugin.getConfig().getDouble("online-when-daily-money-bonus", 0)) : 0;
		if(dailyMoney == 0) return;
		String curSymbol = Text.translateAlternateColorCodes('&',
				plugin.getConfig().getString("currency-symbol", "&2L"));
		boolean announce = plugin.getConfig().getBoolean("announce-daily-money");
		for(Player p : ppl){
			if(Economy.getEconomy().serverToPlayer(p.getUniqueId(), dailyMoney) && announce){
				plugin.getServer().broadcastMessage(ChatColor.DARK_AQUA + 
					p.getPlayer().getDisplayName() + ChatColor.GREEN + " received " + ChatColor.YELLOW +
					dailyMoney + curSymbol + ChatColor.GREEN + " for logging on today!");
			}
		}
	}
}