package Eventials.spawners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import EvLib.UsefulUtils;
import Eventials.Eventials;
import Extras.Text;

public class EvSpawner implements Listener {
	private Eventials plugin;
	private boolean requireSilk, noNBTContainers, noNBTCommandblock, dropMonsterEggBlocks, colorcodeCommandblock;

	public EvSpawner(){
		plugin = Eventials.getPlugin();
		requireSilk = plugin.getConfig().getBoolean("require-silktouch", true);
		noNBTContainers = !plugin.getConfig().getBoolean("allow-nbt-container-placement", true);
		noNBTCommandblock = !plugin.getConfig().getBoolean("allow-nbt-commandblock-placement", true);
		dropMonsterEggBlocks = plugin.getConfig().getBoolean("drop-monsteregg-blocks", true);
		colorcodeCommandblock = plugin.getConfig().getBoolean("allow-colorcodes-in-commandblock", true);

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		if(plugin.getConfig().getBoolean("feed-slimes", true))
			plugin.getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(), plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPlaceBlock(BlockPlaceEvent evt){
		if(evt.isCancelled() || evt.getItemInHand() == null) return;

		if(evt.getItemInHand().hasItemMeta() && evt.getItemInHand().getItemMeta() instanceof BlockStateMeta){
			BlockStateMeta meta = (BlockStateMeta) evt.getItemInHand().getItemMeta();
			if(meta.hasBlockState()){
				if(meta.getBlockState() instanceof CreatureSpawner){
					plugin.getLogger().info("Spawner placed");
					CreatureSpawner storedState = (CreatureSpawner) meta.getBlockState();
					CreatureSpawner blockState = (CreatureSpawner) evt.getBlockPlaced().getState();
					blockState.setSpawnedType(storedState.getSpawnedType());
					blockState.setDelay(storedState.getDelay());
					blockState.update();
				}
				else if(meta.getBlockState() instanceof InventoryHolder && !(meta.getBlockState() instanceof ShulkerBox)){
					if(noNBTContainers) return;
					final ItemStack[] invContents = ((InventoryHolder)meta.getBlockState()).getInventory()
							.getContents().clone();
					final Location location = evt.getBlock().getLocation();
					new BukkitRunnable(){@Override public void run(){
						BlockState state = location.getBlock().getState();
						if(state instanceof InventoryHolder){
							((InventoryHolder)state).getInventory().setContents(invContents);
							state.update();
						}
					}}.runTaskLater(plugin, 1);
				}
				else if(meta.getBlockState() instanceof CommandBlock
						&& evt.getPlayer().hasPermission("evp.evm.commandblockcolor")){
					if(noNBTCommandblock) return;
					String cmd = ((CommandBlock)meta.getBlockState()).getCommand();
					plugin.getLogger().info("Stored command: "+cmd);
					if(colorcodeCommandblock) cmd = Text.translateAlternateColorCodes('&', cmd);

					CommandBlock blockState = (CommandBlock) evt.getBlockPlaced().getState();
					blockState.setCommand(cmd);
					blockState.update();
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSpawnerMine(BlockBreakEvent evt){
		if(evt.isCancelled()) return;
		
		if(evt.getBlock().getType() == Material.MOB_SPAWNER
				&& (!requireSilk || (evt.getPlayer().getInventory().getItemInMainHand() != null
				&& evt.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))))
		{
			ItemStack item = new ItemStack(Material.MOB_SPAWNER);
			CreatureSpawner spawnerState = (CreatureSpawner) evt.getBlock().getState();
			BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
			meta.setBlockState(spawnerState);
			meta.setDisplayName(ChatColor.WHITE+UsefulUtils.getNormalizedName(spawnerState.getSpawnedType())+" Spawner");
			item.setItemMeta(meta);

			evt.setDropItems(true);
			evt.getBlock().getWorld().dropItemNaturally(evt.getBlock().getLocation(), item);
			evt.setExpToDrop(0);
		}
		else if(evt.getBlock().getType() == Material.MONSTER_EGGS && dropMonsterEggBlocks
				&& (!requireSilk || (evt.getPlayer().getInventory().getItemInMainHand() != null
				&& evt.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))))
		{
			evt.setDropItems(true);
			@SuppressWarnings("deprecation")
			ItemStack drop = new ItemStack(evt.getBlock().getType(), 1, evt.getBlock().getState().getRawData());
			evt.getBlock().getWorld().dropItemNaturally(evt.getBlock().getLocation(), drop);
		}
	}
}
