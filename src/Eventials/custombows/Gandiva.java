package Eventials.custombows;

import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagList;

import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Gandiva{
	public static ItemStack makeBow(){
		ItemStack item = new ItemStack(Material.BOW);

		net.minecraft.server.v1_13_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		NBTTagList atributeModifiers = new NBTTagList();//Attribute list
		if(nmsItem.getTag() == null) nmsItem.setTag(new NBTTagCompound());

		//----------------------- Attack attribute -----------------------
		NBTTagCompound attribute = new NBTTagCompound();
		attribute.setString("Slot", "mainhand");
		attribute.setString("AttributeName", "generic.attackDamage");
		attribute.setString("Name", "generic.attackDamage");
		attribute.setInt("Amount", 2);
		attribute.setInt("Operation", 0);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		atributeModifiers.add(attribute);
		//----------------------- Knockb attribute -----------------------
		attribute = new NBTTagCompound();
		attribute.setString("AttributeName", "generic.knockbackResistance");
		attribute.setString("Name", "generic.knockbackResistance");
		attribute.setFloat("Amount", .25F);
		attribute.setInt("Operation", 1);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		atributeModifiers.add(attribute);
		//----------------------- Moving attribute -----------------------
		attribute = new NBTTagCompound();
		attribute.setString("AttributeName", "generic.movementSpeed");
		attribute.setString("Name", "generic.movementSpeed");
		attribute.setFloat("Amount", -.05F);//5% decrease
		attribute.setInt("Operation", 1);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		atributeModifiers.add(attribute);
		//----------------------------------------------------------------
		nmsItem.getTag().set("AttributeModifiers", atributeModifiers);
		nmsItem.getTag().setInt("Unbreakable", 1);
		nmsItem.getTag().setInt("Gandiva", 1);
		nmsItem.getTag().setInt("Age", -32768);
		item = CraftItemStack.asCraftMirror(nmsItem);

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
