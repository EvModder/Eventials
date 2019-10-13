package _SpecificAndMisc;

import java.io.File;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import Eventials.Eventials;

public class CommandVipTake implements CommandExecutor{
	
	@SuppressWarnings("deprecation") @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(args.length < 1)return false;
		Eventials pl = Eventials.getPlugin();
		OfflinePlayer p;
		try{p = pl.getServer().getOfflinePlayer(UUID.fromString(args[0]));}
		catch(IllegalArgumentException ex){p = pl.getServer().getOfflinePlayer(args[0]);}
		
		if(p == null || p.hasPlayedBefore() == false){
			sender.sendMessage("Invalid player uuid/name");
			return false;
		}
		
		//remove any/all sub-vip permissions for this player
		pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), "manudelsub "+p.getName()+" Vip");
		
		for(World world : pl.getServer().getWorlds()){
			File groups = new File("./plugins/GroupManager/worlds/"+world.getName().toLowerCase()+"/groups.yml");
			if(groups.exists() == false) continue;
			/*File users = new File("./plugins/GroupManager/worlds/"+world.getName().toLowerCase()+"/users.yml");
			
			if(groups.exists() && users.exists())
			try{
				if(WorldDataHolder.load(world.getName(), groups, users).getUser(p.getName()).getGroupName().equalsIgnoreCase("Vip")){
					pl.getServer().dispatchCommand(
							pl.getServer().getConsoleSender(), "manuadd "+p.getName()+" Default "+world.getName());
					pl.getLogger().info("Removed Vip rank from "+p.getName()+" for world: "+world.getName());
					continue;
				}
			}catch(FileNotFoundException e){}catch(IOException e){}*/
			// gets here only if there is an exception and the player's Vip is not removed.
			pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), "manuadd "+p.getName()+" Default "+world.getName());
		}
		if(p.isOnline()) p.getPlayer()
				.sendMessage("�3[�7AC�3]�f Your VIP package has expired.\n�3[�7AC�3]�f Thanks again for your contribution!");
		
		else{
			pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), "mail send "+p.getName()+
					" �3[�7AC�3]�f Your VIP package has expired.\nServer: �3[�7AC�3]�f Thanks again for your contribution!");
		}
		return true;
	}
}
