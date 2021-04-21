package Eventials.economy.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import Eventials.Eventials;
import Eventials.economy.EvEconomy;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.extras.TellrawUtils.TextClickAction;
import net.evmodder.EvLib.extras.TellrawUtils.ClickEvent;
import net.evmodder.EvLib.extras.TellrawUtils.ListComponent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;

public class CommandAdvertise extends EvCommand{
	private EvEconomy economy;
	final int MAX_LENGTH;// Max characters in ad
	final int COST;// Price of advertising
	final long DURATION;// Duration (in millis) of ad
	final String curSymbol;// Currency symbol used in Economy
	private long expiresOn;// Date on which the current advertisement expires

	public CommandAdvertise(JavaPlugin pl, EvEconomy eco){
		super(pl);
		economy = eco;
		DURATION = pl.getConfig().getInt("ad-duration")*86400000L;//days to millis
		MAX_LENGTH = pl.getConfig().getInt("ad-max-length", 46);
		COST = pl.getConfig().getInt("ad-cost");
		expiresOn = pl.getConfig().getLong("ad-expires-on");
		curSymbol = TextUtils.translateAlternateColorCodes('&', pl.getConfig().getString("currency-symbol", "&2L"));
	}

	@Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args){return null;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(new GregorianCalendar().getTimeInMillis() < expiresOn){
			sender.sendMessage(ChatColor.YELLOW+"Error: The current advertisement has not yet expired");
			return true;
		}
		if(args.length < 1) return false;

		StringBuilder builder = new StringBuilder(args[0]);
		for(int i = 1; i < args.length; ++i) builder.append(' ').append(args[i]);

		String advert = builder.toString();
		if(advert.startsWith("pay* ")) advert = advert.substring(5);

		String formattedAdvert = ChatColor.translateAlternateColorCodes('&', advert);
		String noFormatAdvert = ChatColor.stripColor(formattedAdvert);

		if(noFormatAdvert.length() > MAX_LENGTH || (advert.contains("&l") && 1.2*noFormatAdvert.length() > MAX_LENGTH)){
			sender.sendMessage(ChatColor.RED+"That advertisement is too long to fit in the MOTD!");
			return true;
		}

		if(sender instanceof Player && !args[0].equals("pay*")){
			//-----------------------------------------------------------
			String preMsg = "\n\n\n\n\n\n\n\n\n"+ChatColor.DARK_AQUA+ChatColor.BOLD+ChatColor.STRIKETHROUGH
					+"======================================="
					+ChatColor.GRAY+"Click to pay the "+ChatColor.GREEN+curSymbol+COST+ChatColor.GRAY+" and set the ad: [";
			String hyperMsg = ChatColor.WHITE+">>"+ChatColor.AQUA+"Pay "+curSymbol+COST+ChatColor.WHITE+"<<";
			String postMsg = ChatColor.GRAY+"]\n"+ChatColor.BLUE+ChatColor.BOLD+ChatColor.STRIKETHROUGH
					+"======================================="
					+ChatColor.DARK_RED+"["+ChatColor.RED+ChatColor.BOLD+"Warn"+ChatColor.DARK_RED+"] "
					+ChatColor.GOLD+"Advertising other servers within this section is not permitted, " +
					"and you will not be refunded if your advertisement is removed.\n\n ";
			ListComponent blob = new ListComponent();
			blob.addComponent(preMsg);
			blob.addComponent(new RawTextComponent(hyperMsg, new TextClickAction(ClickEvent.RUN_COMMAND, "/advertise pay* "+advert)));
			blob.addComponent(postMsg);
			Eventials.getPlugin().sendTellraw(sender.getName(), blob.toString());
			//-----------------------------------------------------------
			sender.sendMessage(ChatColor.AQUA+"- "+ChatColor.DARK_GRAY+"[ "+ChatColor.LIGHT_PURPLE
					+"Ad: "+ChatColor.DARK_GREEN+formattedAdvert+ChatColor.DARK_GRAY+" ]");
			return true;
		}
		else{
			// check money
			if(sender instanceof Player && !economy.playerToServer(((Player)sender).getUniqueId(), COST)){
				sender.sendMessage(ChatColor.RED+"Unable to make payment, check your balance");
			}
			else{
				// set advertisement
				if(setAdvertisement(advert)){
					sender.sendMessage(ChatColor.GREEN+"Advertisement set!");
					expiresOn = new GregorianCalendar().getTimeInMillis() + DURATION;
					Eventials.getPlugin().getConfig().set("ad-expires-on", expiresOn);
				}
				// if failed, and the sender is a player
				else{
					sender.sendMessage(ChatColor.RED+"Encountered an error while updating the MOTD.");
					if(sender instanceof Player) economy.serverToPlayer(((Player)sender).getUniqueId(), COST);
				}
			}
			return true;
		}
	}

	public static boolean setAdvertisement(String advertisement){
		File motdFile = new File("./plugins/Essentials/motd.txt");
		try{
			//load essentials MOTD
			BufferedReader reader = new BufferedReader(new FileReader(motdFile));
			StringBuilder file = new StringBuilder(); String line;
			while((line = reader.readLine()) != null) file.append(line).append('\n');
			reader.close();

			String[] lines = file.toString().split("\n");
			file = new StringBuilder();
			//length-2 to cut off the extra new line and cut off the last line (old ad)
			for(int i = 0; i < lines.length-2; ++i) file.append(lines[i]).append('\n');

			file.append("&3&o- &8[ &dAd: &2").append(advertisement).append("&8 ]");

			//write to essentials MOTD
			BufferedWriter writer = new BufferedWriter(new FileWriter(motdFile));
			writer.write(file.toString()); writer.flush(); writer.close();
			return true;
		}
		catch(FileNotFoundException e){
			Eventials.getPlugin().getLogger().info(
					ChatColor.RED+"Could not load the server's MOTD! (it may not have one right now)");
			return false;
		}
		catch(IOException e){
			Eventials.getPlugin().getLogger().info(e.getStackTrace().toString());
			return false;
		}
	}
}