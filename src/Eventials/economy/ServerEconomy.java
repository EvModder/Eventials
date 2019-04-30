package Eventials.economy;

import java.math.BigDecimal;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;
import Eventials.economy.commands.CommandServerBal;
import net.evmodder.EvLib2.FileIO;
import net.evmodder.EvLib2.VaultHook;
import Eventials.economy.commands.CommandDonateServer;
import Eventials.economy.commands.CommandGlobalBal;

public abstract class ServerEconomy extends BalanceTracker{
	public final boolean infServerBal, trackGlobalBal;
	private BigDecimal serverBal, globalBal;
	protected JavaPlugin plugin;

//	public ServerEconomy(JavaPlugin pl, BigDecimal serverBal, BigDecimal globalBal,
//				boolean infServer, boolean trackGlobal, long startBal, String curSymbol){
//		this(pl, infServer, trackGlobal, startBal, curSymbol);
//		if(this.serverBal == BigDecimal.ZERO) this.serverBal = serverBal;
//		if(this.globalBal == BigDecimal.ZERO) this.globalBal = globalBal;
//	}
	public ServerEconomy(JavaPlugin pl, boolean infServer, boolean trackGlobal, long startBal, String curSymbol){
		super(startBal, curSymbol);
		plugin = pl;
		loadServerBalances();
		infServerBal = infServer;
		trackGlobalBal = trackGlobal;
		new CommandServerBal(pl, this, !infServer);
		new CommandGlobalBal(pl, this, trackGlobal);
		new CommandDonateServer(pl, this, !infServer);
		if(!infServer) pl.getLogger().fine("Loaded server balance: "+serverBal);
		if(trackGlobal)pl.getLogger().fine("Loaded global balance: "+globalBal);
	}
	protected void setIfZero(BigDecimal serverBal, BigDecimal globalBal){
		if(this.serverBal == BigDecimal.ZERO) this.serverBal = serverBal;
		if(this.globalBal == BigDecimal.ZERO) this.globalBal = globalBal;
	}

	private void loadServerBalances(){
		for(String line : FileIO.loadFile("ecostats.txt", "").split("\n")){
			String[] parts = line.split(":");
			if(parts.length == 2){
				parts[0] = parts[0].trim().toLowerCase();
				parts[1] = parts[1].trim();
				if(parts[0].equals("server-balance")) serverBal = new BigDecimal(parts[1]);
				else if(parts[0].equals("global-balance")) globalBal = new BigDecimal(parts[1]);
				else if(parts[0].equals("ad-expires-on")) plugin.getConfig().set("ad-expires-on", Long.parseLong(parts[1]));
			}
		}
		if(serverBal == null) serverBal = BigDecimal.ZERO;
		if(globalBal == null) globalBal = BigDecimal.ZERO;
	}

	public void onDisable(){
		savePlayerBalances();
		StringBuilder builder = new StringBuilder();
		builder.append("server-balance: ").append(serverBal).append("\nglobal-balance: ").append(globalBal);
		long EXPIRES = plugin.getConfig().getLong("ad-expires-on");
		if(EXPIRES != 0) builder.append("\nad-expires-on: ").append(EXPIRES);

		FileIO.saveFile("ecostats.txt", builder.toString());
	}

	public boolean payServer(BigDecimal amt){return chargeServer(amt.negate());}
	public boolean chargeServer(BigDecimal amt){
		serverBal = serverBal.subtract(amt);
		if(infServerBal || amt.compareTo(BigDecimal.ZERO) <= 0
						|| serverBal.compareTo(BigDecimal.ZERO) >= 0) return true;
		else{
			serverBal = serverBal.add(amt);
			return false;
		}
	}
	public boolean payServer(double amt){return chargeServer(-amt);};
	public boolean chargeServer(double amt){return chargeServer(BigDecimal.valueOf(amt));}
	public BigDecimal getServerBal(){return serverBal;}

	public void addGlobalBal(BigDecimal amt){globalBal = globalBal.add(amt);}
	public void addGlobalBal(double amt){globalBal = globalBal.add(BigDecimal.valueOf(amt));}
	public BigDecimal getGlobalBal(){return globalBal;}

	@SuppressWarnings("deprecation")
	public boolean playerToServer(UUID pUUID, BigDecimal amount){
		if(amount.compareTo(BigDecimal.ZERO) < 0) return serverToPlayer(pUUID, amount.negate());//TODO: remove .doubleValue();
		if(!VaultHook.chargeFee(plugin.getServer().getOfflinePlayer(pUUID), amount.doubleValue())) return false;
		chargeServer(amount.negate());
		updateBalance(pUUID, true);
		return true;
	}

	@SuppressWarnings("deprecation")
	public boolean serverToPlayer(UUID pUUID, BigDecimal amount){
		if(amount.compareTo(BigDecimal.ZERO) < 0) return playerToServer(pUUID, amount.negate());
		if(!chargeServer(amount)) return false;
		if(!VaultHook.giveMoney(plugin.getServer().getOfflinePlayer(pUUID), amount.doubleValue())){//TODO: remove .doubleValue();
			chargeServer(amount.negate());
			return false;
		}
		updateBalance(pUUID, true);
		return true;
	}

	public boolean playerToServer(UUID pUUID, double amount){
		return playerToServer(pUUID, BigDecimal.valueOf(amount));
	}

	public boolean serverToPlayer(UUID pUUID, double amount){
		return serverToPlayer(pUUID, BigDecimal.valueOf(amount));
	}
}