package Eventials.economy;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.Vector;
import java.util.function.Predicate;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.evmodder.EvLib.hooks.EssEcoHook;
import net.evmodder.EvLib.FileIO;

public abstract class BalanceTracker{
	private Vector<PlayerBalance> bals;
	private Vector<PlayerBalance> donators;
	private final long startingBal;
	public final String CUR_SYMBOL;
	final int BALS_PER_PAGE = 9;

	class PlayerBalance{
		String name;
		UUID uuid;
		long balance, donated=0;

		PlayerBalance(String pName, UUID pUUID, long bal/*, long donate*/){
			name = pName;
			uuid = pUUID;
			balance = bal;
//			donated = donate;
		}

		@Override
		public boolean equals(Object obj){
			if(obj instanceof PlayerBalance){
				return uuid.equals(((PlayerBalance)obj).uuid) && name.equals(((PlayerBalance)obj).name);
			}
			else if(obj instanceof UUID) return uuid.equals(obj);
			else if(obj instanceof String) return name.equals(obj);
			else return false;
		}
	}

	public BalanceTracker(long startingBal, String currencySymbol){
		this.startingBal = startingBal;
		CUR_SYMBOL = currencySymbol;
		loadPlayerBalances();
	}

	public void loadPlayerBalances(){
		String[] lines = FileIO.loadFile("baltops.txt", "").split("\n");
		bals = new Vector<PlayerBalance>(lines.length);
		donators = new Vector<PlayerBalance>();
		for(String line : lines){
			String[] parts = line.split(":");
			if(parts.length >= 2){
				try{
					bals.add(new PlayerBalance(parts[0], UUID.fromString(parts[1]), Long.parseLong(parts[2])));
					if(parts.length == 4){
						bals.lastElement().donated = Long.parseLong(parts[3]);
						donators.add(bals.lastElement());
					}
				}
				catch(NumberFormatException ex){}
				catch(ArrayIndexOutOfBoundsException ex){}
			}
		}
	}
	
	public void savePlayerBalances(){
		if(!bals.isEmpty()){
			StringBuilder builder = new StringBuilder();
			for(PlayerBalance pb : bals){
				builder.append('\n').append(pb.name).append(':').append(pb.uuid).append(':').append(pb.balance);
				if(pb.donated != 0) builder.append(':').append(pb.donated);
			}
			FileIO.saveFile("baltops.txt", builder.substring(1));
		}
	}

	public Collection<PlayerBalance> getBalances(int start, int end){
		if(start >= end || start >= bals.size()) return new Vector<PlayerBalance>(0);
		Vector<PlayerBalance> target = new Vector<PlayerBalance>(end - start);

		if(end > bals.size()) end = bals.size();
		for(int i = start; i < end; ++i) target.add(bals.get(i));
		return target;
	}

	public Collection<PlayerBalance> getDonations(int start, int end){
		if(start >= end || start >= donators.size()) return new Vector<PlayerBalance>(0);
		Vector<PlayerBalance> target = new Vector<PlayerBalance>(end - start);

		if(end > donators.size()) end = donators.size();
		for(int i = start; i < end; ++i) target.add(donators.get(i));
		return target;
	}

	public void sortBalances(){
		Collections.sort(bals, new Comparator<PlayerBalance>() {
			@Override
			public int compare(PlayerBalance pb1, PlayerBalance pb2) {
				if(pb1.balance > pb2.balance) return -1;
				else if(pb1.balance < pb2.balance) return 1;
				else if(pb1.donated > pb2.donated) return -1;
				else if(pb1.donated < pb2.donated) return 1;
				else return 0;
			}
		});
	}

	public void sortDonations(){
		Collections.sort(donators, new Comparator<PlayerBalance>() {
			@Override
			public int compare(PlayerBalance pb1, PlayerBalance pb2) {
				if(pb1.donated > pb2.donated) return -1;
				else if(pb1.donated < pb2.donated) return 1;
				else if(pb1.balance < pb2.balance) return -1;
				else if(pb1.balance > pb2.balance) return 1;
				else return 0;
			}
		});
	}

	public void updateBalance(final UUID pUUID, boolean isOnline){
		OfflinePlayer p = org.bukkit.Bukkit.getServer().getOfflinePlayer(pUUID);
		long currentBal;
		try{currentBal = (long) EssEcoHook.getBalance(p);}
		catch(Exception e){currentBal = startingBal;}

		for(PlayerBalance pb : bals) if(pUUID.equals(pb.uuid)){
			pb.balance = currentBal;
			pb.name = p.getName();
			return;
		}
		if(isOnline || currentBal > startingBal){
			bals.add(new PlayerBalance(p.getName(), pUUID, currentBal));
		}
		// If they are offline and have below the starting, remove them
		else{
			bals.removeIf(new Predicate<PlayerBalance>(){
				@Override public boolean test(PlayerBalance pb){return pUUID.equals(pb.uuid);}});
		}
	}

	public void removeBalance(final UUID pUUID){
		bals.removeIf(new Predicate<PlayerBalance>(){
			@Override public boolean test(PlayerBalance pb){return pUUID.equals(pb.uuid);}});
	}

	public PlayerBalance getPlayerBalance(UUID pUUID,/* long donations,*/ boolean createIfDoesNotExists){
		for(PlayerBalance pb : bals) if(pUUID.equals(pb.uuid)) return pb;//TODO: Make not O(n)
		if(createIfDoesNotExists){
			OfflinePlayer p = org.bukkit.Bukkit.getServer().getOfflinePlayer(pUUID);
			if(p != null){
				long currentBal;
				try{currentBal = (long) EssEcoHook.getBalance(p);}
				catch(Exception e){currentBal = startingBal;}
				bals.add(new PlayerBalance(p.getName(), pUUID, currentBal));
//				if(donations > 0) bals.lastElement().donated = donations;
				return bals.lastElement();
			}
		}
		return null;
	}
	public PlayerBalance getPlayerBalance(UUID pUUID){return getPlayerBalance(pUUID, /*0,*/ true);}

	public void addDonatedAmount(UUID pUUID, long amount){
		PlayerBalance pb = getPlayerBalance(pUUID);
		if(pb != null){
			pb.donated += amount;
			if(pb.donated == amount){
				donators.add(pb);
			}
			else for(PlayerBalance pb2 : bals) if(pUUID.equals(pb2.uuid)){//TODO: Make not O(n)
				pb2.donated = pb.donated;
				return;
			}
		}
	}

	public int numBaltopPages(){return (bals.size()-1+BALS_PER_PAGE)/BALS_PER_PAGE;}
	public void showBaltop(CommandSender sender, int page){//page count starts at 1
		for(Player p : sender.getServer().getOnlinePlayers()) updateBalance(p.getUniqueId(), true);
		sortBalances();

		int startIdx;
		if(page < 0 && sender instanceof Player){
			startIdx = (bals.indexOf(getPlayerBalance(((Player)sender).getUniqueId()))/BALS_PER_PAGE)*BALS_PER_PAGE;
		}
		else{
			startIdx = (page - 1) * BALS_PER_PAGE;
			if(startIdx < 0) startIdx = 0;
		}

		int idx = startIdx + 1;

		int nextDigit = 10;
		while(nextDigit <= idx) nextDigit *= 10;
		boolean hasNextDigit = nextDigit - idx < BALS_PER_PAGE;

		StringBuilder builder = new StringBuilder("").append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH)
				.append("------------------------------------------------\n");

		int pages = (bals.size()-1+BALS_PER_PAGE)/BALS_PER_PAGE;
		builder.append(ChatColor.GOLD).append("Page ").append(ChatColor.RED)
			   .append(startIdx/BALS_PER_PAGE + 1).append(ChatColor.GOLD).append(" of ").append(ChatColor.RED)
			   .append(pages).append(ChatColor.GOLD).append(".\n");

		for(PlayerBalance bal : getBalances(startIdx, startIdx+BALS_PER_PAGE)){
			if(hasNextDigit && idx < nextDigit) builder.append(ChatColor.DARK_GRAY).append("¨¨");
			builder.append(ChatColor.GRAY).append(idx).append(ChatColor.DARK_GRAY).append(". ")
			.append(CUR_SYMBOL).append(ChatColor.YELLOW).append(bal.balance).append(ChatColor.AQUA).append(" - ")
			.append(sender.getName().equals(bal.name) ? ChatColor.WHITE : ChatColor.GREEN).append(bal.name)
			.append('\n');
			++idx;
		}

		sender.sendMessage(builder.toString());
	}

	public int numDonatetopPages(){return (donators.size()-1+BALS_PER_PAGE)/BALS_PER_PAGE;}
	public void showDonatetop(CommandSender sender, int page){//page count starts at 1
		sortDonations();

		int startIdx;
		if(page < 0 && sender instanceof Player){
			PlayerBalance pb = getPlayerBalance(((Player)sender).getUniqueId());
			startIdx = (pb == null ? 0 : donators.indexOf(pb)/BALS_PER_PAGE);
		}
		else{
			startIdx = (page - 1) * BALS_PER_PAGE;
			if(startIdx < 0) startIdx = 0;
		}

		int idx = startIdx + 1;

		int nextDigit = 10;
		while(nextDigit <= idx) nextDigit *= 10;
		boolean hasNextDigit = nextDigit - idx < BALS_PER_PAGE;

		StringBuilder builder = new StringBuilder("").append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH)
				.append("------------------------------------------------\n");

		int pages = (donators.size()-1+BALS_PER_PAGE)/BALS_PER_PAGE;
		builder.append(ChatColor.GOLD).append("Page ").append(ChatColor.RED)
			   .append(startIdx/BALS_PER_PAGE + 1).append(ChatColor.GOLD).append(" of ").append(ChatColor.RED)
			   .append(pages).append(ChatColor.GOLD).append(".\n");

		for(PlayerBalance bal : getDonations(startIdx, startIdx+BALS_PER_PAGE)){
			if(hasNextDigit && idx < nextDigit) builder.append(ChatColor.DARK_GRAY).append("¨¨");
			builder.append(ChatColor.GRAY).append(idx).append(ChatColor.DARK_GRAY).append(". ")
			.append(CUR_SYMBOL).append(ChatColor.YELLOW).append(bal.donated).append(ChatColor.AQUA).append(" - ")
			.append(sender.getName().equals(bal.name) ? ChatColor.WHITE : ChatColor.GREEN).append(bal.name)
			.append('\n');
			++idx;
		}

		sender.sendMessage(builder.toString());
	}
}