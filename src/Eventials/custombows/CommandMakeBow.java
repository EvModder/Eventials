package Eventials.custombows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import Eventials.custombows.CustomBows.BowType;
import net.evmodder.EvLib.EvCommand;
import net.evmodder.EvLib.extras.NBTTagUtils;
import net.evmodder.EvLib.extras.NBTTagUtils.RefNBTTagCompound;
import net.evmodder.EvLib.extras.TextUtils;

public class CommandMakeBow extends EvCommand{
	public CommandMakeBow(JavaPlugin pl){super(pl);}

	String getAttributeName(Attribute attribute){ // Uses 1.16+ names
		switch(attribute){
			case GENERIC_ARMOR: return "generic.armor";
			case GENERIC_ARMOR_TOUGHNESS: return "generic.armor_toughness";
			case GENERIC_ATTACK_DAMAGE: return "generic.attack_damage";
			case GENERIC_ATTACK_KNOCKBACK: return "generic.attack_knockback";
			case GENERIC_ATTACK_SPEED: return "generic.attack_speed";
			case GENERIC_FLYING_SPEED: return "generic.flying_speed";
			case GENERIC_FOLLOW_RANGE: return "generic.follow_range";
			case GENERIC_KNOCKBACK_RESISTANCE: return "generic.knockback_resistance";
			case GENERIC_LUCK: return "generic.luck";
			case GENERIC_MAX_HEALTH: return "generic.max_health";
			case GENERIC_MOVEMENT_SPEED: return "generic.movement_speed";
			case HORSE_JUMP_STRENGTH: return "horse.jump_strength";
			case ZOMBIE_SPAWN_REINFORCEMENTS: return "zombie.spawn_reinforcements";
		}
		return null;
	}
	void addAttribute(ItemMeta meta, Attribute attribute, double amount, Operation operation, boolean randUUID){
		String attributeName = getAttributeName(attribute);
		UUID uuid = randUUID ? UUID.randomUUID() : UUID.fromString(attributeName);
		AttributeModifier modifer = new AttributeModifier(uuid, attributeName, amount, operation);
		meta.addAttributeModifier(attribute, modifer);
	}

	ItemStack makeBow(BowType type){
		ItemStack item = new ItemStack(Material.BOW);
		RefNBTTagCompound tag = new RefNBTTagCompound();
		tag.setString("BowType", type.name().toUpperCase());
//		tag.setInt("Unbreakable", 1);
		tag.setInt("Age", -32768); // prevent item despawning
		RefNBTTagCompound mailTag = new RefNBTTagCompound(); mailTag.setInt("Eventials:mailable", 42); tag.set("PublicBukkitValues", mailTag);
		item = NBTTagUtils.setTag(item, tag);
		ItemMeta meta = item.getItemMeta();
		switch(type){
			case DETERMINED:
				meta.setCustomModelData(42);
				break;
			case FINDER:
				break;
			case FLINT:
				meta.setCustomModelData(42);
				addAttribute(meta, Attribute.GENERIC_ATTACK_DAMAGE, 4, Operation.ADD_NUMBER, /*randUUID=*/false); // Attack +4
				addAttribute(meta, Attribute.GENERIC_MOVEMENT_SPEED, .05D, Operation.ADD_SCALAR, /*randUUID=*/false); // Movement +5%
				meta.setDisplayName(ChatColor.DARK_GRAY+""+ChatColor.BOLD+"Flint");
				meta.addItemFlags(/*ItemFlag.HIDE_UNBREAKABLE, */ItemFlag.HIDE_ATTRIBUTES);
				item.setItemMeta(meta);
				return item;
			case FOLLOWER:
				break;
			case FORCE:
				break;
			case GANDIVA:
				meta.setCustomModelData(42);
				addAttribute(meta, Attribute.GENERIC_ATTACK_DAMAGE, 2, Operation.ADD_NUMBER, /*randUUID=*/false); // Attack +2
				addAttribute(meta, Attribute.GENERIC_KNOCKBACK_RESISTANCE, .25D, Operation.ADD_SCALAR, /*randUUID=*/false); // Knockback +25%
				addAttribute(meta, Attribute.GENERIC_MOVEMENT_SPEED, .05D, Operation.ADD_SCALAR, /*randUUID=*/false); // Movement -5%
				meta.setDisplayName(ChatColor.GRAY+"Gandiva");
				meta.addEnchant(Enchantment.ARROW_DAMAGE, 6, true);
				meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
				meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
				meta.addItemFlags(/*ItemFlag.HIDE_UNBREAKABLE, */ItemFlag.HIDE_ATTRIBUTES);
				item.setItemMeta(meta);
				return item;
			case ICHAIVAL:
				break;
			case ICARUS:
				break;
			case NONE:
				break;
			case RAPIDFIRE:
				break;
			case TARGETFIRE:
				meta.setDisplayName(ChatColor.RESET+"Targetfire");
				meta.setLore(Arrays.asList(TextUtils.translateAlternateColorCodes('&', "&6\u1f3f9 &#f442&#ddd0&#f442&#ddd0&#333 / 08")));
				meta.setCustomModelData(2020);
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addEnchant(Enchantment.MENDING, 1, true);
				item.setItemMeta(meta);
				return item;
		}
		return null;
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String Label, String[] args){
		if(args.length == 1 && sender instanceof Player){
			final List<String> tabCompletes = new ArrayList<>();
			args[0] = args[0].toLowerCase();
			for(BowType bow : BowType.values()){
				if(bow.name().toLowerCase().startsWith(args[0])) tabCompletes.add(bow.name().toLowerCase());
			}
			return tabCompletes;
		}
		return null;
	}

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/makebow <type>
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
			return true;
		}
		BowType type;

		if(args.length < 1){
			sender.sendMessage(ChatColor.RED+"Too few arguments!\n"+ChatColor.GRAY+command.getUsage());
			return true;
		}
		else{
			try{type = BowType.valueOf(args[0].toUpperCase());}
			catch(IllegalArgumentException ex){
				sender.sendMessage(ChatColor.RED+"Invalid bow type\n"+ChatColor.GRAY+command.getUsage());
				return true;
			}
		}
		((Player)sender).getInventory().addItem(makeBow(type));
		sender.sendMessage(ChatColor.GOLD+"Bow:"+ChatColor.GRAY+type.name().toLowerCase()+ChatColor.GOLD+" created!");
		return true;
	}
}
