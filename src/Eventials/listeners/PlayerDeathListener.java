package Eventials.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import Eventials.Eventials;
import Evil_Code_EvKits.EvKits;
import net.evmodder.EvLib.TextUtils;

public class PlayerDeathListener implements Listener{
	private final Eventials plugin;
	private final boolean tellPlayer, logConsole;

	public PlayerDeathListener(boolean tell, boolean log){
		plugin = Eventials.getPlugin();
		tellPlayer = tell;
		logConsole = log;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent evt){
		//If not in an arena, tell them their death coords
		Location loc = evt.getEntity().getLocation();

		EvKits evKitPVP = (EvKits) plugin.getServer().getPluginManager().getPlugin("EvKitPvP");
		if(evKitPVP == null || !evKitPVP.isEnabled() || evKitPVP.isInArena(loc, false) == null)
		{
			String coordsStr = new StringBuilder().append(ChatColor.GOLD).append("Death Coords")
					.append(ChatColor.DARK_GRAY).append(": ")
					.append(TextUtils.locationToString(loc, ChatColor.GRAY, ChatColor.DARK_GRAY, 0))
					.append(ChatColor.DARK_GRAY).append('.').toString();
			if(tellPlayer) evt.getEntity().sendMessage(coordsStr);
			if(logConsole) plugin.getLogger().info(evt.getEntity().getName()+"'s "+coordsStr);
		}
	}
}