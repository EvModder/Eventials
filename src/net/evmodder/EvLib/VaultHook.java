package net.evmodder.EvLib;

import java.math.BigDecimal;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.earth2me.essentials.api.UserDoesNotExistException;

public class VaultHook {
	private static boolean vaultEnabled;
	public static Economy econ = null;
	public static Permission perms = null;
	public static Chat chat = null;

	public VaultHook(Plugin pl){
		if(!setupEconomy(pl)){
			if(pl.getServer().getPluginManager().getPlugin("Essentials") == null){
				pl.getLogger().warning("Unable to connect to Vault or EssentialsEco economies");
			}
			else{// Removed to reduce spam
//				plugin.getLogger().info("Vault not found, using EssentialsEco as economy base");
			}
		}
		else{
			vaultEnabled = true;
			setupPermissions(pl);
			setupChat(pl);
		}
	}

	private boolean setupEconomy(Plugin pl) {
		if(pl.getServer().getPluginManager().getPlugin("Vault") == null) return false;

		RegisteredServiceProvider<Economy> rsp = pl.getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null) return false;

		econ = rsp.getProvider();
		return econ != null;
	}

	private boolean setupChat(Plugin plugin) {
		RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
		chat = rsp.getProvider();
		return chat != null;
	}

	private boolean setupPermissions(Plugin plugin) {
		RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	public static double getBalance(OfflinePlayer p) {
		if(VaultHook.vaultEnabled()) return VaultHook.econ.getBalance(p);
		else{
			try{return EssEcoHook.getBalance(p);}
			catch(UserDoesNotExistException e){return 0D;}
		}
	}

	public static boolean hasAtLeast(OfflinePlayer p, double amount){
		if(VaultHook.vaultEnabled()) return VaultHook.econ.has(p, amount);
		else return EssEcoHook.hasAtLeast(p, amount);
	}

	@Deprecated //Note: Deprecated only to discourage use in preference of serverToPlayer()
	public static boolean giveMoney(OfflinePlayer p, double amount){
		if(p == null) return false;
		if(VaultHook.vaultEnabled()){
			EconomyResponse r = VaultHook.econ.depositPlayer(p, amount);
			return r.transactionSuccess();
		}
		else return EssEcoHook.giveMoney(p, amount);
	}
	@Deprecated //Note: Deprecated only to discourage use in preference of serverToPlayer()
	public static boolean giveMoney(OfflinePlayer p, BigDecimal amount){
		if(p == null) return false;
		if(VaultHook.vaultEnabled()){
			EconomyResponse r = VaultHook.econ.depositPlayer(p, amount.doubleValue());
			return r.transactionSuccess();
		}
		else return EssEcoHook.giveMoney(p, amount);
	}

	@Deprecated //Note: Deprecated only to discourage use in preference of playerToServer()
	public static boolean setMoney(OfflinePlayer p, double amount){
		if(p == null) return false;
		if(VaultHook.vaultEnabled()){
			amount -= VaultHook.econ.getBalance(p);
			if(amount < 0) return VaultHook.econ.withdrawPlayer(p, -amount).transactionSuccess();
			else if(amount > 0) return VaultHook.econ.depositPlayer(p, amount).transactionSuccess();
			else return true;
		}
		else return EssEcoHook.setMoney(p, amount);
	}

	@Deprecated //Note: Deprecated in preference of playerToServer()
	public static boolean chargeFee(OfflinePlayer p, double amount){
		if(VaultHook.vaultEnabled()){
			EconomyResponse r = VaultHook.econ.withdrawPlayer(p, amount);
			if(r.transactionSuccess() == false) return false;
		}
		else return EssEcoHook.chargeFee(p, amount);
		return true;
	}
	@Deprecated //Note: Deprecated in preference of playerToServer()
	public static boolean chargeFee(OfflinePlayer p, BigDecimal amount){
		if(VaultHook.vaultEnabled()){
			EconomyResponse r = VaultHook.econ.withdrawPlayer(p, amount.doubleValue());
			if(r.transactionSuccess() == false) return false;
		}
		else return EssEcoHook.chargeFee(p, amount);
		return true;
	}

	public static boolean vaultEnabled(){return vaultEnabled;}
}