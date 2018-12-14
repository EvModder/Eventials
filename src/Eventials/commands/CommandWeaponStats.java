package Eventials.commands;

import java.util.Arrays;
import java.util.HashMap;
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
import EvLib.CommandBase2;
import Eventials.Eventials;

public class CommandWeaponStats extends CommandBase2 implements Listener{
	final String[] defaultStats = new String[]{"Monster Kills", "Animal Kills", "Player Kills", "Boss Kills"};
	final Object[] defaultValues = new Object[]{0, 0, 0, 0};
	final String[] defaultLore;// = new String[defaultStats.length+1];

	public CommandWeaponStats(Eventials pl, boolean enabled){
		super(pl, enabled);
		if(enabled) pl.getServer().getPluginManager().registerEvents(this, pl);

		defaultLore = new String[]{
				ChatColor.translateAlternateColorCodes('&', "&7&lWeaponStats"),
				ChatColor.translateAlternateColorCodes('&', "&a * &7"+defaultStats[0]+":&6 "+defaultValues[0])
		};
		//defaultLore[0] = ChatColor.translateAlternateColorCodes('&', "&7&lWeaponStats");
		//for(int i=0; i<defaultStats.length; ++i)
		//	defaultLore[i+1] = ChatColor.translateAlternateColorCodes(
		//		'&', "&a * &7"+defaultStats[i]+":&6 "+defaultValues[i]);
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

		ItemMeta meta = item.getItemMeta();
		meta.setLore(Arrays.asList(defaultLore));
		item.setItemMeta(meta);
		p.getInventory().setItemInMainHand(item);
		return true;
	}

	public boolean hasWeaponStats(ItemStack weapon){
		return (weapon != null && weapon.hasItemMeta() && weapon.getItemMeta().hasLore()
				&& weapon.getItemMeta().getLore().contains(defaultLore[0]));
	}

	public HashMap<String, Integer> getWeaponStats(ItemStack weapon){
		if(hasWeaponStats(weapon)){
			HashMap<String, Integer> stats = new HashMap<String, Integer>();
			for(String line : weapon.getItemMeta().getLore()){
				for(String stat : defaultStats){
					if(line.contains(stat) && line.contains(":")){
						int value = Integer.parseInt(ChatColor.stripColor(line.split(":")[1]).trim());
						stats.put(stat, value);
					}
				}
			}
			return stats;
		}
		return null;
	}

	public boolean updateWeaponStat(ItemStack weapon, String stat, int delta){
		if(hasWeaponStats(weapon)){
			ItemMeta meta = weapon.getItemMeta();
			List<String> lore = meta.getLore();
			ListIterator<String> it = lore.listIterator();
			while(it.hasNext()){
				String line = it.next();
				if(line.contains(stat) && line.contains(":")){
					int value = Integer.parseInt(ChatColor.stripColor(line.split(":")[1]).trim());
					value += delta;
					it.set(line.replaceAll("\\d*$", "") + value);
					meta.setLore(lore);
					weapon.setItemMeta(meta);
					return true;
				}
			}
		}
		return false;
	}

	@EventHandler
	public void onMobKill(EntityDeathEvent evt){
		if(evt.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent
				&& evt.getEntity() instanceof LivingEntity){
			EntityDamageByEntityEvent damageEvt = (EntityDamageByEntityEvent) evt.getEntity().getLastDamageCause();
			Object damager = damageEvt.getDamager() instanceof Arrow ?
					((Arrow)damageEvt.getDamager()).getShooter() : damageEvt.getDamager();

			if(damager instanceof Player){
				Player killer = (Player) damager;
				ItemStack weapon = killer.getInventory().getItemInMainHand();
				boolean usedBow = damageEvt.getDamager() instanceof Arrow;
				if(weapon == null || (usedBow && weapon.getType() != Material.BOW)){
					weapon = killer.getInventory().getItemInOffHand();
					if(weapon == null || (usedBow && weapon.getType() != Material.BOW)) return;
				}
				String stat = null;
				if(evt.getEntity() instanceof Monster) stat = defaultStats[0];
				else if(evt.getEntity() instanceof EnderDragon // Needs to be here since Wither falls under Creature
						 || evt.getEntity() instanceof Wither) stat = defaultStats[3];
				else if(evt.getEntity() instanceof Creature) stat = defaultStats[1];
				else if(evt.getEntity() instanceof Player) stat = defaultStats[2];
				else return;
				updateWeaponStat(weapon, stat, 1);
			}
		}
	}//EntityDeathEvent
}