package Extras;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

@SuppressWarnings("deprecation")
public class PlayerMessageInterceptor implements Player{
	Player player;
	public java.util.Vector<String> msgs;
	public PlayerMessageInterceptor(Player p){player = p; msgs = new java.util.Vector<String>();}

	@Override public void closeInventory(){player.closeInventory();}
	@Override public int getCooldown(Material m){return player.getCooldown(m);}
	@Override public Inventory getEnderChest(){return player.getEnderChest();}
	@Override public int getExpToLevel(){return player.getExpToLevel();}
	@Override public GameMode getGameMode(){return player.getGameMode();}
	@Override public PlayerInventory getInventory(){return player.getInventory();}
	@Override public ItemStack getItemInHand(){return player.getItemInHand();}
	@Override public ItemStack getItemOnCursor(){return player.getItemOnCursor();}
	@Override public MainHand getMainHand(){return player.getMainHand();}
	@Override public String getName(){return player.getName();}
	@Override public InventoryView getOpenInventory(){return player.getOpenInventory();}
	@Override public Entity getShoulderEntityLeft(){return player.getShoulderEntityLeft();}
	@Override public Entity getShoulderEntityRight(){return player.getShoulderEntityRight();}
	@Override public int getSleepTicks(){return player.getSleepTicks();}
	@Override public boolean hasCooldown(Material m){return player.hasCooldown(m);}
	@Override public boolean isBlocking(){return player.isBlocking();}
	@Override public boolean isHandRaised(){return player.isHandRaised();}
	@Override public boolean isSleeping(){return player.isSleeping();}
	@Override public InventoryView openEnchanting(Location loc, boolean b){return player.openEnchanting(loc, b);}
	@Override public InventoryView openInventory(Inventory i){return player.openInventory(i);}
	@Override public void openInventory(InventoryView iv){player.openInventory(iv);}
	@Override public InventoryView openMerchant(Villager v, boolean b){return player.openMerchant(v, b);}
	@Override public InventoryView openMerchant(Merchant m, boolean b){return player.openMerchant(m, b);}
	@Override public InventoryView openWorkbench(Location loc, boolean b){return player.openWorkbench(loc, b);}
	@Override public void setCooldown(Material m, int t){player.setCooldown(m, t);}
	@Override public void setGameMode(GameMode gm){player.setGameMode(gm);}
	@Override public void setItemInHand(ItemStack item){player.setItemInHand(item);}
	@Override public void setItemOnCursor(ItemStack item){player.setItemOnCursor(item);}
	@Override public void setShoulderEntityLeft(Entity e){player.setShoulderEntityLeft(e);}
	@Override public void setShoulderEntityRight(Entity e){player.setShoulderEntityRight(e);}
	@Override public boolean setWindowProperty(Property p, int s){return player.setWindowProperty(p, s);}
	@Override public boolean addPotionEffect(PotionEffect p){return player.addPotionEffect(p);}
	@Override public boolean addPotionEffect(PotionEffect p, boolean b){return player.addPotionEffect(p, b);}
	@Override public boolean addPotionEffects(Collection<PotionEffect> ps){return player.addPotionEffects(ps);}
	@Override public Collection<PotionEffect> getActivePotionEffects(){return player.getActivePotionEffects();}
	@Override public boolean getCanPickupItems(){return player.getCanPickupItems();}
	@Override public EntityEquipment getEquipment(){return player.getEquipment();}
	@Override public double getEyeHeight(){return player.getEyeHeight();}
	@Override public double getEyeHeight(boolean b){return player.getEyeHeight(b);}
	@Override public Location getEyeLocation(){return player.getEyeLocation();}
	@Override public Player getKiller(){return player.getKiller();}
	@Override public double getLastDamage(){return player.getLastDamage();}
	@Override public List<Block> getLastTwoTargetBlocks(Set<Material> s, int d)
			{return player.getLastTwoTargetBlocks(s, d);}
	@Override public Entity getLeashHolder() throws IllegalStateException{return player.getLeashHolder();}
	@Override public List<Block> getLineOfSight(Set<Material> s, int d){return player.getLineOfSight(s, d);}
	@Override public int getMaximumAir(){return player.getMaximumAir();}
	@Override public int getMaximumNoDamageTicks(){return player.getMaximumNoDamageTicks();}
	@Override public int getNoDamageTicks(){return player.getNoDamageTicks();}
	@Override public PotionEffect getPotionEffect(PotionEffectType pet){return player.getPotionEffect(pet);}
	@Override public int getRemainingAir(){return player.getRemainingAir();}
	@Override public boolean getRemoveWhenFarAway(){return player.getRemoveWhenFarAway();}
	@Override public Block getTargetBlock(Set<Material> s, int d){return player.getTargetBlock(s, d);}
	@Override public boolean hasAI(){return player.hasAI();}
	@Override public boolean hasLineOfSight(Entity e){return player.hasLineOfSight(e);}
	@Override public boolean hasPotionEffect(PotionEffectType pet){return player.hasPotionEffect(pet);}
	@Override public boolean isCollidable(){return player.isCollidable();}
	@Override public boolean isGliding(){return player.isGliding();}
	@Override public boolean isLeashed(){return player.isLeashed();}
	@Override public void removePotionEffect(PotionEffectType pet){player.removePotionEffect(pet);}
	@Override public void setAI(boolean b){player.setAI(b);}
	@Override public void setCanPickupItems(boolean b){player.setCanPickupItems(b);}
	@Override public void setCollidable(boolean b){player.setCollidable(b);}
	@Override public void setGliding(boolean b){player.setGliding(b);}
	@Override public void setLastDamage(double d){player.setLastDamage(d);}
	@Override public boolean setLeashHolder(Entity e){return player.setLeashHolder(e);}
	@Override public void setMaximumAir(int a){player.setMaximumAir(a);}
	@Override public void setMaximumNoDamageTicks(int t){player.setMaximumNoDamageTicks(t);}
	@Override public void setNoDamageTicks(int t){player.setNoDamageTicks(t);}
	@Override public void setRemainingAir(int a){player.setRemainingAir(a);}
	@Override public void setRemoveWhenFarAway(boolean b){player.setRemoveWhenFarAway(b);}
	@Override public AttributeInstance getAttribute(Attribute a){return player.getAttribute(a);}
	@Override public boolean addPassenger(Entity e){return player.addPassenger(e);}
	@Override public boolean addScoreboardTag(String s){return player.addScoreboardTag(s);}
	@Override public boolean eject(){return player.eject();}
	@Override public int getEntityId(){return player.getEntityId();}
	@Override public float getFallDistance(){return player.getFallDistance();}
	@Override public int getFireTicks(){return player.getFireTicks();}
	@Override public double getHeight(){return player.getHeight();}
	@Override public EntityDamageEvent getLastDamageCause(){return player.getLastDamageCause();}
	@Override public Location getLocation(){return player.getLocation();}
	@Override public Location getLocation(Location loc){return player.getLocation(loc);}
	@Override public int getMaxFireTicks(){return player.getMaxFireTicks();}
	@Override public List<Entity> getNearbyEntities(double x, double y, double z)
			{return player.getNearbyEntities(x, y, z);}
	@Override public Entity getPassenger(){return player.getPassenger();}
	@Override public List<Entity> getPassengers(){return player.getPassengers();}
	@Override public PistonMoveReaction getPistonMoveReaction(){return player.getPistonMoveReaction();}
	@Override public int getPortalCooldown(){return player.getPortalCooldown();}
	@Override public Set<String> getScoreboardTags(){return player.getScoreboardTags();}
	@Override public Server getServer(){return player.getServer();}
	@Override public int getTicksLived(){return player.getTicksLived();}
	@Override public EntityType getType(){return player.getType();}
	@Override public UUID getUniqueId(){return player.getUniqueId();}
	@Override public Entity getVehicle(){return player.getVehicle();}
	@Override public Vector getVelocity(){return player.getVelocity();}
	@Override public double getWidth(){return player.getWidth();}
	@Override public World getWorld(){return player.getWorld();}
	@Override public boolean hasGravity(){return player.hasGravity();}
	@Override public boolean isCustomNameVisible(){return player.isCustomNameVisible();}
	@Override public boolean isDead(){return player.isDead();}
	@Override public boolean isEmpty(){return player.isEmpty();}
	@Override public boolean isGlowing(){return player.isGlowing();}
	@Override public boolean isInsideVehicle(){return player.isInsideVehicle();}
	@Override public boolean isInvulnerable(){return player.isInvulnerable();}
	@Override public boolean isOnGround(){return player.isOnGround();}
	@Override public boolean isSilent(){return player.isSilent();}
	@Override public boolean isValid(){return player.isValid();}
	@Override public boolean leaveVehicle(){return player.leaveVehicle();}
	@Override public void playEffect(EntityEffect ee){player.playEffect(ee);}
	@Override public void remove(){player.remove();}
	@Override public boolean removePassenger(Entity e){return player.removePassenger(e);}
	@Override public boolean removeScoreboardTag(String s){return player.removeScoreboardTag(s);}
	@Override public void setCustomNameVisible(boolean b){player.setCustomNameVisible(b);}
	@Override public void setFallDistance(float d){player.setFallDistance(d);}
	@Override public void setFireTicks(int t){player.setFireTicks(t);}
	@Override public void setGlowing(boolean b){player.setGlowing(b);}
	@Override public void setGravity(boolean b){player.setGravity(b);}
	@Override public void setInvulnerable(boolean b){player.setInvulnerable(b);}
	@Override public void setLastDamageCause(EntityDamageEvent ede){player.setLastDamageCause(ede);}
	@Override public boolean setPassenger(Entity e){return player.setPassenger(e);}
	@Override public void setPortalCooldown(int c){player.setPortalCooldown(c);}
	@Override public void setSilent(boolean b){player.setSilent(b);}
	@Override public void setTicksLived(int t){player.setTicksLived(t);}
	@Override public void setVelocity(Vector v){player.setVelocity(v);}
	@Override public boolean teleport(Location loc){return player.teleport(loc);}
	@Override public boolean teleport(Entity e){return player.teleport(e);}
	@Override public boolean teleport(Location loc, TeleportCause tc){return player.teleport(loc, tc);}
	@Override public boolean teleport(Entity e, TeleportCause tc){return player.teleport(e, tc);}
	@Override public List<MetadataValue> getMetadata(String s){return player.getMetadata(s);}
	@Override public boolean hasMetadata(String s){return player.hasMetadata(s);}
	@Override public void removeMetadata(String s, Plugin p){player.removeMetadata(s, p);}
	@Override public void setMetadata(String s, MetadataValue mv){player.setMetadata(s, mv);}
	@Override public void sendMessage(String s){msgs.add(s); player.sendMessage(s);}
	@Override public void sendMessage(String[] ms){msgs.addAll(Arrays.asList(ms)); player.sendMessage(ms);}
	@Override public PermissionAttachment addAttachment(Plugin p){return player.addAttachment(p);}
	@Override public PermissionAttachment addAttachment(Plugin p, int a){return player.addAttachment(p, a);}
	@Override public PermissionAttachment addAttachment(Plugin p, String s, boolean b)
			{return player.addAttachment(p, s, b);}
	@Override public PermissionAttachment addAttachment(Plugin p, String s, boolean b, int a)
			{return player.addAttachment(p, s, b, a);}
	@Override public Set<PermissionAttachmentInfo> getEffectivePermissions()
			{return player.getEffectivePermissions();}
	@Override public boolean hasPermission(String s){return player.hasPermission(s);}
	@Override public boolean hasPermission(Permission p){return player.hasPermission(p);}
	@Override public boolean isPermissionSet(String s){return player.isPermissionSet(s);}
	@Override public boolean isPermissionSet(Permission p){return player.isPermissionSet(p);}
	@Override public void recalculatePermissions(){player.recalculatePermissions();}
	@Override public void removeAttachment(PermissionAttachment pa){player.removeAttachment(pa);}
	@Override public boolean isOp(){return player.isOp();}
	@Override public void setOp(boolean b){player.setOp(b);}
	@Override public String getCustomName(){return player.getCustomName();}
	@Override public void setCustomName(String s){player.setCustomName(s);}
	@Override public void damage(double d){player.damage(d);}
	@Override public void damage(double d, Entity e){player.damage(d, e);}
	@Override public double getHealth(){return player.getHealth();}
	@Override public double getMaxHealth(){return player.getMaxHealth();}
	@Override public void resetMaxHealth(){player.resetMaxHealth();}
	@Override public void setHealth(double h){player.setHealth(h);}
	@Override public void setMaxHealth(double h){player.setMaxHealth(h);}
	@Override public <T extends Projectile> T launchProjectile(Class<? extends T> p)
			{return player.launchProjectile(p);}
	@Override public <T extends Projectile> T launchProjectile(Class<? extends T> p, Vector v)
			{return player.launchProjectile(p, v);}
	@Override public void abandonConversation(Conversation c){player.abandonConversation(c);}
	@Override public void abandonConversation(Conversation c, ConversationAbandonedEvent cae)
			{player.abandonConversation(c, cae);}
	@Override public void acceptConversationInput(String s){player.acceptConversationInput(s);}
	@Override public boolean beginConversation(Conversation c){return player.beginConversation(c);}
	@Override public boolean isConversing(){return player.isConversing();}
	@Override public long getFirstPlayed(){return player.getFirstPlayed();}
	@Override public long getLastPlayed(){return player.getLastPlayed();}
	@Override public Player getPlayer(){return player.getPlayer();}
	@Override public boolean hasPlayedBefore(){return player.hasPlayedBefore();}
	@Override public boolean isBanned(){return player.isBanned();}
	@Override public boolean isOnline(){return player.isOnline();}
	@Override public boolean isWhitelisted(){return player.isWhitelisted();}
	@Override public void setWhitelisted(boolean b){player.setWhitelisted(b);}
	@Override public Map<String, Object> serialize(){return player.serialize();}
	@Override public Set<String> getListeningPluginChannels(){return player.getListeningPluginChannels();}
	@Override public void sendPluginMessage(Plugin p, String msg, byte[] b){player.sendPluginMessage(p, msg, b);}
	@Override public void awardAchievement(Achievement a){player.awardAchievement(a);}
	@Override public boolean canSee(Player p){return player.canSee(p);}
	@Override public void chat(String s){player.chat(s);}
	@Override public void decrementStatistic(Statistic s) throws IllegalArgumentException
			{player.decrementStatistic(s);}
	@Override public void decrementStatistic(Statistic s, int i) throws IllegalArgumentException
			{player.decrementStatistic(s, i);}
	@Override public void decrementStatistic(Statistic s, Material m) throws IllegalArgumentException
			{player.decrementStatistic(s, m);}
	@Override public void decrementStatistic(Statistic s, EntityType e) throws IllegalArgumentException
			{player.decrementStatistic(s, e);}
	@Override public void decrementStatistic(Statistic s, Material m, int i) throws IllegalArgumentException
			{player.decrementStatistic(s, m, i);}
	@Override public void decrementStatistic(Statistic s, EntityType e, int i)
			{player.decrementStatistic(s, e, i);}
	@Override public InetSocketAddress getAddress(){return player.getAddress();}
	@Override public AdvancementProgress getAdvancementProgress(Advancement a)
			{return player.getAdvancementProgress(a);}
	@Override public boolean getAllowFlight(){return player.getAllowFlight();}
	@Override public Location getBedSpawnLocation(){return player.getBedSpawnLocation();}
	@Override public Location getCompassTarget(){return player.getCompassTarget();}
	@Override public String getDisplayName(){return player.getDisplayName();}
	@Override public float getExhaustion(){return player.getExhaustion();}
	@Override public float getExp(){return player.getExp();}
	@Override public float getFlySpeed(){return player.getFlySpeed();}
	@Override public int getFoodLevel(){return player.getFoodLevel();}
	@Override public double getHealthScale(){return player.getHealthScale();}
	@Override public int getLevel(){return player.getLevel();}
	@Override public String getLocale(){return player.getLocale();}
	@Override public String getPlayerListName(){return player.getPlayerListName();}
	@Override public long getPlayerTime(){return player.getPlayerTime();}
	@Override public long getPlayerTimeOffset(){return player.getPlayerTimeOffset();}
	@Override public WeatherType getPlayerWeather(){return player.getPlayerWeather();}
	@Override public float getSaturation(){return player.getSaturation();}
	@Override public Scoreboard getScoreboard(){return player.getScoreboard();}
	@Override public Entity getSpectatorTarget(){return player.getSpectatorTarget();}
	@Override public int getStatistic(Statistic s) throws IllegalArgumentException
			{return player.getStatistic(s);}
	@Override public int getStatistic(Statistic s, Material m) throws IllegalArgumentException
			{return player.getStatistic(s, m);}
	@Override public int getStatistic(Statistic s, EntityType e) throws IllegalArgumentException
			{return player.getStatistic(s, e);}
	@Override public int getTotalExperience(){return player.getTotalExperience();}
	@Override public float getWalkSpeed(){return player.getWalkSpeed();}
	@Override public void giveExp(int e){player.giveExp(e);}
	@Override public void giveExpLevels(int l){player.giveExpLevels(l);}
	@Override public boolean hasAchievement(Achievement a){return player.hasAchievement(a);}
	@Override public void hidePlayer(Player p){player.hidePlayer(p);}
	@Override public void incrementStatistic(Statistic s) throws IllegalArgumentException
			{player.incrementStatistic(s);}
	@Override public void incrementStatistic(Statistic s, int i) throws IllegalArgumentException
			{player.incrementStatistic(s, i);}
	@Override public void incrementStatistic(Statistic s, Material m) throws IllegalArgumentException
			{player.incrementStatistic(s, m);}
	@Override public void incrementStatistic(Statistic s, EntityType e) throws IllegalArgumentException
			{player.incrementStatistic(s, e);}
	@Override public void incrementStatistic(Statistic s, Material m, int i) throws IllegalArgumentException
			{player.incrementStatistic(s, m, i);}
	@Override public void incrementStatistic(Statistic s, EntityType e, int i) throws IllegalArgumentException
			{player.incrementStatistic(s, e, i);}
	@Override public boolean isFlying(){return player.isFlying();}
	@Override public boolean isHealthScaled(){return player.isHealthScaled();}
	@Override public boolean isPlayerTimeRelative(){return player.isPlayerTimeRelative();}
	@Override public boolean isSleepingIgnored(){return player.isSleepingIgnored();}
	@Override public boolean isSneaking(){return player.isSneaking();}
	@Override public boolean isSprinting(){return player.isSprinting();}
	@Override public void kickPlayer(String msg){player.kickPlayer(msg);}
	@Override public void loadData(){player.loadData();}
	@Override public boolean performCommand(String cmd){return player.performCommand(cmd);}
	@Override public void playEffect(Location loc, Effect e, int i){player.playEffect(loc, e, i);}
	@Override public <T> void playEffect(Location loc, Effect e, T t){player.playEffect(loc, e, t);}
	@Override public void playNote(Location loc, byte i, byte note){player.playNote(loc, i, note);}
	@Override public void playNote(Location loc, Instrument i, Note note){player.playNote(loc, i, note);}
	@Override public void playSound(Location loc, Sound s, float vol, float pitch)
			{player.playSound(loc, s, vol, pitch);}
	@Override public void playSound(Location loc, String s, float vol, float pitch)
			{player.playSound(loc, s, vol, pitch);}
	@Override public void playSound(Location loc, Sound s, SoundCategory sc, float vol, float pitch)
			{player.playSound(loc, s, sc, vol, pitch);}
	@Override public void playSound(Location loc, String s, SoundCategory sc, float vol, float pitch)
			{player.playSound(loc, s, sc, vol, pitch);}
	@Override public void removeAchievement(Achievement a){player.removeAchievement(a);}
	@Override public void resetPlayerTime(){player.resetPlayerTime();}
	@Override public void resetPlayerWeather(){player.resetPlayerWeather();}
	@Override public void resetTitle(){player.resetTitle();}
	@Override public void saveData(){player.saveData();}
	@Override public void sendBlockChange(Location loc, Material m, byte b){player.sendBlockChange(loc, m, b);}
	@Override public boolean sendChunkChange(Location loc, int x, int y, int z, byte[] b)
			{return player.sendChunkChange(loc, x, y, z, b);}
	@Override public void sendMap(MapView mv){player.sendMap(mv);}
	@Override public void sendRawMessage(String msg){player.sendRawMessage(msg);}
	@Override public void sendSignChange(Location loc, String[] lines) throws IllegalArgumentException
			{player.sendSignChange(loc, lines);}
	@Override public void sendTitle(String title, String subtitle){player.sendTitle(title, subtitle);}
	@Override public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut)
			{player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);}
	@Override public void setAllowFlight(boolean b){player.setAllowFlight(b);}
	@Override public void setBedSpawnLocation(Location loc){player.setBedSpawnLocation(loc);}
	@Override public void setBedSpawnLocation(Location loc, boolean forst)
			{player.setBedSpawnLocation(loc, forst);}
	@Override public void setCompassTarget(Location loc){player.setCompassTarget(loc);}
	@Override public void setDisplayName(String name){player.setDisplayName(name);}
	@Override public void setExhaustion(float f){player.setExhaustion(f);}
	@Override public void setExp(float e){player.setExp(e);}
	@Override public void setFlySpeed(float s) throws IllegalArgumentException{player.setFallDistance(s);}
	@Override public void setFlying(boolean b){player.setFlying(b);}
	@Override public void setFoodLevel(int i){player.setFoodLevel(i);}
	@Override public void setHealthScale(double d) throws IllegalArgumentException{player.setHealthScale(d);}
	@Override public void setHealthScaled(boolean d){player.setHealthScaled(d);}
	@Override public void setLevel(int lvl){player.setLevel(lvl);}
	@Override public void setPlayerListName(String name){player.setPlayerListName(name);}
	@Override public void setPlayerTime(long time, boolean relative){player.setPlayerTime(time, relative);}
	@Override public void setPlayerWeather(WeatherType wt){player.setPlayerWeather(wt);}
	@Override public void setResourcePack(String pack){player.setResourcePack(pack);}
	@Override public void setResourcePack(String pack, byte[] b){player.setResourcePack(pack, b);}
	@Override public void setSaturation(float f){player.setSaturation(f);}
	@Override public void setScoreboard(Scoreboard s) throws IllegalArgumentException, IllegalStateException
			{player.setScoreboard(s);}
	@Override public void setSleepingIgnored(boolean b){player.setSleepingIgnored(b);}
	@Override public void setSneaking(boolean b){player.setSneaking(b);}
	@Override public void setSpectatorTarget(Entity e){player.setSpectatorTarget(e);}
	@Override public void setSprinting(boolean b){player.setSprinting(b);}
	@Override public void setStatistic(Statistic s, int i) throws IllegalArgumentException
			{player.setStatistic(s,  i);}
	@Override public void setStatistic(Statistic s, Material m, int i) throws IllegalArgumentException
			{player.setStatistic(s, m, i);}
	@Override public void setStatistic(Statistic s, EntityType et, int i){player.setStatistic(s, et, i);}
	@Override public void setTexturePack(String pack){player.setTexturePack(pack);}
	@Override public void setTotalExperience(int e){player.setTotalExperience(e);}
	@Override public void setWalkSpeed(float speed) throws IllegalArgumentException{player.setWalkSpeed(speed);}
	@Override public void showPlayer(Player p){player.showPlayer(p);}
	@Override public void spawnParticle(Particle p, Location loc, int num){player.spawnParticle(p, loc, num);}
	@Override public <T> void spawnParticle(Particle p, Location loc, int num, T t)
			{player.spawnParticle(p, loc, num, t);}
	@Override public void spawnParticle(Particle p, double x, double y, double z, int num)
			{player.spawnParticle(p, x, y, z, num);}
	@Override public <T> void spawnParticle(Particle p, double x, double y, double z, int num, T t)
			{player.spawnParticle(p, x, y, z, num, t);}
	@Override public void spawnParticle(Particle p, Location loc, int num, double offx, double offy, double offz)
			{player.spawnParticle(p, loc, num, offx, offy, offz);}
	@Override public <T> void spawnParticle(Particle p, Location loc, int num, double offx, double offy, double offz,
			T t){player.spawnParticle(p, loc, num, offx, offy, offz, t);}
	@Override public void spawnParticle(Particle p, Location loc, int num, double offx, double offy, double offz,
			double ex){player.spawnParticle(p, loc, num, offx, offy, offz, ex);}
	@Override public void spawnParticle(Particle p, double x, double y, double z, int num, double offx, double offy,
			double offz){player.spawnParticle(p, x, y, z, num, offx, offy, offz);}
	@Override public <T> void spawnParticle(Particle p, Location loc, int num, double offx, double offy, double offz,
			double ex, T t){player.spawnParticle(p, loc, num, offx, offy, offz, ex, t);}
	@Override public <T> void spawnParticle(Particle p, double x, double y, double z, int num, double offx, double offy,
			double offz, T t){player.spawnParticle(p, x, y, z, num, offx, offy, offz, t);}
	@Override public void spawnParticle(Particle p, double x, double y, double z, int num, double offx, double offy,
			double offz, double ex){player.spawnParticle(p, x, y, z, num, offx, offy, offz, ex);}
	@Override public <T> void spawnParticle(Particle p, double x, double y, double z, int num, double offx, double offy,
			double offz, double ex, T t){player.spawnParticle(p, x, y, z, num, offx, offy, offz, ex, t);}
	@Override public void stopSound(Sound s){player.stopSound(s);}
	@Override public void stopSound(String s){player.stopSound(s);}
	@Override public void stopSound(Sound s, SoundCategory sc){player.stopSound(s, sc);}
	@Override public void stopSound(String s, SoundCategory sc){player.stopSound(s, sc);}
	@Override public void updateInventory(){player.updateInventory();}
	@Override public boolean discoverRecipe(NamespacedKey k){return player.discoverRecipe(k);}
	@Override public int discoverRecipes(Collection<NamespacedKey> ks){return player.discoverRecipes(ks);}
	@Override public Location getBedLocation(){return player.getBedLocation();}
	@Override public boolean sleep(Location l, boolean b){return player.sleep(l, b);}
	@Override public boolean undiscoverRecipe(NamespacedKey k){return player.undiscoverRecipe(k);}
	@Override public int undiscoverRecipes(Collection<NamespacedKey> ks){return player.undiscoverRecipes(ks);}
	@Override public void wakeup(boolean b){player.wakeup(b);}
	@Override public Block getTargetBlockExact(int x){return player.getTargetBlockExact(x);}
	@Override public Block getTargetBlockExact(int x, FluidCollisionMode fm){return player.getTargetBlockExact(x, fm);}
	@Override public boolean isRiptiding(){return player.isRiptiding();}
	@Override public boolean isSwimming(){return player.isSwimming();}
	@Override public RayTraceResult rayTraceBlocks(double d){return player.rayTraceBlocks(d);}
	@Override public RayTraceResult rayTraceBlocks(double d, FluidCollisionMode fm){return player.rayTraceBlocks(d, fm);}
	@Override public void setSwimming(boolean b){player.setSwimming(b);}
	@Override public BoundingBox getBoundingBox(){return player.getBoundingBox();}
	@Override public BlockFace getFacing(){return player.getFacing();}
	@Override public boolean isPersistent(){return player.isPersistent();}
	@Override public void setPersistent(boolean b){player.setPersistent(b);}
	@Override public int getClientViewDistance(){return player.getClientViewDistance();}
	@Override public String getPlayerListFooter(){return player.getPlayerListFooter();}
	@Override public String getPlayerListHeader(){return player.getPlayerListHeader();}
	@Override public void hidePlayer(Plugin pl, Player p){player.hidePlayer(pl, p);}
	@Override public void sendBlockChange(Location loc, BlockData bd){player.sendBlockChange(loc, bd);}
	@Override public void setPlayerListFooter(String s){player.setPlayerListFooter(s);}
	@Override public void setPlayerListHeader(String s){player.setPlayerListHeader(s);}
	@Override public void setPlayerListHeaderFooter(String s, String s2){player.setPlayerListHeaderFooter(s, s2);}
	@Override public void showPlayer(Plugin pl, Player p){player.showPlayer(pl, p);}
	@Override public void updateCommands(){player.updateCommands();}
	@Override public Pose getPose(){return player.getPose();}
	@Override public void setRotation(float f1, float f2){player.setRotation(f1, f2);}
	@Override public PersistentDataContainer getPersistentDataContainer(){return player.getPersistentDataContainer();}
}