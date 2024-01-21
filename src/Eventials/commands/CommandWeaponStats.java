package Eventials.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import Eventials.Eventials;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.extras.TextUtils;
import net.evmodder.EvLib.extras.TypeUtils;

public class CommandWeaponStats extends EvCommand implements Listener{
	private final String WSTATS_TAG;
	private final HashMap<WeaponStat, Object> WSTATS;
//	private final String[] DEFAULT_WSTATS;

	private enum WeaponStat{
		PLAYER_KILLS{
			@Override public String toString(){return "Player Kills";}
		},
		MONSTER_KILLS{
			@Override public String toString(){return "Monster Kills";}
		},
		ANIMAL_KILLS{
			@Override public String toString(){return "Animal Kills";}
		},
		BOSS_KILLS{
			@Override public String toString(){return "Boss Kills";}
		},
		BLOCKS_MINED{
			@Override public String toString(){return "Blocks Broken";}
		},
		/*CHANGED_HANDS{
			@Override public String toString(){return "Ownership Transfers";}
		},*/
		LAST_VICTIM{
			@Override public String toString(){return "Last Victim";}
		};
		@Override abstract public String toString();
	}

	private String getLoreForStat(WeaponStat stat){
		return ChatColor.translateAlternateColorCodes('&', "&a * &7"+stat+":&6 "+WSTATS.get(stat));
	}
	private String getLoreForStat(WeaponStat stat, Object value){
		return ChatColor.translateAlternateColorCodes('&', "&a * &7"+stat+":&6 "+value);
	}

	private WeaponStat getStatFromLore(String lore){
		int sep = lore.indexOf(':');
		if(sep > 0) lore = lore.substring(0, sep);
		String statName = ChatColor.stripColor(lore).replaceAll("[\\s*]", "").toUpperCase();
		try{return WeaponStat.valueOf(statName);}
		catch(IllegalArgumentException ex){return null;}
	}

	private boolean hasWeaponStats(ItemStack item){
		return (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()
				&& item.getItemMeta().getLore().contains(WSTATS_TAG));
	}

	private boolean incrementWeaponStat(ItemStack item, WeaponStat statToChange, int delta){
		if(hasWeaponStats(item)){
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			ListIterator<String> it = lore.listIterator();
			while(it.hasNext()){
				String line = it.next();
				if(getStatFromLore(line) == statToChange){
					int value = Integer.parseInt(ChatColor.stripColor(line.split(":")[1]).trim());
					it.set(getLoreForStat(statToChange, value + delta));
					meta.setLore(lore);
					item.setItemMeta(meta);
					return true;
				}
			}
		}
		return false;
	}
	private boolean setWeaponStat(ItemStack item, WeaponStat statToChange, Object value){
		if(hasWeaponStats(item)){
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			ListIterator<String> it = lore.listIterator();
			while(it.hasNext()){
				String line = it.next();
				if(getStatFromLore(line) == statToChange){
					it.set(getLoreForStat(statToChange, value));
					meta.setLore(lore);
					item.setItemMeta(meta);
					return true;
				}
			}
		}
		return false;
	}

	public CommandWeaponStats(Eventials pl, boolean enabled){
		super(pl, enabled);
		if(enabled) pl.getServer().getPluginManager().registerEvents(this, pl);

		WSTATS_TAG = ChatColor.translateAlternateColorCodes('&', "&7&lWeaponStats");
		WSTATS = new HashMap<>();
		WSTATS.put(WeaponStat.PLAYER_KILLS, 0);
		WSTATS.put(WeaponStat.MONSTER_KILLS, 0);
		WSTATS.put(WeaponStat.ANIMAL_KILLS, 0);
		WSTATS.put(WeaponStat.BOSS_KILLS, 0);
		WSTATS.put(WeaponStat.BLOCKS_MINED, 0);
//		WSTATS.put(WeaponStat.CHANGED_HANDS, 0);
		WSTATS.put(WeaponStat.LAST_VICTIM, "");
//		DEFAULT_WSTATS = new String[]{
//			WSTATS_TAG,
//			getLoreForStat(WeaponStat.PLAYER_KILLS),
//			getLoreForStat(WeaponStat.MONSTER_KILLS)
//		};
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
		if(args.length > 0 && sender instanceof Player){
			HashSet<WeaponStat> alreadyAdded = new HashSet<>();
			String lastArg = args[args.length-1];
			for(String arg : args){
				try{alreadyAdded.add(WeaponStat.valueOf(arg.toUpperCase()));}
				catch(IllegalArgumentException ex){if(!arg.equals(lastArg)) return null;}
			}
			lastArg = lastArg.toUpperCase();
			final List<String> tabCompletes = new ArrayList<>();
			for(WeaponStat stat : WSTATS.keySet()){
				if(!alreadyAdded.contains(stat) && stat.name().startsWith(lastArg)){
					tabCompletes.add(stat.name());
				}
			}
			return tabCompletes;
		}
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players!");
			return true;
		}
		Player p = (Player) sender;
		ItemStack item = p.getInventory().getItemInMainHand();
		if(item == null || item.getItemMeta() == null){
			sender.sendMessage(ChatColor.RED+"Invalid item in hand");
			return true;
		}
		List<WeaponStat> statsToAdd;
		if(args.length == 0){
			if(TypeUtils.isAxe(item.getType()))
				statsToAdd = Arrays.asList(WeaponStat.MONSTER_KILLS, WeaponStat.PLAYER_KILLS, WeaponStat.BLOCKS_MINED);
			else if(TypeUtils.isPickaxe(item.getType()) || TypeUtils.isShovel(item.getType()) || TypeUtils.isHoe(item.getType()))
				statsToAdd = Arrays.asList(WeaponStat.BLOCKS_MINED);
			else
				statsToAdd = Arrays.asList(WeaponStat.MONSTER_KILLS, WeaponStat.PLAYER_KILLS);
		}
		else{
			statsToAdd = new ArrayList<>();
			for(String arg : args){
				try{statsToAdd.add(WeaponStat.valueOf(arg.toUpperCase()));}
				catch(IllegalArgumentException ex){
					sender.sendMessage(ChatColor.RED+"Unknown stat: "+arg);
					return true;
				}
			}
		}

		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.hasLore() && !meta.getLore().isEmpty() ? meta.getLore() : Arrays.asList(WSTATS_TAG);
		if(!lore.get(0).equals(WSTATS_TAG)) lore.add(0, WSTATS_TAG);
		for(String line : lore) statsToAdd.remove(getStatFromLore(line));
		if(statsToAdd.isEmpty()){
			sender.sendMessage(ChatColor.RED+"This item is already tracking the specified stats");
			return true;
		}
		for(WeaponStat newStat : statsToAdd) lore.add(getLoreForStat(newStat));
		meta.setLore(lore);
		item.setItemMeta(meta);
		p.getInventory().setItemInMainHand(item);
		sender.sendMessage(ChatColor.GREEN+"Added "+ChatColor.GOLD+statsToAdd.size()+ChatColor.GREEN+" stats!");
		return true;
	}

	@SuppressWarnings("deprecation") @EventHandler
	public void onMobKill(EntityDeathEvent evt){
		if(evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent
				&& evt.getEntity() instanceof LivingEntity){
			EntityDamageByEntityEvent damageEvt = (EntityDamageByEntityEvent) evt.getEntity().getLastDamageCause();
			boolean usedBow = damageEvt.getDamager() instanceof Arrow;
			Object damager = usedBow ? ((Arrow)damageEvt.getDamager()).getShooter() : damageEvt.getDamager();

			if(damager instanceof Player){
				Player killer = (Player) damager;
				ItemStack weapon = killer.getInventory().getItemInMainHand();
				if(weapon == null || (usedBow &&
						weapon.getType() != Material.BOW && weapon.getType() != Material.CROSSBOW)){
					weapon = killer.getInventory().getItemInOffHand();
					if(weapon == null || (usedBow &&
							weapon.getType() != Material.BOW && weapon.getType() != Material.CROSSBOW)) return;
				}
				WeaponStat stat = null;
				if(evt.getEntity() instanceof Monster) stat = WeaponStat.MONSTER_KILLS;
				else if(evt.getEntity() instanceof EnderDragon || evt.getEntity() instanceof Wither)
					stat = WeaponStat.BOSS_KILLS;
				else if(evt.getEntity() instanceof Creature) stat = WeaponStat.ANIMAL_KILLS;
				else if(evt.getEntity() instanceof Player) stat = WeaponStat.PLAYER_KILLS;
				else return;
				incrementWeaponStat(weapon, stat, 1);

				String victimName = evt.getEntity().getCustomName();
				if(victimName == null) victimName = TextUtils.getNormalizedName(evt.getEntityType());
				setWeaponStat(weapon, WeaponStat.LAST_VICTIM, victimName);
			}
		}
	}//EntityDeathEvent
}