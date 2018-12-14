package Eventials.music.noteblockapi;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.HashMap;

public class NoteBlockPlayerMain extends JavaPlugin{
	static NoteBlockPlayerMain plugin;
	HashMap<String, ArrayList<SongPlayer>> playingSongs = new HashMap<String, ArrayList<SongPlayer>>();
	HashMap<String, Byte> playerVolume = new HashMap<String, Byte>();

	@Override public void onEnable(){
		plugin = this;
	}

	@Override public void onDisable(){
		getServer().getScheduler().cancelTasks(this);
	}

	public static boolean isReceivingSong(Player p){
		return ((plugin.playingSongs.get(p.getName()) != null) && (!plugin.playingSongs.get(p.getName()).isEmpty()));
	}

	public static void stopPlaying(Player p){
		if(plugin.playingSongs.get(p.getName()) == null) return;
		for(SongPlayer s : plugin.playingSongs.get(p.getName())) s.removePlayer(p);
	}

	public static void setPlayerVolume(Player p, byte volume){
		plugin.playerVolume.put(p.getName(), volume);
	}

	public static byte getPlayerVolume(Player p){
		Byte b = plugin.playerVolume.get(p.getName());
		if(b == null) plugin.playerVolume.put(p.getName(), (b=100));
		return b;
	}
}