package Eventials.custombows;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.evmodder.EvLib.RefNBTTag;
import net.evmodder.EvLib.RefNBTTagList;

public class Gandiva{
	public static ItemStack makeBow(){
		RefNBTTagList attributeModifiers = new RefNBTTagList();
		//----------------------- Attack attribute -----------------------
		RefNBTTag attribute = new RefNBTTag();
		attribute.setString("Slot", "mainhand");
		attribute.setString("AttributeName", "generic.attackDamage");
		attribute.setString("Name", "generic.attackDamage");
		attribute.setInt("Amount", 2);
		attribute.setInt("Operation", 0);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		attributeModifiers.add(attribute);
		//----------------------- Knockb attribute -----------------------
		attribute = new RefNBTTag();
		attribute.setString("AttributeName", "generic.knockbackResistance");
		attribute.setString("Name", "generic.knockbackResistance");
		attribute.setFloat("Amount", .25F);
		attribute.setInt("Operation", 1);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		attributeModifiers.add(attribute);
		//----------------------- Moving attribute -----------------------
		attribute = new RefNBTTag();
		attribute.setString("AttributeName", "generic.movementSpeed");
		attribute.setString("Name", "generic.movementSpeed");
		attribute.setFloat("Amount", -.05F);//5% decrease
		attribute.setInt("Operation", 1);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		attributeModifiers.add(attribute);
		//----------------------------------------------------------------
		RefNBTTag tag = new RefNBTTag();
		tag.set("AttributeModifiers", attributeModifiers);
		tag.setInt("Unbreakable", 1);
		tag.setInt("Gandiva", 1);
		tag.setInt("Age", -32768);

		ItemStack item = new ItemStack(Material.BOW);
		item = RefNBTTag.setTag(item, tag);

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY+"Gandiva");
		meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
		meta.addEnchant(Enchantment.ARROW_FIRE, 2, true);
		meta.addEnchant(Enchantment.ARROW_DAMAGE, 8, true);
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
	}
}
