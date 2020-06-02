package Eventials;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import Eventials.listeners.*;
import Eventials.mailbox.MailboxClient;
import Eventials.books.WriterTools;
import Eventials.bridge.EvBridgeClient;
import Eventials.commands.*;
import Eventials.custombows.CustomBows;
import Eventials.spawners.*;
import Eventials.splitworlds.SplitWorlds;
import Eventials.economy.EvEconomy;
import Eventials.voter.EvVoter;
import _SpecificAndMisc.EventAndMisc;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.FileIO;

/** Everything inside this class is very AltCraft-specific,
 *  and not really meant for (or useful for) public distribution.
 */
//Event Ideas: Randomizing Blocks
//TODO: Must stay in bed to skip night (mimic vanilla?)
//TODO: hover-text for recent-joins shows how long since they were on
//TODO: chat "ping/tick" sound (toggleable) and name mention sound?
//TODO: Eye-of-ender popping
//TODO: splitworlds option to also separate stats/advancements per-world
public class Eventials extends EvPlugin {
	private static Eventials plugin; public static Eventials getPlugin(){return plugin;}
	public PlayerLoginListener loginListener;
//	private Scheduler scheduler;
	public EvBridgeClient bridge;//TODO: getter/setter
	private EvVoter voter;
	private EvEconomy eco;
	private MailboxClient mailbox;

	@Override public void onEvEnable(){
		plugin = this;
		new EventAndMisc(this);//TODO: Temporary?

		String bridge_host = config.getString("bridge-host", "localhost");
		int bridge_port = config.getInt("bridge-port", 42374);
		bridge = new EvBridgeClient(getLogger(), bridge_host, bridge_port);//TODO: make flag-controlled in config

		if(config.getBoolean("book-editor-enabled", true)) new WriterTools(this);
		if(config.getBoolean("enable-custom-bows", true)) new CustomBows(this);
		if(config.getBoolean("economy-enabled", true)) eco = new EvEconomy(this);
		if(config.getBoolean("scheduler-enabled", true)) new Scheduler(this);
		if(config.getBoolean("evspawner-enabled", true)) new EvSpawner(this);
		if(config.getBoolean("evvoter-enabled", true)) voter = new EvVoter(this);
		if(config.getBoolean("splitworlds-enabled", true)) new SplitWorlds(this);
		if(config.getBoolean("mailbox-enabled", true)) mailbox = new MailboxClient(this);

		if(config.getBoolean("pre-command", true))
			getServer().getPluginManager().registerEvents(new PreCommandListener(), this);
		boolean tellD = config.getBoolean("tell-death-coords", true), logD = config.getBoolean("log-death-coords", true);
		if(tellD || logD)
			getServer().getPluginManager().registerEvents(new PlayerDeathListener(tellD, logD), this);
		if(config.getBoolean("custom-ping", true))
			getServer().getPluginManager().registerEvents(new ServerPingListener(), this);
		if(config.getBoolean("disable-skeleton-traps", true))
			getServer().getPluginManager().registerEvents(new TrapSpawnListener(), this);
		if(config.getBoolean("allow-mob-armies", true))
			getServer().getPluginManager().registerEvents(new CreatureSpawnListener(), this);
		if(config.getDouble("skip-night-sleep-percent-required", 1) < 1 
		|| config.getDouble("skip-storm-sleep-percent-required", 1) < 1
		|| config.getDouble("skip-thunder-sleep-percent-required", 1) < 1)
			getServer().getPluginManager().registerEvents(new PlayerSleepListener(), this);
//		if(config.getBoolean("allow-colorcodes-in-commandblock")) 
//			getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
		getServer().getPluginManager().registerEvents(loginListener = new PlayerLoginListener(), this);


		new CommandBreakPhysics(this);
		new CommandClearEntities(this);
		new CommandDiscord(this);
		new CommandEventials(this);
		new CommandFloatingText(this);
		new CommandGhost(this);
		new CommandInsight(this);
		new CommandItemName(this);
		new CommandItemPrefix(this);
		new CommandItemSuffix(this);
		new CommandParticles(this);
		new CommandPig(this);
		new CommandRecentJoins(this);
		new CommandRegionDelete(this);
		new CommandRegionPos(this);
		new CommandRegionTp(this);
		new CommandSetLore(this);
		new CommandSigntool(this, config.getBoolean("enable-signtools", true));
		new CommandStatsClear(this);
		new CommandStatsRestore(this);
		new CommandVipGive(this);
		new CommandVipTake(this);
		new CommandWeaponStats(this, config.getBoolean("enable-weaponstats", true));

		if(config.getBoolean("prevent-multicraft-list-console-spam")){
			getServer().getLogger().setFilter(new Filter(){@Override public boolean isLoggable(LogRecord record){
				return !record.getMessage().contains(" issued server command: ");
			}});
		}
	}

	@Override public void onEvDisable(){
		if(eco != null) eco.onDisable();
		if(loginListener != null) loginListener.onDisable();
		if(voter != null) voter.onDisable();
		if(mailbox != null) mailbox.onDisable();
//		saveConfig();
		if(config.getBoolean("scheduler-enabled", true)){
			int currentCycle = config.getInt("current-cylce", 0);
			if(currentCycle == 0) FileIO.deleteFile("scheduler-data.txt");
			else FileIO.saveFile("scheduler-data.txt", "current-cylce: "+currentCycle);
		}
	}

	public void saveData(){
		if(eco != null) eco.onDisable();
		if(loginListener != null) loginListener.onDisable();
		if(voter != null) voter.onDisable();
//		saveConfig();
	}

	public void runCommand(String command){
		getServer().dispatchCommand(getServer().getConsoleSender(), command);
	}
	// Doesn't work without "spigot.yml >> commands.log: false" :(
	/*public void runCommandSilently(String command){
		MessageInterceptor pmi = new MessageInterceptor(getServer().getConsoleSender(), true);
		getServer().dispatchCommand(pmi.getProxy(), command);
	}*/
}