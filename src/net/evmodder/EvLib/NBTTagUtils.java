package net.evmodder.EvLib;

import java.util.HashMap;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib.ReflectionUtils;
import net.evmodder.EvLib.ReflectionUtils.RefClass;
import net.evmodder.EvLib.ReflectionUtils.RefConstructor;
import net.evmodder.EvLib.ReflectionUtils.RefMethod;

public class NBTTagUtils{// version = X1.0
	static final RefClass cItemStack = ReflectionUtils.getRefClass("{nms}.ItemStack");
	static final RefClass cNBTTagCompound = ReflectionUtils.getRefClass("{nms}.NBTTagCompound");
	static final RefClass classCraftItemStack = ReflectionUtils.getRefClass("{cb}.inventory.CraftItemStack");
	static final RefMethod methodAsNMSCopy = classCraftItemStack.getMethod("asNMSCopy", ItemStack.class);
	static final RefMethod methodAsCraftMirror = classCraftItemStack.getMethod("asCraftMirror", cItemStack);
	static final RefMethod methodGetTag = cItemStack.getMethod("getTag");
	static final RefMethod methodSetTag = cItemStack.getMethod("setTag", cNBTTagCompound);
	static final RefMethod methodTagRemove = cNBTTagCompound.getMethod("remove", String.class);
	static final RefMethod methodTagIsEmpty = cNBTTagCompound.getMethod("isEmpty");
	static final HashMap<Class<?>, RefMethod> tagSetters = new HashMap<Class<?>, RefMethod>();
	static{
		tagSetters.put(cNBTTagCompound.getRealClass(), cNBTTagCompound.getMethod("set", String.class, Object.class));
		tagSetters.put(String.class,	cNBTTagCompound.getMethod("setString",		String.class, String.class));
		tagSetters.put(boolean.class,	cNBTTagCompound.getMethod("setBoolean",		String.class, boolean.class));
		tagSetters.put(int.class,		cNBTTagCompound.getMethod("setInt",			String.class, int.class));
		tagSetters.put(int[].class,		cNBTTagCompound.getMethod("setIntArray",	String.class, int[].class));
		tagSetters.put(byte.class,		cNBTTagCompound.getMethod("setByte",		String.class, byte.class));
		tagSetters.put(byte[].class,	cNBTTagCompound.getMethod("setByteArray",	String.class, byte[].class));
		tagSetters.put(short.class,		cNBTTagCompound.getMethod("setShort",		String.class, short.class));
		tagSetters.put(long.class,		cNBTTagCompound.getMethod("setLong",		String.class, long.class));
		tagSetters.put(float.class,		cNBTTagCompound.getMethod("setFloat",		String.class, float.class));
		tagSetters.put(double.class,	cNBTTagCompound.getMethod("setDouble",		String.class, double.class));
	}
	//
	static final RefConstructor cnstrNBTTagCompound = cNBTTagCompound.findConstructor(0);
	public static Object newNBTTag(){return cnstrNBTTagCompound.create();}
	//
	public static ItemStack addNBTTag(ItemStack item, String key, Object value, RefMethod methodSetX){
		Object nmsItem = methodAsNMSCopy.of(null).call(item);
		if(methodGetTag.of(nmsItem).call() == null) methodSetTag.of(nmsItem).call(newNBTTag());
		methodSetX.of(methodGetTag.of(nmsItem).call()).call(key, value);
		item = (ItemStack) methodAsCraftMirror.of(null).call(nmsItem);
		return item;
	}
	public static ItemStack addNBTTag(ItemStack item, String key, Object value){
		return addNBTTag(item, key, value, tagSetters.get(value.getClass()));
	}
	public static ItemStack addNBTTag(ItemStack item, String key, String value){
		return addNBTTag(item, key, value, tagSetters.get(String.class));
	}
	public static ItemStack addNBTTag(ItemStack item, String key, boolean value){
		return addNBTTag(item, key, value, tagSetters.get(boolean.class));
	}
	public static ItemStack addNBTTag(ItemStack item, String key, int value){
		return addNBTTag(item, key, value, tagSetters.get(int.class));
	}
	public static ItemStack addNBTTag(ItemStack item, String key, int[] value){
		return addNBTTag(item, key, value, tagSetters.get(int[].class));
	}
	public static ItemStack addNBTTag(ItemStack item, String key, byte value){
		return addNBTTag(item, key, value, tagSetters.get(byte.class));
	}
	public static ItemStack addNBTTag(ItemStack item, String key, byte[] value){
		return addNBTTag(item, key, value, tagSetters.get(byte[].class));
	}
	public static ItemStack addNBTTag(ItemStack item, String key, short value){
		return addNBTTag(item, key, value, tagSetters.get(short.class));
	}
	public static ItemStack addNBTTag(ItemStack item, String key, long value){
		return addNBTTag(item, key, value, tagSetters.get(long.class));
	}
	public static ItemStack addNBTTag(ItemStack item, String key, float value){
		return addNBTTag(item, key, value, tagSetters.get(float.class));
	}
	public static ItemStack addNBTTag(ItemStack item, String key, double value){
		return addNBTTag(item, key, value, tagSetters.get(double.class));
	}

	public static ItemStack removeNBTTag(ItemStack item, String key){
		Object nmsItem = methodAsNMSCopy.of(null).call(item);
		Object tag = methodGetTag.of(nmsItem).call();
		if(tag == null) return item;
		methodTagRemove.of(tag).call(key);
		if(methodTagIsEmpty.of(tag).call().equals(true)) tag = null;
		methodSetTag.of(nmsItem).call(tag);
		return (item = (ItemStack) methodAsCraftMirror.of(null).call(nmsItem));
	}
}