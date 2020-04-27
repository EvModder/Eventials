package Eventials.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.EulerAngle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.extras.TextUtils;

public class CommandFloatingText extends EvCommand{
	public static final String ICON =
			ChatColor.translateAlternateColorCodes('&', "&c[&6F&eT&aX&2T&3]&r");
	public static final String LOGO = 
			ChatColor.translateAlternateColorCodes('&', "&4F&cl&6o&ea&at&2i&3n&9g&1T&5e&dx&4t&r");
	public static final String HEADER =
			ChatColor.translateAlternateColorCodes('&',
					"&7&m--------------------[&r "+LOGO+" &7&m]--------------------");
	static final String nearbyChoicesPrompt =
			ChatColor.translateAlternateColorCodes('&', " &7Run &2/ftxt list&7 to view nearby choices");
	public CommandFloatingText(EvPlugin p) { super(p); }
	HashMap<UUID, Vector<ArmorStand>> listResults = new HashMap<UUID, Vector<ArmorStand>>();

	final String[] COMMANDS = new String[]{"help","place","remove","edit","move","list"};
	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String Label, String[] args){
		if(sender instanceof Player && args.length > 0){
			args[0] = args[0].toLowerCase();
			if(args.length == 1){
				final List<String> tabCompletes = new ArrayList<String>();
				for(String c : COMMANDS) if(c.startsWith(args[0])) tabCompletes.add(c);
				return tabCompletes;
			}
			if(args.length == 2 && (args[0].equals("remove") || args[0].equals("edit") || args[0].equals("move"))){
				final List<String> tabCompletes = new ArrayList<String>();
				args[1] = args[1].toLowerCase();
				for(int i=0; i<listResults.get(((Player)sender).getUniqueId()).size(); ++i)
						if((""+i).startsWith(args[1])) tabCompletes.add(""+i);
				return tabCompletes;
			}
			else if(args.length > 2 && args[0].equals("move")){
				Location loc = ((Player)sender).getLocation();
				String pCoords = "" + loc.getBlockX() + ' ' + loc.getBlockY() + ' ' + loc.getBlockZ();
				String typedCoords = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
				if(pCoords.startsWith(typedCoords)) return Arrays.asList(pCoords);
			}
		}
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		listResults.putIfAbsent(((Player)sender).getUniqueId(), new Vector<ArmorStand>());
		if(args.length == 0) return handleHelp(sender, args);
		args[0] = args[0].toLowerCase();
		if(args[0].startsWith("p") || args[0].equals("set")) return handlePlace(sender, args);
		if(args[0].startsWith("r") || args[0].equals("del")) return handleRemove(sender, args);
		if(args[0].startsWith("e")) return handleEdit(sender, args);
		if(args[0].startsWith("m")) return handleMove(sender, args);
		if(args[0].startsWith("l")) return handleList(sender);
		return handleHelp(sender, args);// send help
	}

	public static ArmorStand placeFloater(Location loc, String msg){
		ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class);

		as.setInvulnerable(true);
		as.setGravity(false);
		as.setVisible(false);
		as.setMarker(true);
		as.setCustomNameVisible(true);
		as.setCollidable(false);
		as.setArms(false);
		as.setBasePlate(false);
		as.setCanPickupItems(false);
		as.setSmall(true);
		as.setSilent(true);
		as.setHeadPose(EulerAngle.ZERO);
		as.setBodyPose(EulerAngle.ZERO);
		as.setLeftArmPose(EulerAngle.ZERO);
		as.setRightArmPose(EulerAngle.ZERO);
		as.setLeftLegPose(EulerAngle.ZERO);
		as.setRightLegPose(EulerAngle.ZERO);

		//as.setMaximumAir(1337);
		as.setCustomName(msg);
		return as;
	}

	public static boolean removeFloater(ArmorStand as){
		boolean wasValid = as.isValid();
		as.remove();
		return wasValid;
	}

	public static boolean isFloater(ArmorStand e){
		return /*e.getMaximumAir() == 1337 && */e.getCustomName() != null;
		//return e.isSmall() && !e.isCollidable() && !e.isVisible() && e.getCustomName() != null;
	}

	public static String implodeAndFormat(String[] args, int start, int end){
		String txt = StringUtils.join(args, ' ', start, end);
		txt = TextUtils.translateAlternateColorCodes('&', txt);
		txt = txt.replaceAll("(?<=(?:^|[^\\\\]))(\\\\{2})*\\\\n", "$1\n");
		txt = txt.replaceAll("(?<=(?:^|[^\\\\]))(\\\\{2})*>", "$1\n");
		txt = txt.replaceAll("(?<=(?:^|[^\\\\]))(\\\\{2})*\\\\s", "$1 ");
		txt = txt.replaceAll("(?<=(?:^|[^\\\\]))(\\\\{2})*\\\\>", "$1>");
		txt = txt.replaceAll("\\\\\\\\", "\\\\");
		return txt;
	}

	public static Vector<ArmorStand> getNearbyFloaters(Location loc){
		Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc, 6, 5, 6);
		Vector<ArmorStand> floaters = new Vector<ArmorStand>();
		if(nearbyEntities == null) return floaters;
		for(Entity e : nearbyEntities){
			if(e instanceof ArmorStand && isFloater((ArmorStand)e)){
				floaters.add((ArmorStand)e);
			}
		}
		return floaters;
	}

	// Beware: L3thal Code
	private boolean handleHelp(CommandSender sender, String... args){
		sender.sendMessage(new StringBuilder(HEADER)
			.append('\n').append(ChatColor.GREEN).append("/ftxt place <txt>").append(ChatColor.DARK_GRAY)
			.append(" - ").append(ChatColor.GRAY).append("Place floating text at your location")
			.append('\n').append(ChatColor.GREEN).append("/ftxt remove <ID>").append(ChatColor.DARK_GRAY)
			.append(" - ").append(ChatColor.GRAY).append("Remove a floating text")
			.append('\n').append(ChatColor.GREEN).append("/ftxt edit <ID>").append(ChatColor.DARK_GRAY)
			.append(" - ").append(ChatColor.GRAY).append("Edit a floating text")
			.append('\n').append(ChatColor.GREEN).append("/ftxt move <ID> <location>").append(ChatColor.DARK_GRAY)
			.append(" - ").append(ChatColor.GRAY).append("Move a floating text")
			.append('\n').append(ChatColor.GREEN).append("/ftxt list").append(ChatColor.DARK_GRAY)
			.append(" - ").append(ChatColor.GRAY).append("List nearby floating texts")
		.toString());
		return true;
	}

	private boolean handlePlace(CommandSender sender, String... args){
		if(args.length < 2){
			sender.sendMessage(ChatColor.RED + "Too few arguments!");
			return true;
		}
		double offset = 1;
		/*try{ offset = Double.parseDouble(args[1]); }
		catch(NumberFormatException ex){
			sender.sendMessage(ChatColor.RED + "Invalid offset, must be a number value");
			return false;
		}*/
		Location loc = ((Player)sender).getLocation();
		loc.setY(loc.getY() + offset);
		loc.setX(Math.round(loc.getX()*2D)/2D);//round to nearest 0.5
		loc.setZ(Math.round(loc.getZ()*2D)/2D);

		String txt = implodeAndFormat(args, 1, args.length);

		CommandFloatingText.placeFloater(loc, txt);
		sender.sendMessage(ICON + ChatColor.GRAY + " " + TextUtils.locationToString(loc, ChatColor.GRAY, ChatColor.DARK_GRAY) + ChatColor.GREEN + ": \""
								+ ChatColor.RESET + txt + ChatColor.GREEN + "\" placed.");
		return true;
	}

	private boolean handleEdit(CommandSender sender, String... args){
		if(args.length < 3){
			sender.sendMessage(ChatColor.RED + "Too few arguments!");
			return true;
		}
		Vector<ArmorStand> nearbyFloaters = listResults.get(((Player)sender).getUniqueId());
		if(nearbyFloaters.isEmpty()){
			sender.sendMessage(ICON + ChatColor.RED + " no choices found!");
			sender.sendMessage(ICON + nearbyChoicesPrompt);
			return true;
		}
		int choice;
		try{ choice = Integer.parseInt(args[1]); }
		catch(NumberFormatException ex){
			sender.sendMessage(ChatColor.RED + "Invalid choice, must be a number value");
			return false;
		}
		if(choice >= nearbyFloaters.size()){
			sender.sendMessage(ChatColor.RED + "Invalid choice, pick from " + ChatColor.GREEN + "/ftxt list");
			return true;
		}
		ArmorStand as = nearbyFloaters.get(choice);
		if(as == null){
			sender.sendMessage(ChatColor.RED + "ERROR: The selected floating text cannot be located!");
			return true;
		}
		Location oldLoc = as.getLocation().clone();
		String oldTxt = as.getCustomName();
		String newTxt = implodeAndFormat(args, 2, args.length);

		if(!removeFloater(as)){
			sender.sendMessage(ChatColor.RED + "ERROR: Unable to update the floating text!");
			return true;
		}
		nearbyFloaters.set(choice, placeFloater(oldLoc, newTxt));

//		sender.sendMessage(ICON + ChatColor.GOLD + " Old: " + ChatColor.GRAY + Text.locationToString(oldLoc)
//								+ ChatColor.GOLD + ", " + ChatColor.RESET + oldTxt);
		sender.sendMessage(ICON + ChatColor.GREEN + " Edit successful!");
		sender.sendMessage(ICON + ChatColor.GOLD + " Old: " + ChatColor.RESET + oldTxt);
		sender.sendMessage(ICON + ChatColor.GOLD + " New: " + ChatColor.RESET + newTxt);
		return true;
	}

	private boolean handleRemove(CommandSender sender, String... args){
		if(args.length < 2){
			sender.sendMessage(ChatColor.RED + "Too few arguments!");
			return true;
		}
		Location loc = ((Player)sender).getLocation();
		Vector<ArmorStand> nearbyFloaters = listResults.get(((Player)sender).getUniqueId());
		if(nearbyFloaters.isEmpty()){
			sender.sendMessage(ICON + ChatColor.RED + " no choices found!");
			sender.sendMessage(ICON + nearbyChoicesPrompt);
			return true;
		}
		int choice;
		try{ choice = Integer.parseInt(args[1]); }
		catch(NumberFormatException ex){
			sender.sendMessage(ChatColor.RED + "Invalid choice, must be a number value");
			return false;
		}
		if(choice >= nearbyFloaters.size()){
			sender.sendMessage(ChatColor.RED + "Invalid choice, pick from " + ChatColor.GREEN + "/ftxt list");
			return true;
		}
		ArmorStand as = nearbyFloaters.get(choice);
		if(as == null){
			sender.sendMessage(ChatColor.RED + "ERROR: The selected floating text cannot be located!");
			return true;
		}
		String txt = as.getCustomName();

		if(!removeFloater(as)){
			sender.sendMessage(ChatColor.RED + "ERROR: Unable to update the floating text!");
			return true;
		}
		nearbyFloaters.set(choice, null);

		sender.sendMessage(ICON + " " + ChatColor.GRAY + TextUtils.locationToString(loc, ChatColor.GRAY, ChatColor.DARK_GRAY) + ChatColor.GRAY + ": \""
				+ ChatColor.RESET + txt + ChatColor.GRAY + "\" removed.");
		return true;
	}

	private boolean handleMove(CommandSender sender, String... args){
		if(args.length != 3 && args.length < 5){
			sender.sendMessage(ChatColor.RED + "Too few arguments!");
			return true;
		}
		Location newLoc = ((Player)sender).getLocation();
		Vector<ArmorStand> nearbyFloaters = listResults.get(((Player)sender).getUniqueId());
		if(nearbyFloaters.isEmpty()){
			sender.sendMessage(ICON + ChatColor.RED + " no choices found!");
			sender.sendMessage(ICON + nearbyChoicesPrompt);
			return true;
		}
		int choice;
		try{ choice = Integer.parseInt(args[1]); }
		catch(NumberFormatException ex){
			sender.sendMessage(ChatColor.RED + "Invalid choice, must be a number value");
			return false;
		}
		if(choice >= nearbyFloaters.size()){
			sender.sendMessage(ChatColor.RED + "Invalid choice, pick from " + ChatColor.GREEN + "/ftxt list");
			return true;
		}
		ArmorStand as = nearbyFloaters.get(choice);
		if(as == null){
			sender.sendMessage(ChatColor.RED + "ERROR: The selected floating text cannot be located!");
			return true;
		}
		if(args.length >= 5){
			try{ newLoc = new Location(newLoc.getWorld(),
					Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4])); }
			catch(NumberFormatException ex){
				sender.sendMessage(ChatColor.RED + "Invalid location! must enter numeric coordinates");
				return false;
			}
		}
		String oldTxt = as.getCustomName();
		Location oldLoc = as.getLocation().clone();

		as.teleport(newLoc, TeleportCause.COMMAND);
		/*if(!removeFloater(as)){
			sender.sendMessage(ChatColor.RED + "ERROR: Unable to update the floating text!");
			return true;
		}
		nearbyFloaters.set(choice, placeFloater(newLoc, oldTxt));*/

		sender.sendMessage(ICON + ChatColor.GREEN + " Move of ["+ChatColor.RESET+oldTxt+ChatColor.GREEN+"] successful!");
		sender.sendMessage(ICON + ChatColor.GOLD + " Old: " + ChatColor.GRAY + TextUtils.locationToString(oldLoc, ChatColor.GRAY, ChatColor.DARK_GRAY));
		sender.sendMessage(ICON + ChatColor.GOLD + " New: " + ChatColor.GRAY + TextUtils.locationToString(newLoc, ChatColor.GRAY, ChatColor.DARK_GRAY));
		return true;
	}

	private boolean handleList(CommandSender sender){
		UUID uuid = ((Player)sender).getUniqueId();
		Location loc = ((Player)sender).getLocation();
		listResults.put(uuid, getNearbyFloaters(loc));
		if(listResults.get(uuid).isEmpty()){
			sender.sendMessage(ICON + ChatColor.GRAY + " no floating text nearby");
			return true;
		}

		sender.sendMessage(HEADER);
		String listFormat = ChatColor.translateAlternateColorCodes('&', "&7[&eID&7] <&fCurrent Message&7>");
		StringBuilder builder = new StringBuilder(listFormat);
		int i = 0;
		for(ArmorStand floater : listResults.get(uuid)){
			String txt = floater.getCustomName();
			builder.append('\n').append(ChatColor.GRAY)
				.append('[').append(ChatColor.YELLOW).append(i++).append(ChatColor.GRAY).append("] ")
				.append(ChatColor.RESET).append(txt);
		}
		sender.sendMessage(builder.toString());
		return true;
	}
}