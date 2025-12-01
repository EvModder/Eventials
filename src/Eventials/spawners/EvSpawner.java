package Eventials.spawners;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;
import Eventials.Eventials;
import net.evmodder.EvLib.bukkit.NBTTagUtils;
import net.evmodder.EvLib.bukkit.NBTTagUtils.RefNBTTagCompound;
import net.evmodder.EvLib.TextUtils;
import net.evmodder.EvLib.bukkit.TypeUtils;

public class EvSpawner implements Listener {
	final Random rand;
	private Eventials plugin;
	private final CreatureSpawner BASE_STATE;
	final boolean requireSilk, stackableSpawners, dropMonsterEggBlocks,
				noNBTContainers, noNBTCommandblock, colorcodeCommandblock;

	public EvSpawner(Eventials pl){
		plugin = pl;
		requireSilk = plugin.getConfig().getBoolean("require-silktouch", true);
		dropMonsterEggBlocks = plugin.getConfig().getBoolean("drop-monsteregg-blocks", true);
		noNBTContainers = !plugin.getConfig().getBoolean("allow-nbt-container-placement", true);
		noNBTCommandblock = !plugin.getConfig().getBoolean("allow-nbt-commandblock-placement", true);
		colorcodeCommandblock = plugin.getConfig().getBoolean("allow-colorcodes-in-commandblock", true);
		stackableSpawners = plugin.getConfig().getBoolean("stackable-spawners", true);
		if(stackableSpawners){
			Block baseBlock = Bukkit.getWorlds().get(0).getBlockAt(67, 0, -43);
			BlockState baseState = baseBlock.getState();
			baseBlock.setType(Material.SPAWNER);
			BASE_STATE = (CreatureSpawner) baseBlock.getState();
			baseState.update(true, false);
			rand = null;
		}
		else{
			BASE_STATE = null;
			rand = new Random();
		}

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		if(plugin.getConfig().getBoolean("feed-slimes", true) || plugin.getConfig().getBoolean("dye-shulkers", true))
			plugin.getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(), plugin);
	}

	public static void CopySpawnerState(CreatureSpawner from, CreatureSpawner to){
		to.setRequiredPlayerRange(from.getRequiredPlayerRange());
		to.setMaxNearbyEntities(from.getMaxNearbyEntities());
		to.setSpawnedType(from.getSpawnedType());
		to.setSpawnRange(from.getSpawnRange());
		if(from.getMinSpawnDelay() > 0) to.setMinSpawnDelay(from.getMinSpawnDelay());
		if(from.getMaxSpawnDelay() > from.getMinSpawnDelay()) to.setMaxSpawnDelay(from.getMaxSpawnDelay());
		to.setDelay(from.getDelay());
		to.setSpawnCount(from.getSpawnCount());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPlaceBlock(BlockPlaceEvent evt){
		if(evt.isCancelled() || TypeUtils.isShulkerBox(evt.getBlockPlaced().getType())) return;

		if(evt.getItemInHand().hasItemMeta() && evt.getItemInHand().getItemMeta() instanceof BlockStateMeta){
			BlockStateMeta meta = (BlockStateMeta) evt.getItemInHand().getItemMeta();
			if(meta.hasBlockState()){
				if(meta.getBlockState() instanceof CreatureSpawner){
					plugin.getLogger().info("Spawner placed");
					CreatureSpawner blockState = (CreatureSpawner) evt.getBlockPlaced().getState();
					CopySpawnerState((CreatureSpawner)meta.getBlockState(), blockState);
					blockState.update();
				}
				else if(meta.getBlockState() instanceof InventoryHolder && !(meta.getBlockState() instanceof ShulkerBox)){
					plugin.getLogger().info("Placed (non-shulker) InventoryHolder block");
					if(noNBTContainers) return;
					final ItemStack[] invContents = ((InventoryHolder)meta.getBlockState()).getInventory()
							.getContents().clone();
					final Location location = evt.getBlock().getLocation();
					new BukkitRunnable(){@Override public void run(){
						BlockState state = location.getBlock().getState();
						if(!evt.isCancelled() && state.getType() == meta.getBlockState().getType()){
							((InventoryHolder)state).getInventory().setContents(invContents);
							state.update(true, false);
							plugin.getLogger().info("Updated InventoryHolder contents");
						}
					}}.runTaskLater(plugin, 1);
				}
				else if(meta.getBlockState() instanceof CommandBlock
						&& evt.getPlayer().hasPermission("evp.evm.commandblockcolor")){
					plugin.getLogger().info("Placed CommandBlock");
					if(noNBTCommandblock) return;
					String cmd = ((CommandBlock)meta.getBlockState()).getCommand();
					plugin.getLogger().info("Stored command: "+cmd);
					if(colorcodeCommandblock) cmd = TextUtils.translateAlternateColorCodes('&', cmd);
					plugin.getLogger().info("New command: " + cmd);

					CommandBlock blockState = (CommandBlock) evt.getBlockPlaced().getState();
					blockState.setCommand(cmd);
					blockState.update(true, false);
				}
			}
		}
	}

	@SuppressWarnings("deprecation") @EventHandler(priority = EventPriority.MONITOR)
	public void onSpawnerMine(BlockBreakEvent evt){
		if(evt.isCancelled()) return;

		if(evt.getBlock().getType() == Material.SPAWNER
				&& (!requireSilk || (evt.getPlayer().getInventory().getItemInMainHand() != null
				&& evt.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))))
		{
			CreatureSpawner spawnerState = (CreatureSpawner) evt.getBlock().getState();

			ItemStack item = new ItemStack(Material.SPAWNER);
			BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE+TextUtils.getNormalizedName(BASE_STATE.getSpawnedType())+" Spawner");
			if(stackableSpawners){
				CopySpawnerState(spawnerState, BASE_STATE);
				meta.setBlockState(BASE_STATE);
				item.setItemMeta(meta);
			}
			else{
				meta.setBlockState(spawnerState);
				item.setItemMeta(meta);
				RefNBTTagCompound tag = new RefNBTTagCompound();
				tag.setInt("ev_spawner_key", rand.nextInt());
				item = NBTTagUtils.setTag(item, tag);
			}

			evt.setExpToDrop(0);
			evt.setDropItems(false);
			evt.getBlock().getWorld().dropItemNaturally(evt.getBlock().getLocation(), item);
		}
		else if(TypeUtils.isInfested(evt.getBlock().getType()) && dropMonsterEggBlocks
				&& (!requireSilk || (evt.getPlayer().getInventory().getItemInMainHand() != null
				&& evt.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))))
		{
			evt.setExpToDrop(0);
			evt.setDropItems(false);
			ItemStack item = new ItemStack(evt.getBlock().getType());
			evt.getBlock().getWorld().dropItemNaturally(evt.getBlock().getLocation(), item);
		}
	}
}