package Eventials.custombows;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import net.evmodder.EvLib.extras.RefNBTTag;
import net.evmodder.EvLib.extras.RefNBTTagList;

public class Flint{
	static void doPower(ProjectileSource shooter, ItemStack bow, Arrow arrow){
		arrow.setVelocity(arrow.getVelocity().multiply(2));
	}

	public static ItemStack makeBow(){
		RefNBTTag tag = new RefNBTTag();
		RefNBTTagList attributeModifiers = new RefNBTTagList();
		//----------------------- Attack attribute -----------------------
		RefNBTTag attribute = new RefNBTTag();
		attribute.setString("Slot", "mainhand");
		attribute.setString("AttributeName", "generic.attackDamage");
		attribute.setString("Name", "generic.attackDamage");
		attribute.setInt("Amount", 4);
		attribute.setInt("Operation", 0);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		attributeModifiers.add(attribute);
		//----------------------- Moving attribute -----------------------
		attribute = new RefNBTTag();
		attribute.setString("AttributeName", "generic.movementSpeed");
		attribute.setString("Name", "generic.movementSpeed");
		attribute.setFloat("Amount", .05F);//5% increase
		attribute.setInt("Operation", 1);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		attributeModifiers.add(attribute);
		//----------------------------------------------------------------
		tag.set("AttributeModifiers", attributeModifiers);
		tag.setInt("Unbreakable", 1);
		tag.setInt("Flint", 1);
		tag.setInt("Age", -32768);

		ItemStack item = new ItemStack(Material.BOW);
		item = RefNBTTag.setTag(item, tag);

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GRAY+""+ChatColor.BOLD+"Flint");
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
	}
}
