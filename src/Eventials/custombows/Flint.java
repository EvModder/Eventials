package Eventials.custombows;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

public class Flint{
	static void doPower(ProjectileSource shooter, ItemStack bow, Arrow arrow){
		arrow.setVelocity(arrow.getVelocity().multiply(2));
	}

	public static ItemStack makeBow(){
		ItemStack item = new ItemStack(Material.BOW);

		net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		NBTTagList atributeModifiers = new NBTTagList();//Attribute list
		if(nmsItem.getTag() == null) nmsItem.setTag(new NBTTagCompound());

		//----------------------- Attack attribute -----------------------
		NBTTagCompound attribute = new NBTTagCompound();
		attribute.setString("Slot", "mainhand");
		attribute.setString("AttributeName", "generic.attackDamage");
		attribute.setString("Name", "generic.attackDamage");
		attribute.setInt("Amount", 4);
		attribute.setInt("Operation", 0);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		atributeModifiers.add(attribute);
		//----------------------- Moving attribute -----------------------
		attribute = new NBTTagCompound();
		attribute.setString("AttributeName", "generic.movementSpeed");
		attribute.setString("Name", "generic.movementSpeed");
		attribute.setFloat("Amount", .05F);//5% increase
		attribute.setInt("Operation", 1);
		attribute.setInt("UUIDLeast", 1);
		attribute.setInt("UUIDMost", 1);
		atributeModifiers.add(attribute);
		//----------------------------------------------------------------
		nmsItem.getTag().set("AttributeModifiers", atributeModifiers);
		nmsItem.getTag().setInt("Unbreakable", 1);
		nmsItem.getTag().setInt("Flint", 1);
		nmsItem.getTag().setInt("Age", -32768);
		item = CraftItemStack.asCraftMirror(nmsItem);

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GRAY+""+ChatColor.BOLD+"Flint");
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
	}
}
