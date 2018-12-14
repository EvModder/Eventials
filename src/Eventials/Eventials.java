package Eventials;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import EvLib.EvPlugin;
import EvLib.FileIO;
import EvLib.VaultHook;
import EventAndMisc.EventAndMisc;
import Eventials.listeners.*;
import Eventials.commands.*;
import Eventials.custombows.CustomBows;
import Eventials.spawners.*;
import Eventials.economy.Economy;
import Eventials.scheduler.Scheduler;
import Eventials.voter.EvVoter;
import Extras.Extras;

/** Everything inside this class is very AltCraft-specific,
 *  and not really meant for (or useful for) public distribution.
 */
//Event Ideas: Randomizing Blocks
public class Eventials extends EvPlugin {
	private static Eventials plugin; public static Eventials getPlugin(){return plugin;}
	public PlayerLoginListener loginListener;
//	private Scheduler scheduler;
	private EvVoter voter;
	private Economy eco;

	@Override public void onEvEnable(){
		plugin = this;
		new VaultHook(this);
		if(config.getBoolean("enable-custom-bows", true)) new CustomBows();
		if(config.getBoolean("economy-enabled", true)) eco = new Economy(this);
		if(config.getBoolean("scheduler-enabled", true)) new Scheduler();
		if(config.getBoolean("evspawner-enabled", true)) new EvSpawner();
		if(config.getBoolean("evvoter-enabled", true)) voter = new EvVoter();
		if(config.getBoolean("fancy-help", true)) Extras.loadFancyHelp(this);

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
//		if(config.getBoolean("allow-colorcodes-in-commandblock")) 
//			getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
		getServer().getPluginManager().registerEvents(loginListener = new PlayerLoginListener(), this);


		new CommandBreakPhysics(this);
		new CommandClearEntities(this);
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

		new EventAndMisc();//TODO: Temporary?

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
//		saveConfig();
		if(config.getBoolean("scheduler-enabled", true))
			FileIO.saveFile("scheduler-data.txt", "current-cylce: "+config.getInt("current-cylce", 0));
	}

	public void runCommand(String command){
		getServer().dispatchCommand(getServer().getConsoleSender(), command);
	}
}