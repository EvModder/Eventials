package Eventials;

import java.util.Arrays;
import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import net.evmodder.EvLib.EvUtils;
import net.evmodder.EvLib.extras.NBTTagUtils;

class ScoreboardTracker{
	private final Eventials pl;
	private final HashSet<String> ADVANCEMENTS_COUNTED;

	ScoreboardTracker(final Eventials plugin){
		pl = plugin;
		if(pl.getConfig().getBoolean("add-scoreboards-for-vanilla-statistics", false)) createScoreboardsForAllVanillaStats();
		if(pl.getConfig().getBoolean("add-scoreboards-for-items-destroyed", false)) registerItemDestructionListeners();
		if(pl.getConfig().getBoolean("add-scoreboard-for-chats", false)) registerChatScoreboardListener();
		if(pl.getConfig().getBoolean("add-scoreboard-for-advancements", false)){
			ADVANCEMENTS_COUNTED = new HashSet<>(pl.getConfig().getStringList("advancements-counted"));
			registerAdvancementScoreboardListener();
		}
		else ADVANCEMENTS_COUNTED = null;
		if(pl.getConfig().getBoolean("add-scoreboards-for-horse-attributes", false)){
			try{
				Class.forName("net.evmodder.HorseOwners.api.events.HorseClaimEvent");
				pl.getServer().getPluginManager().registerEvents(new HorseScoardboardTracker(pl), pl);
			}
			catch(ClassNotFoundException e){}
		}
	}

	private String capitalizeAndSpacify(String str){
		StringBuilder builder = new StringBuilder("");
		boolean wordStart = true;
		for(final char c : str.toCharArray()){
			final char upperC = Character.toUpperCase(c);
			if(wordStart){builder.append(upperC); wordStart = false;}
			else if(upperC < 'A' || upperC > 'Z'){if(!wordStart) builder.append(' '); wordStart = true;}
			else if(c == upperC) builder.append(' ').append(c);
			else builder.append(c);
		}
		return builder.toString().replaceAll(" Cm$", " CM");
	}
	private void registerZstatObjective(String name, String criteria, String displayName){
		if(!displayName.startsWith("\"")) displayName = '"'+displayName+'"';
		final String cmd = new StringBuilder("scoreboard objectives add ")
				.append(name).append(' ').append(criteria).append(' ').append(displayName).toString();
		pl.runCommand(cmd);
//		pl.getLogger().info("ran cmd: "+cmd);
	}
	private void createScoreboardsForAllVanillaStats(){
		final Scoreboard board = pl.getServer().getScoreboardManager().getMainScoreboard();

		boolean needToRegister = true;
		try{board.registerNewObjective("zstats-dummy", "dummy", "Zstats Test");}
		catch(IllegalArgumentException ex){needToRegister = false;}
		if(needToRegister) new BukkitRunnable(){@Override public void run(){
			pl.getLogger().info("Registering all statistics as scoreboards (may take a minute)...");
	
			// Simple stats (excluding trigger & dummy)
			for(String simpleStat : Arrays.asList("air", "armor", "deathCount", "food", "health", "level", "playerKillCount", "totalKillCount", "xp")){
				registerZstatObjective("zstats-"+simpleStat, simpleStat, capitalizeAndSpacify(simpleStat));
			}
			// "minecraft.custom:minecraft.<custom_stat>"
			for(String customStat : Arrays.asList(
					"animals_bred", "aviate_one_cm", "bell_ring", "boat_one_cm", "clean_armor", "clean_banner", "clean_shulker_box",
					"climb_one_cm", "crouch_one_cm", "damage_absorbed", "damage_blocked_by_shield", "damage_dealt", "damage_dealt_absorbed",
					"damage_dealt_resisted", "damage_resisted", "damage_taken", "deaths", "drop", "eat_cake_slice", "enchant_item",
					"fall_one_cm", "fill_cauldron", "fish_caught", "fly_one_cm", "horse_one_cm", "inspect_dispenser", "inspect_dropper",
					"inspect_hoppers", "interact_with_anvil", "interact_with_beacon", "interact_with_blast_furnace", "interact_with_brewingstand",
					"interact_with_campfire", "interact_with_cartography_table", "interact_with_crafting_table", "interact_with_furnace",
					"interact_with_grindstone", "interact_with_lectern", "interact_with_loom", "interact_with_smithing_table", "interact_with_smoker",
					"interact_with_stonecutter", "jump", "leave_game", "minecart_one_cm", "mob_kills", "open_barrel", "open_chest", "open_enderchest",
					"open_shulker_box", "pig_one_cm", "play_noteblock", "play_record", "play_time", "player_kills", "pot_flower", "raid_trigger",
					"raid_win", "sleep_in_bed", "sneak_time", "sprint_one_cm", "strider_one_cm", "swim_one_cm", "talked_to_villager", "target_hit",
					"time_since_death", "time_since_rest", "total_world_time", "traded_with_villager", "trigger_trapped_chest", "tune_noteblock",
					"use_cauldron", "walk_on_water_one_cm", "walk_one_cm", "walk_under_water_one_cm"
			)){
				registerZstatObjective("zstats-"+customStat, "minecraft.custom:minecraft."+customStat, capitalizeAndSpacify(customStat));
			}
			// "killedByTeam.<color>", "teamKill.<color>"
			for(ChatColor color : ChatColor.values()){
				final String criteria1 = "killedByTeam."+color.getChar();
				registerZstatObjective("zstats-"+criteria1, criteria1, criteria1);
				final String criteria2 = "teamKill."+color.getChar();
				registerZstatObjective("zstats-"+criteria2, criteria2, criteria2);
			}
			for(Material mat : Material.values()){
				final String matNameL = mat.name().toLowerCase();
				if(mat.getMaxDurability() > 0){
					final String criteria = "minecraft.broken:minecraft."+matNameL;
					registerZstatObjective("zstats-"+criteria, criteria, capitalizeAndSpacify("Broken "+matNameL));
				}
				registerZstatObjective("zstats-dropped_"+matNameL, "minecraft.dropped:minecraft."+matNameL, capitalizeAndSpacify("Dropped "+matNameL));
				registerZstatObjective("zstats-pick_up_"+matNameL, "minecraft.picked_up:minecraft."+matNameL, capitalizeAndSpacify("Picked up "+matNameL));
				registerZstatObjective("zstats-used_"+matNameL, "minecraft.used:minecraft."+matNameL, capitalizeAndSpacify("Used "+matNameL));
				if(mat.isBlock()){
					registerZstatObjective("zstats-mined_"+matNameL, "minecraft.mined:minecraft."+matNameL, capitalizeAndSpacify("Mined "+matNameL));
				}
			}
			for(EntityType e : EntityType.values()){
				final String entNameL = e.name().toLowerCase();
				registerZstatObjective("zstats-killed_"+entNameL, "minecraft.killed:minecraft."+entNameL, capitalizeAndSpacify("Killed "+entNameL));
				registerZstatObjective("zstats-killed_by_"+entNameL, "minecraft.killed_by:minecraft."+entNameL, capitalizeAndSpacify("Killed by "+entNameL));
			}
			HashSet<Material> alreadyAdded = new HashSet<>();
			pl.getServer().recipeIterator().forEachRemaining(r -> {
				if(alreadyAdded.add(r.getResult().getType())){
					final String matNameLower = r.getResult().getType().name().toLowerCase();
					final String criteria = "minecraft.crafted:minecraft."+matNameLower;
					registerZstatObjective("zstats-crafted_"+matNameLower, criteria, capitalizeAndSpacify("Crafted "+matNameLower));
				}
			});
			pl.getLogger().info("Vanilla Zstats scoreboards registered");
		}}.runTaskLater(pl, 20*5);
	}

	private void registerItemDestructionListeners(){
		Scoreboard board = pl.getServer().getScoreboardManager().getMainScoreboard();
		try{
			board.registerNewObjective("istats-fire", "dummy", "Items lost to fire");
			board.registerNewObjective("istats-lava", "dummy", "Items lost to lava");
			board.registerNewObjective("istats-void", "dummy", "Items lost to void");
			board.registerNewObjective("istats-cactus", "dummy", "Items lost to cactus");
			board.registerNewObjective("istats-despawn", "dummy", "Items lost to despawn");
			board.registerNewObjective("istats-explosion", "dummy", "Items lost to explosion");
			board.registerNewObjective("istats-lightning", "dummy", "Items lost to lightning");
			board.registerNewObjective("istats-anvil", "dummy", "Items lost to anvil smush");
		}
		catch(IllegalArgumentException ex){}
		
		pl.getServer().getPluginManager().registerEvents(new Listener(){
			void incrDeathScore(String statName, ItemStack item){
				final String matNameL = item.getType().name().toLowerCase();
				Score newScoreObject = board.getObjective(statName).getScore(matNameL);
				newScoreObject.setScore((newScoreObject.isScoreSet() ? newScoreObject.getScore() : 0) + item.getAmount());
			}
			String getStatNameFromDamageCause(DamageCause cause){
				switch(cause){
					case CONTACT: return "istats-cactus";
					//NOTE: Sadly, items < y -63 instantly get deleted without any damage event
					//https://www.spigotmc.org/threads/detection-of-item-falls-into-the-void.282496/
					case VOID: return "istats-void";
					case LAVA: return "istats-lava";
					case FIRE: case FIRE_TICK: return "istats-fire";
					case ENTITY_EXPLOSION: case BLOCK_EXPLOSION: return "istats-explosion";
					case LIGHTNING: return "istats-lightning";
					case FALLING_BLOCK: return "istats-anvil";
					default: {
						pl.getLogger().severe("Unexpected damage cause for item entity: "+cause.name());
						return "istats-unknown";
					}
				}
			}
			{//"constructor"
				try{
					@SuppressWarnings("unchecked")
					Class<? extends Event> clazz = (Class<? extends Event>)
						Class.forName("com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent");
					pl.getServer().getPluginManager().registerEvent(clazz, this, EventPriority.MONITOR, new EventExecutor(){
						@Override public void execute(Listener listener, Event event){
							//pl.getLogger().info("entity remove from world event");
							Entity entity = ((EntityEvent)event).getEntity();
							if(entity instanceof Item && entity.getLocation().getY() <
									 (entity.getWorld().getEnvironment() == Environment.NORMAL ? -127 : -63)){
								pl.getLogger().info("item < critical y lvl: "+((Item)entity).getItemStack().getType());
								incrDeathScore("istats-void", ((Item)entity).getItemStack());
							}
						}
					}, pl);
				}
				catch(ClassNotFoundException e){}
			}
			@EventHandler(priority = EventPriority.MONITOR)
			public void itemDespawnEvent(ItemDespawnEvent evt){
				if(!evt.isCancelled()){
					pl.getLogger().info("Item Despawn: "+evt.getEntity().getLocation().toString());
					incrDeathScore("istats-despawn", evt.getEntity().getItemStack());
					evt.getEntity().remove();
				}
			}
			@EventHandler(priority = EventPriority.MONITOR)
			public void onItemMiscDamage(EntityDamageEvent evt){
				if(!evt.isCancelled() && evt.getEntity() instanceof Item){
					//pl.getLogger().info("Item damage event: "+evt.getCause().name()+" (item cur health: "+NBTTagUtils.getTag(evt.getEntity()).getShort("Health")+")");
					if(NBTTagUtils.getTag(evt.getEntity()).getShort("Health") <= evt.getFinalDamage()){
						incrDeathScore(getStatNameFromDamageCause(evt.getCause()), ((Item)evt.getEntity()).getItemStack());
						evt.getEntity().remove();
					}
				}
			}
		}, pl);
	}


	private void registerChatScoreboardListener(){
		try{registerZstatObjective("zstats-chats", "dummy", "Chats");}
		catch(IllegalArgumentException ex){}
		// Needs a custom listener:
		pl.getServer().getPluginManager().registerEvents(new Listener(){
			final Objective chatObjective = pl.getServer().getScoreboardManager().getMainScoreboard().getObjective("zstats-chats");
			@EventHandler public void onPlayerChat(AsyncPlayerChatEvent evt){
				final Score score = chatObjective.getScore(evt.getPlayer().getName());
				score.setScore((score.isScoreSet() ? score.getScore() : 0) + 1);
			}
		}, pl);
	}

	private boolean isCountedAdvancement(Advancement adv){
		final int i = adv.getKey().getKey().indexOf('/');
		return i != -1 && adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) 
				&& ADVANCEMENTS_COUNTED.contains(adv.getKey().getKey().substring(0, i));
	}
	private void registerAdvancementScoreboardListener(){
		try{registerZstatObjective("zstats-advancements", "dummy", "Advancements");}
		catch(IllegalArgumentException ex){}
		// Needs a custom listener:
		pl.getServer().getPluginManager().registerEvents(new Listener(){
			final Objective advObjective = pl.getServer().getScoreboardManager().getMainScoreboard().getObjective("zstats-advancements");
			@EventHandler public void onAdvancementGet(PlayerAdvancementDoneEvent evt){
				if(!isCountedAdvancement(evt.getAdvancement())/* || evt.getPlayer().getGameMode() == GameMode.SPECTATOR*/) return;
				final int advancements = EvUtils.getVanillaAdvancements(evt.getPlayer(), ADVANCEMENTS_COUNTED).size();
				advObjective.getScore(evt.getPlayer().getName()).setScore(advancements);
			}
		}, pl);
	}
}