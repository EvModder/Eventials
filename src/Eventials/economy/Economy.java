package Eventials.economy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import EvLib.VaultHook;
import Eventials.Eventials;
import Eventials.economy.commands.*;
import Eventials.economy.listeners.*;
import Extras.PlayerMessageInterceptor;
import Extras.Text;

public class Economy extends ServerEconomy{
	final boolean useCurItem, updateBalsOnPayment;

	private static Economy eco; public static Economy getEconomy(){return eco;}
	private final Material currencyType; public Material getCurrency(){return currencyType;}

	public Economy(Eventials pl){
		super(pl, !pl.getConfig().getBoolean("track-server-balance", true),
				   pl.getConfig().getBoolean("track-global-balance", true),
				   (long) pl.getConfig().getDouble("starting-balance", 0),
				   Text.translateAlternateColorCodes('&', pl.getConfig().getString("currency-symbol", "&2L")));

		//Set defaults for ServerEconomy if applicable
		BigDecimal serverBal = BigDecimal.valueOf(pl.getConfig().getLong("starting-server-balance", 0));
		BigDecimal globalBal = BigDecimal.valueOf(pl.getConfig().getLong("starting-global-balance", serverBal.longValue()));
		if(serverBal.compareTo(globalBal) > 0) globalBal = serverBal;
		setIfZero(serverBal, globalBal);

		//Now into the custom Economy stuff
		eco = this;

		useCurItem = pl.getConfig().getBoolean("use-item-as-currency", true);
		updateBalsOnPayment = pl.getConfig().getBoolean("update-balance-on-paid-commands", true);
		currencyType = Material.getMaterial(pl.getConfig().getString("currency-item", "WATER_LILY").toUpperCase());

		if(pl.getConfig().getBoolean("economy-signs"))
			pl.getServer().getPluginManager().registerEvents(new EconomySignListener(), pl);
//		if(trackGlobalBal){
//			plugin.getServer().getPluginManager().registerEvents(new _UNUSED_ChunkPopulateListener(), plugin);
//			plugin.getServer().getPluginManager().registerEvents(new _UNUSED_PlayerFishingListener(), plugin);
//			plugin.getServer().getPluginManager().registerEvents(new _UNUSED_CurrencyLoseGainListener(), plugin);
//		}
		if(pl.getConfig().getBoolean("advancement-reward")){
			pl.getServer().getPluginManager().registerEvents(new AdvancementListener(), plugin);
		}
		if(pl.getConfig().getDouble("chunk-generate-cost", 0.0) != 0){
			pl.getServer().getPluginManager().registerEvents(new ChunkGenerateListener(), plugin);
		}

		new CommandAdvertise(pl, this);
		new CommandDeposit(pl, this, useCurItem);
		new CommandDonateTop(pl, this);
		new CommandMoneyOrder(pl, this, pl.getConfig().getBoolean("enable-moneyorders", false));
		new CommandWithdraw(pl, this, useCurItem);

		new BukkitRunnable(){@Override public void run(){loadPaidCommands();}}.runTaskLater(pl, 10*20);//10s
	}

	void loadPaidCommands(){
		for(final Entry<String, Object> e : plugin.getConfig().getConfigurationSection("paid-commands")
				.getValues(false).entrySet()){
			final PluginCommand cmd = plugin.getServer().getPluginCommand(e.getKey());
			if(cmd == null){
				plugin.getLogger().info("Unknown priced command: "+e.getKey());
				continue;
			}
			final CommandExecutor executor = cmd.getExecutor();
			cmd.setExecutor(new CommandExecutor(){
				@Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
					if(sender instanceof Player && !sender.hasPermission("eventials.bypass.paidcommands") &&
						!sender.hasPermission("eventials.bypass.paidcommands."+cmd.getName()))
					{
						plugin.getLogger().info("Priced command: "+cmd.getName());
						if(!VaultHook.hasAtLeast((Player)sender, (double)e.getValue())){
							sender.sendMessage(ChatColor.RED+"You do not have enough money to perform this command!\n"
									+ChatColor.GRAY+"Price: "+ChatColor.YELLOW+e.getValue()+ChatColor.DARK_GREEN+'L');
							return true;
						}
						if(args.length == 0 || (!args[args.length-1].equalsIgnoreCase("confirm")
								&& !args[args.length-1].equalsIgnoreCase("c")))
						{
							String preMsg = ChatColor.GRAY+"This command costs "
									+ChatColor.YELLOW+e.getValue()+CUR_SYMBOL+ChatColor.GRAY
									+". To "+ChatColor.BOLD+"CONFIRM"+ChatColor.GRAY
									+" this charge,\n"+ChatColor.GRAY+"run ";
							String cmdValue = "/"+label;
							if(args.length != 0) cmdValue += " "+StringUtils.join(args, " ");
							cmdValue += " confirm";
							String hyperMsg = ChatColor.DARK_GREEN+cmdValue;
							Text.sendModifiedText(preMsg, hyperMsg, Text.TextAction.CMD, cmdValue, "", (Player)sender);
							return true;
						}
						else{
							args = Arrays.copyOf(args, args.length-1);
							if(attemptPaidCommand(executor, cmd, (Player)sender, label, args)){
								playerToServer(((Player)sender).getUniqueId(), (double)e.getValue());
								sender.sendMessage(ChatColor.GRAY+"You paid "+ChatColor.YELLOW+e.getValue()
										+ChatColor.DARK_GREEN+'L'+ChatColor.GRAY+" to run "+ChatColor.WHITE+label);
								if(updateBalsOnPayment) updateBalance(((Player)sender).getUniqueId(), true);
							}
							else plugin.getLogger().info("Unsuccessful :C ");
							return true;
						}
					}
					else return executor.onCommand(sender, cmd, label, args);
				}
			});
		}
	}

	boolean attemptPaidCommand(CommandExecutor executor, Command cmd, final Player player, String label, String[] args){
		PlayerMessageInterceptor pmi = new PlayerMessageInterceptor(player);
		if(executor.onCommand(pmi, cmd, label, args)){
			for(String m : pmi.msgs)
				if(m.startsWith(ChatColor.RED+"") || m.startsWith(ChatColor.DARK_RED+"")
						|| m.contains("error") || m.contains("invalid") || m.contains("wrong")) return false;
			return true;
		}
		return false;
	}

	public static double getMoneyOrderValue(ItemStack mo){
		if(mo != null && mo.getType() == Material.PAPER && mo.hasItemMeta() && mo.getItemMeta().hasLore()
				&& mo.getItemMeta().getLore().get(0).contains("Stored balance:"))
		{
			return Double.parseDouble(ChatColor.stripColor(
					mo.getItemMeta().getLore().get(0).split(":")[1].replace("$", "").trim()));
		}
		else return 0;
	}
}