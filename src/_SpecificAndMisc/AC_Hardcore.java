package _SpecificAndMisc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;
import net.evmodder.EvLib.extras.TellrawUtils.TextHoverAction;
import net.evmodder.EvLib.extras.TabText;
import net.evmodder.EvLib.extras.TellrawUtils;
import net.evmodder.EvLib.extras.TellrawUtils.ClickEvent;
import net.evmodder.EvLib.extras.TellrawUtils.HoverEvent;
import net.evmodder.EvLib.extras.TellrawUtils.RawTextComponent;
import net.evmodder.EvLib.extras.TellrawUtils.TextClickAction;
import net.evmodder.EvLib.extras.TellrawUtils.ListComponent;
import net.evmodder.EvLib.extras.TextUtils;

public class AC_Hardcore implements Listener{
	private final Eventials pl;
	final boolean fancyPl;
	final String engravingsTellraw;

	public AC_Hardcore(){
		pl = Eventials.getPlugin();
		fancyPl = pl.getConfig().getBoolean("fancy-pl", true);
		pl.getServer().getPluginManager().registerEvents(this, pl);

		//-----------------------------------------------------------------
		String[] engravings = TabText.parse(TextUtils.translateAlternateColorCodes('&',
				"&8 • &#ec5&oForge`&f(&615 &2votes&f)\n" +
				"&8 • &#adf&oBless`&f(&610 &2votes&f)\n" +
				"&8 • &#e41&oCurse`&f(&610 &2votes&f)\n" +
				"&8 • &#5e6&oSalute`&f(&67 &2votes&f)\n" +
				"&8 • &#bbb&oEngrave`&f(&66 &2votes&f)\n" +
				"&8 • &#666remove #`&f(&61 &2vote&f)"
		), /*mono=*/false, /*flexFill=*/false, /*tabs=*/new int[]{62, 62}).split("\\n");
		ListComponent engraveComp = new ListComponent(
			new RawTextComponent(/*text=*/"", new TextClickAction(ClickEvent.RUN_COMMAND, "/embellish forge")),
			TellrawUtils.convertHexColorsToComponents(engravings[0]+"\n"));
		ListComponent forgeComp = new ListComponent(
			new RawTextComponent(/*text=*/"", new TextClickAction(ClickEvent.RUN_COMMAND, "/embellish bless")),
			TellrawUtils.convertHexColorsToComponents(engravings[1]+"\n"));
		ListComponent blessComp = new ListComponent(
			new RawTextComponent(/*text=*/"", new TextClickAction(ClickEvent.RUN_COMMAND, "/embellish curse")),
			TellrawUtils.convertHexColorsToComponents(engravings[2]+"\n"));
		ListComponent curseComp = new ListComponent(
			new RawTextComponent(/*text=*/"", new TextClickAction(ClickEvent.RUN_COMMAND, "/embellish salute")),
			TellrawUtils.convertHexColorsToComponents(engravings[3]+"\n"));
		ListComponent saluteComp = new ListComponent(
			new RawTextComponent(/*text=*/"", new TextClickAction(ClickEvent.RUN_COMMAND, "/embellish engrave")),
			TellrawUtils.convertHexColorsToComponents(engravings[4]+"\n"));
		ListComponent removeComp = TellrawUtils.convertHexColorsToComponents(engravings[5]);
		engravingsTellraw = new ListComponent(
			new RawTextComponent(ChatColor.GRAY
					+ "To embellish an item with your name using votes, hold the item\n"
					+ "you wish to modify in your main hand and select one option:\n",
				new TextHoverAction(HoverEvent.SHOW_TEXT, ChatColor.GRAY+"Click on a command to run it")),
			engraveComp, forgeComp, blessComp, curseComp, saluteComp, removeComp
		).toString();
		//-----------------------------------------------------------------
		//pl.getLogger().info("Loaded Hardcore-specific things (/pl, /engrave, /votes)");
	}

	ChatColor enableTest(String pluginName){
		Plugin plugin = Eventials.getPlugin().getServer().getPluginManager().getPlugin(pluginName);
		return (plugin != null && plugin.isEnabled()) ? ChatColor.GREEN : ChatColor.RED;
	}
	void showFancyPlugins(Player player){
		ListComponent blob = new ListComponent(
			new RawTextComponent("Plugins: "),
//			new RawTextComponent(enableTest("Renewable")+"Essentials",
//					new TextHoverAction(HoverEvent.SHOW_TEXT, "Prevents unrenewable items from being destroyed")),
//			new RawTextComponent("§r, "),
			new RawTextComponent(enableTest("Essentials")+"Essentials",
					new TextHoverAction(HoverEvent.SHOW_TEXT, "Collection of useful tools and commands")),
			new RawTextComponent("§r, "),
			new RawTextComponent(enableTest("HardcoreTweaks")+"HCTweaks",
					new TextHoverAction(HoverEvent.SHOW_TEXT, "Package of custom-built features and tweaks")),
			new RawTextComponent("§r, "),
			new RawTextComponent(enableTest("ChatManager")+"ChatManager",
					new TextHoverAction(HoverEvent.SHOW_TEXT, "Keeps chat pg13 + Color/Format for chat & signs")),
			new RawTextComponent("§r,\n"),
			new RawTextComponent(enableTest("DropHeads")+"DropHeads",
					new TextHoverAction(HoverEvent.SHOW_TEXT, "Provides a chance to get heads from mobs/players")),
			new RawTextComponent("§r, "),
			new RawTextComponent(enableTest("HorseOwners")+"HorseRank",
					new TextHoverAction(HoverEvent.SHOW_TEXT, "Naming horses and viewing/ranking their stats")),
			new RawTextComponent("§r, "),
//			new RawTextComponent(enableTest("EnchantBook")+"EnchantBook",
//					new TextHoverAction(HoverEvent.SHOW_TEXT, "Color item names in anvils, looting on axes, etc!")),
//			new RawTextComponent("§r, "),
			new RawTextComponent("§aMore", new TextHoverAction(HoverEvent.SHOW_TEXT,
//					enableTest("WorldEdit")+"WorldEdit§r, "+
//					enableTest("WorldGuard")+"WorldGuard§r, "+
//					enableTest("PluginLoader")+"PluginLoader§r, "+
					"§aEvNoCheat§r, "+
//					enableTest("PermissionsBukkit")+"PermissionsBukkit§r, "+
					enableTest("BungeeTabListPlus")+"TabList+§r, "+
					enableTest("Votifier")+"Votifier§r.")),
			new RawTextComponent("\n§7§oHover over a plugin to see more details!")
		);
		Eventials.getPlugin().sendTellraw(player.getName(), blob.toString());
	}

	@EventHandler
	public void onPreLogin(PlayerLoginEvent evt){
		final UUID uuid = evt.getPlayer().getUniqueId();
		OfflinePlayer offP = pl.getServer().getOfflinePlayer(uuid);
		if(offP.hasPlayedBefore() && offP.getLastPlayed() <= 1566802800000L){
			new BukkitRunnable(){@Override public void run(){
				Player player = pl.getServer().getPlayer(uuid);
				player.addScoreboardTag("event_participant");
			}}.runTaskLater(pl, 20);
			try{new File("./plugins/EvFolder/aug_evt/"+uuid+".txt").createNewFile();}
			catch(IOException e){e.printStackTrace();}
		}
	}

	enum Engraving{FORGE, BLESS, CURSE, SALUTE, ENGRAVE};
	Engraving getEngraving(String loreLine){
		loreLine = ChatColor.stripColor(loreLine).toLowerCase();
		if(loreLine.contains("forged")) return Engraving.FORGE;
		if(loreLine.contains("blessed")) return Engraving.BLESS;
		if(loreLine.contains("cursed")) return Engraving.CURSE;
		if(loreLine.contains("saluted")) return Engraving.SALUTE;
		if(loreLine.contains("engraved")) return Engraving.ENGRAVE;
		return null;
	}

	@EventHandler
	public void onPreCommand(PlayerCommandPreprocessEvent evt){
		if(evt.getMessage().charAt(0) != '/') return;
		String message = evt.getMessage().trim();
		String command = message.toLowerCase();
		int space = command.indexOf(' ');
		command = (space > 0 ? command.substring(1, space) : command.substring(1));
		Player player = evt.getPlayer();

		if(command.equals("pl") || command.equals("plugins")){
			if(fancyPl && player.hasPermission("bukkit.command.plugins")){
				evt.setCancelled(true);
				showFancyPlugins(player);
			}
		}
		/*if(command.contains("add_participant") && evt.getPlayer().getName().equals("EvDoc")){//TOOD: remove hacky 2019 aug event helper
			String name = message.split(" ")[1];
			@SuppressWarnings("deprecation")
			OfflinePlayer p = pl.getServer().getOfflinePlayer(name);
			try{new File("./plugins/EvFolder/aug_evt/"+p.getUniqueId()+".txt").createNewFile();}
			catch(IOException e){e.printStackTrace();}
			evt.getPlayer().sendMessage("Added: "+p.getName()+" ("+p.getUniqueId()+")");
		}*/
		if(command.equals("vote") || command.equals("votes")){
			pl.runCommand("minecraft:tellraw "+player.getName()+" ["
					+ "{\"text\":\"You currently have \",\"color\":\"gray\"},"
					+ "{\"score\":{\"name\":\""+player.getUniqueId().toString()+"\",\"objective\":\"votes-by-uuid\"},\"color\":\"dark_green\"},"
					+ "{\"text\":\" unused votes.\",\"color\":\"gray\"}]");
		}
		if(command.equals("engrave") || command.equals("embellish")){
			evt.setCancelled(true);
			ItemStack item = player.getInventory().getItemInMainHand();
			if(item == null){pl.sendTellraw(player.getName(), engravingsTellraw); return;}
			Engraving engraving = null;
			if(space > 0){
				try{engraving = Engraving.valueOf(message.substring(space+1).toUpperCase());}
				catch(IllegalArgumentException ex){engraving = null;}
			}
			int removeIndex = -1;
			if(engraving == null){
				int space2 = message.lastIndexOf(' ');
				if(space2 != space && message.substring(space+1, space2).equalsIgnoreCase("remove")){
					try{removeIndex = Integer.parseInt(message.substring(space2+1));}
					catch(IllegalArgumentException ex){
						player.sendMessage(ChatColor.RED+"Please specify the embellishment to remove (ordered from top to bottom, starting at 1)");
						return;
					};
				}
				else{pl.sendTellraw(player.getName(), engravingsTellraw); return;}
			}
			ItemMeta meta = item.getItemMeta();
			List<String> lores = (meta.hasLore() && meta.getLore() != null) ? meta.getLore() : new ArrayList<>();
			int currentIndex = 0, loreRemoveIndex = -1, loreInsertIndex = -1;
			boolean isForger = false;
			int engravingsByMe = 0;
			for(int i=0; i<lores.size(); ++i){
				Engraving existingEngraving = getEngraving(lores.get(i));
				if(existingEngraving != null){
					if(loreInsertIndex == -1){
						switch(existingEngraving){
							case FORGE: break;
							case BLESS:
								if(engraving == Engraving.FORGE) loreInsertIndex = i;
								break;
							case CURSE:
								if(engraving == Engraving.FORGE || engraving == Engraving.BLESS) loreInsertIndex = i;
								break;
							case SALUTE:
								if(engraving != Engraving.ENGRAVE && engraving != Engraving.SALUTE) loreInsertIndex = i;
								break;
							case ENGRAVE:
								if(engraving != Engraving.ENGRAVE) loreInsertIndex = i;
								break; 
						}
					}
					if(++currentIndex == removeIndex) loreRemoveIndex = i;
					if(lores.get(i).toLowerCase().endsWith(" "+player.getName().toLowerCase())){
						if(existingEngraving == Engraving.FORGE) isForger = true;
						if(existingEngraving == engraving){
							player.sendMessage(ChatColor.RED+"You already have that embellishment on this item");
							return;
						}
						if(++engravingsByMe == 2 && removeIndex == -1){
							player.sendMessage(ChatColor.RED+"Items can only receive 2 engravings per player");
							return;
						}
						if(existingEngraving == Engraving.CURSE && engraving == Engraving.BLESS){
							player.sendMessage(ChatColor.RED+"You cannot bless an item which you have cursed");
							return;
						}
						if(existingEngraving == Engraving.BLESS && engraving == Engraving.CURSE){
							player.sendMessage(ChatColor.RED+"You cannot curse an item which you have blessed");
							return;
						}
					}
				}
			}
			//
			int votes = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("votes-by-uuid").getScore(player.getUniqueId().toString()).getScore();
			int cost = 1; // cost to remove is 1
			String newLore = "";
			if(engraving != null) switch(engraving){
				case FORGE:  cost = 15; newLore = "&#ec5&oForged by "; break;
				case BLESS:  cost = 10; newLore = "&#adf&oBlessed by "; break;
				case CURSE:  cost = 10; newLore = "&#e41&oCursed by "; break;
				case SALUTE: cost = 07; newLore = "&#5e6&oSaluted by "; break;
				case ENGRAVE:cost = 06; newLore = "&#bbb&oEngraved by "; break;
			}
			if(votes < cost){
				player.sendMessage(ChatColor.RED+"This requires "+cost+" votes, you only have "+votes);
				return;
			}
			//
			if(removeIndex != -1){
				if(!isForger){
					player.sendMessage(ChatColor.RED+"For you to remove embellishments, the item must be 'Forged by "+player.getName()+"'");
					return;
				}
				if(loreRemoveIndex == -1){
					player.sendMessage(ChatColor.RED+"Specified index is too high! This item only has "+currentIndex+" embellishment");
					return;
				}
				player.sendMessage(ChatColor.GRAY+"Removing: '"+lores.get(loreRemoveIndex)+ChatColor.GRAY+"'");
				lores.remove(loreRemoveIndex);
			}
			else{
				newLore = TextUtils.translateAlternateColorCodes('&', newLore)+player.getName();
				if(loreInsertIndex == -1) lores.add(newLore);
				else lores.add(loreInsertIndex, newLore);
				player.sendMessage(newLore+"!");
			}
			meta.setLore(lores);
			item.setItemMeta(meta);
			player.getInventory().setItemInMainHand(item);
			pl.runCommand("scoreboard players set "+player.getUniqueId()+" votes-by-uuid "+(votes-cost));
			pl.runCommand("scoreboard players set "+player.getName()+" votes-by-name "+(votes-cost));
		}
	}
}