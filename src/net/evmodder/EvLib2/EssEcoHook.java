package net.evmodder.EvLib2;

import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

public class EssEcoHook {
	public static double getBalance(OfflinePlayer p) throws UserDoesNotExistException {
		return net.ess3.api.Economy.getMoneyExact(p.getName()).doubleValue();
	}

	public static boolean hasAtLeast(OfflinePlayer p, double amount){
		try{return net.ess3.api.Economy.hasEnough(p.getName(), new BigDecimal(amount));}
		catch(UserDoesNotExistException e){return false;}
	}

	@Deprecated //Note: Deprecated only to discourage use in preference of serverToPlayer()
	public static boolean giveMoney(OfflinePlayer p, double amount){
		if(p == null) return false;
		try{net.ess3.api.Economy.add(p.getName(), new BigDecimal(amount));}
		// returns false if it encounters an error
		catch(NoLoanPermittedException e){return false;}
		catch(UserDoesNotExistException e){return false;}
		return true;
	}
	@Deprecated //Note: Deprecated only to discourage use in preference of serverToPlayer()
	public static boolean giveMoney(OfflinePlayer p, BigDecimal amount){
		if(p == null) return false;
		try{net.ess3.api.Economy.add(p.getName(), amount);}
		// returns false if it encounters an error
		catch(NoLoanPermittedException e){return false;}
		catch(UserDoesNotExistException e){return false;}
		return true;
	}

	@Deprecated //Note: Deprecated only to discourage use in preference of playerToServer()
	public static boolean setMoney(OfflinePlayer p, double amount){
		if(p == null) return false;
		try{net.ess3.api.Economy.setMoney(p.getName(), new BigDecimal(amount));}
		// returns false if it encounters an error
		catch(NoLoanPermittedException e){
			Bukkit.getLogger().warning(ChatColor.RED+"EssEcoHook: Failure in setMoney() - NoLoanPermittedException");
			return false;
		}
		catch(UserDoesNotExistException e){
			Bukkit.getLogger().warning(ChatColor.RED+"EssEcoHook: Failure in setMoney() - UserDoesNotExistException");
			return false;
		}
		return true;
	}

	@Deprecated //Note: Deprecated in preference of playerToServer()
	public static boolean chargeFee(OfflinePlayer p, double amount){
		// check money
		try{if(net.ess3.api.Economy.hasEnough(p.getName(), new BigDecimal(amount)) == false) return false;}
		catch(UserDoesNotExistException e){return false;}

		// take money
		try{net.ess3.api.Economy.substract(p.getName(), new BigDecimal(amount));}
		// returns false if it encounters an error
		catch(NoLoanPermittedException e){return false;}
		catch(UserDoesNotExistException e){return false;}
		return true;
	}

	@Deprecated //Note: Deprecated in preference of playerToServer()
	public static boolean chargeFee(OfflinePlayer p, BigDecimal amount){
		// check money
		try{if(net.ess3.api.Economy.hasEnough(p.getName(), amount) == false) return false;}
		catch(UserDoesNotExistException e){return false;}

		// take money
		try{net.ess3.api.Economy.substract(p.getName(), amount);}
		// returns false if it encounters an error
		catch(NoLoanPermittedException e){return false;}
		catch(UserDoesNotExistException e){return false;}
		return true;
	}
}