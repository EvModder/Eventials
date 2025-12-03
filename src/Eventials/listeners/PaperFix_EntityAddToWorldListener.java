package Eventials.listeners;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import net.evmodder.EvLib.util.ReflectionUtils;

public class PaperFix_EntityAddToWorldListener{
	static private Object SHARED_RANDOM;
	static private Method method_CraftEntity_getHandle, method_Random_setSeed;
	static private Class<?> classRandomSource;
	static private Method method_RandomSource_create;
	static private Field field_Entity_random;

	public PaperFix_EntityAddToWorldListener(JavaPlugin pl){
		try{
			final Class<?> classEntity = ReflectionUtils.getClass("{nm}.world.entity.Entity");
			// These two should fail on non-Paper
			SHARED_RANDOM = ReflectionUtils.getStatic(classEntity.getField("SHARED_RANDOM"));
			@SuppressWarnings("unchecked")
			Class<? extends Event> clazz = (Class<? extends Event>)ReflectionUtils.getClass("com.destroystokyo.paper.event.entity.EntityAddToWorldEvent");

			// Initialize these in advance
			method_CraftEntity_getHandle = ReflectionUtils.getMethod(ReflectionUtils.getClass("{cb}.entity.CraftEntity"), "getHandle");
			try{
				classRandomSource = ReflectionUtils.getClass("{nm}.util.RandomSource");
				method_RandomSource_create = ReflectionUtils.findMethod(classRandomSource, /*isStatic=*/true, classRandomSource);
			}
			catch(RuntimeException e){classRandomSource = null; method_RandomSource_create = null;}
			final Class<?> classRandom = classRandomSource == null ? Random.class : classRandomSource;
			method_Random_setSeed = ReflectionUtils.findMethod(classRandom, /*isStatic=*/false, Void.TYPE, long.class);
			field_Entity_random = ReflectionUtils.findField(classEntity, classRandom, /*isStatic=*/false, /*isPublic=*/true);
			field_Entity_random.setAccessible(true);

			pl.getServer().getPluginManager().registerEvent(clazz, new Listener(){}, EventPriority.MONITOR, new EventExecutor(){
				@Override public void execute(Listener listener, Event event){
					final Entity entity = ((EntityEvent)event).getEntity();
					final Object nmsEntity = ReflectionUtils.call(method_CraftEntity_getHandle, entity);
					try{
						if(field_Entity_random.get(nmsEntity) != SHARED_RANDOM
								|| (entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.SQUID)) return;
						final Object rdmObj = (classRandomSource == null ? new Random() : ReflectionUtils.callStatic(method_RandomSource_create));
						if(entity.getType() == EntityType.SQUID) ReflectionUtils.call(method_Random_setSeed, rdmObj, (long)entity.getEntityId());
						field_Entity_random.set(nmsEntity, rdmObj);
					}
					catch(IllegalArgumentException | IllegalAccessException e){e.printStackTrace();}
				}
			}, pl);
		}
		catch(NoSuchFieldException e){/*no SHARED_RANDOM => not running paper*/}
		catch(RuntimeException e){e.printStackTrace();}
	}
}
