package net.evmodder.EvLib2;

import java.util.HashMap;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib2.RefNBTTagList;
import net.evmodder.EvLib2.ReflectionUtils;
import net.evmodder.EvLib2.ReflectionUtils.RefClass;
import net.evmodder.EvLib2.ReflectionUtils.RefConstructor;
import net.evmodder.EvLib2.ReflectionUtils.RefMethod;

public class RefNBTTag implements RefNBTBase{// version = X1.0
	static final RefClass classItemStack = ReflectionUtils.getRefClass("{nms}.ItemStack");
	static final RefClass classNBTTagCompound = ReflectionUtils.getRefClass("{nms}.NBTTagCompound");
	static final RefClass classNBTBase = ReflectionUtils.getRefClass("{nms}.NBTBase"); 
	static final RefClass classCraftItemStack = ReflectionUtils.getRefClass("{cb}.inventory.CraftItemStack");
	static final RefMethod methodAsNMSCopy = classCraftItemStack.getMethod("asNMSCopy", ItemStack.class);
	static final RefMethod methodAsCraftMirror = classCraftItemStack.getMethod("asCraftMirror", classItemStack);
	static final RefMethod methodGetTag = classItemStack.getMethod("getTag");
	static final RefMethod methodSetTag = classItemStack.getMethod("setTag", classNBTTagCompound);
	static final RefMethod methodTagRemove = classNBTTagCompound.getMethod("remove", String.class);
	static final RefMethod methodTagIsEmpty = classNBTTagCompound.getMethod("isEmpty");
	static final HashMap<Class<?>, RefMethod> tagSetters = new HashMap<Class<?>, RefMethod>();
	static final HashMap<Class<?>, RefMethod> tagGetters = new HashMap<Class<?>, RefMethod>();
	static final Class<?> realNBTTagCompoundClass = classNBTTagCompound.getRealClass();
	static final Class<?> realNBTBaseClass = classNBTBase.getRealClass();
	static{
		tagSetters.put(realNBTBaseClass,classNBTTagCompound.getMethod("set",			String.class, classNBTBase));
		tagSetters.put(boolean.class,	classNBTTagCompound.getMethod("setBoolean",		String.class, boolean.class));
		tagSetters.put(byte.class,		classNBTTagCompound.getMethod("setByte",		String.class, byte.class));
		tagSetters.put(byte[].class,	classNBTTagCompound.getMethod("setByteArray",	String.class, byte[].class));
		tagSetters.put(double.class,	classNBTTagCompound.getMethod("setDouble",		String.class, double.class));
		tagSetters.put(float.class,		classNBTTagCompound.getMethod("setFloat",		String.class, float.class));
		tagSetters.put(int.class,		classNBTTagCompound.getMethod("setInt",			String.class, int.class));
		tagSetters.put(int[].class,		classNBTTagCompound.getMethod("setIntArray",	String.class, int[].class));
		tagSetters.put(long.class,		classNBTTagCompound.getMethod("setLong",		String.class, long.class));
		tagSetters.put(short.class,		classNBTTagCompound.getMethod("setShort",		String.class, short.class));
		tagSetters.put(String.class,	classNBTTagCompound.getMethod("setString",		String.class, String.class));
	}
	static{
		tagGetters.put(realNBTBaseClass,classNBTTagCompound.getMethod("get",			String.class));
		tagGetters.put(boolean.class,	classNBTTagCompound.getMethod("getBoolean",		String.class));
		tagGetters.put(byte.class,		classNBTTagCompound.getMethod("getByte",		String.class));
		tagGetters.put(byte[].class,	classNBTTagCompound.getMethod("getByteArray",	String.class));
		tagGetters.put(double.class,	classNBTTagCompound.getMethod("getDouble",		String.class));
		tagGetters.put(float.class,		classNBTTagCompound.getMethod("getFloat",		String.class));
		tagGetters.put(int.class,		classNBTTagCompound.getMethod("getInt",			String.class));
		tagGetters.put(int[].class,		classNBTTagCompound.getMethod("getIntArray",	String.class));
		tagGetters.put(long.class,		classNBTTagCompound.getMethod("getLong",		String.class));
		tagGetters.put(short.class,		classNBTTagCompound.getMethod("getShort",		String.class));
		tagGetters.put(String.class,	classNBTTagCompound.getMethod("getString",		String.class));
	}

	static final RefConstructor cnstrNBTTagCompound = classNBTTagCompound.findConstructor(0);

	Object nmsTag;
	public RefNBTTag(){nmsTag = cnstrNBTTagCompound.create();}
	public RefNBTTag(RefNBTTag base){nmsTag = base;};
	RefNBTTag(Object nmsTag){this.nmsTag = nmsTag;}
	private void addToTag(String key, Object value, Class<?> type) {tagSetters.get(type).of(nmsTag).call(key, value);}
	private Object getFromTag(String key, Class<?> type) {return tagGetters.get(type).of(nmsTag).call(key);}

	public void set(String key, RefNBTTag value){addToTag(key, value.nmsTag, realNBTBaseClass);}
	public void set(String key, RefNBTTagList value){addToTag(key, value.nmsTagList, realNBTBaseClass);}
	public void setBoolean	(String key, boolean		value){addToTag(key, value, boolean.class);}
	public void setByte		(String key, byte			value){addToTag(key, value, byte.class);}
	public void setByteArray(String key, byte[]			value){addToTag(key, value, byte[].class);}
	public void setDouble	(String key, double			value){addToTag(key, value, double.class);}
	public void setFloat	(String key, float			value){addToTag(key, value, float.class);}
	public void setInt		(String key, int			value){addToTag(key, value, int.class);}
	public void setIntArray	(String key, int[]			value){addToTag(key, value, int[].class);}
	public void setLong		(String key, long			value){addToTag(key, value, long.class);}
	public void setShort	(String key, short			value){addToTag(key, value, short.class);}
	public void setString	(String key, String			value){addToTag(key, value, String.class);}
	//
	public RefNBTBase get(String key){
		Object value = getFromTag(key, realNBTBaseClass);
		if(value == null) return null;
		return value.getClass().equals(realNBTTagCompoundClass) ? new RefNBTTag(value) : new RefNBTTagList(value);
	}
	public boolean getBoolean	(String key){return (boolean)	getFromTag(key, boolean.class);}
	public byte getByte			(String key){return (byte)		getFromTag(key, byte.class);}
	public byte[] getByteArray	(String key){return (byte[])	getFromTag(key, byte[].class);}
	public double getDouble		(String key){return (double)	getFromTag(key, double.class);}
	public float getFloat		(String key){return (float)		getFromTag(key, float.class);}
	public int getInt			(String key){return (int)		getFromTag(key, int.class);}
	public int[] getIntArray	(String key){return (int[])		getFromTag(key, int[].class);}
	public long getLong			(String key){return (long)		getFromTag(key, long.class);}
	public short getShort		(String key){return (short)		getFromTag(key, short.class);}
	public String getString		(String key){return (String)	getFromTag(key, String.class);}
	//
	public void remove(String key){methodTagRemove.of(nmsTag).call(key);}

	// For ItemStacks ----------------------------------------------------
	public static ItemStack setTag(ItemStack item, RefNBTTag tag){
		Object nmsTag = (tag == null || methodTagIsEmpty.of(tag.nmsTag).call().equals(true)) ? null : tag.nmsTag;
		Object nmsItem = methodAsNMSCopy.of(null).call(item);
		methodSetTag.of(nmsItem).call(nmsTag);
		item = (ItemStack) methodAsCraftMirror.of(null).call(nmsItem);
		return item;
	}
	public static RefNBTTag getTag(ItemStack item){
		Object nmsItem = methodAsNMSCopy.of(null).call(item);
		Object nmsTag = methodGetTag.of(nmsItem).call();
		return nmsTag == null ? new RefNBTTag() : new RefNBTTag(nmsTag);
	};
}