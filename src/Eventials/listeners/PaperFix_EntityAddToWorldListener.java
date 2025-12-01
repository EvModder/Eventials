package Eventials.listeners;

import java.lang.reflect.Field;
import java.util.Random;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.plugin.EventExecutor;
import Eventials.Eventials;
import net.evmodder.EvLib.bukkit.ReflectionUtils;
import net.evmodder.EvLib.bukkit.ReflectionUtils.RefClass;
import net.evmodder.EvLib.bukkit.ReflectionUtils.RefMethod;

public class PaperFix_EntityAddToWorldListener{
	static private Object SHARED_RANDOM;
	static private RefMethod methodGetHandle, methodSetSeed;
	static private RefClass rdmSrcClass;
	static private RefMethod makeRdmSrc;
	static private Field rdmField;

	public PaperFix_EntityAddToWorldListener(Eventials pl){
		try{
			final RefClass entityClass = ReflectionUtils.getRefClass("{nm}.world.entity.Entity");
			// These two should fail on non-Paper
			SHARED_RANDOM = entityClass.getRealClass().getField("SHARED_RANDOM").get(null);
			@SuppressWarnings("unchecked")
			Class<? extends Event> clazz = (Class<? extends Event>)Class.forName("com.destroystokyo.paper.event.entity.EntityAddToWorldEvent");

			// Initialize these in advance
			methodGetHandle = ReflectionUtils.getRefClass("{cb}.entity.CraftEntity").getMethod("getHandle");
			try{
				rdmSrcClass = ReflectionUtils.getRefClass("{nm}.util.RandomSource");
				makeRdmSrc = rdmSrcClass.findMethod(/*isStatic=*/true, rdmSrcClass);
			}
			catch(RuntimeException e){rdmSrcClass = null; makeRdmSrc = null;}
			final RefClass rdmClass = rdmSrcClass == null ? ReflectionUtils.getRefClass(Random.class) : rdmSrcClass;
			methodSetSeed = rdmClass.findMethod(/*isStatic=*/false, Void.TYPE, long.class);
			rdmField = entityClass.findField(rdmClass, /*isStatic=*/false, /*isPublic=*/true).getRealField();
			rdmField.setAccessible(true);

			pl.getServer().getPluginManager().registerEvent(clazz, new Listener(){}, EventPriority.MONITOR, new EventExecutor(){
				@Override public void execute(Listener listener, Event event){
					final Entity entity = ((EntityEvent)event).getEntity();
					final Object nmsEntity = methodGetHandle.of(entity).call();
					try{
						if(rdmField.get(nmsEntity) != SHARED_RANDOM
								|| (entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.SQUID)
						) return;
						final Object rdmObj = (rdmSrcClass == null ? new Random() : makeRdmSrc.call());
						if(entity.getType() == EntityType.SQUID) methodSetSeed.of(rdmObj).call((long)entity.getEntityId());
						rdmField.set(nmsEntity, rdmObj);
					}
					catch(IllegalArgumentException | IllegalAccessException e){e.printStackTrace();}
				}
			}, pl);
		}
		catch(NoSuchFieldException e){/*no SHARED_RANDOM => not running paper*/}
		catch(RuntimeException | IllegalAccessException | ClassNotFoundException e){e.printStackTrace();}
	}
}
