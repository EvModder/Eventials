package EvLib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class EvPlugin extends JavaPlugin{
	protected FileConfiguration config;
	@Override public FileConfiguration getConfig(){return config;}
	@Override public void saveConfig(){
		FileConfiguration currentConfig = null;
		InputStream defaultConfig = getClass().getResourceAsStream("/config.yml");
		if(defaultConfig != null) currentConfig = FileIO.loadConfig(this, "config-"+getName()+".yml", defaultConfig);
		
		if(config != null && (currentConfig == null || !config.equals(currentConfig)))
		try{config.save(new File("./plugins/EvFolder/config-"+getName()+".yml"));}
		catch(IOException ex){ex.printStackTrace();}
	}
	@Override public void reloadConfig(){
		InputStream defaultConfig = getClass().getResourceAsStream("/config.yml");
		if(defaultConfig != null) config = FileIO.loadConfig(this, "config-"+getName()+".yml", defaultConfig);
	}
	public void reloadConfig(FileConfiguration config){
		InputStream defaultConfig = getClass().getResourceAsStream("/config.yml");
		if(defaultConfig != null) config = FileIO.loadConfig(this, "config-"+getName()+".yml", defaultConfig);
	}

	@Override public final void onEnable(){
//		getLogger().info("Loading " + getDescription().getFullName());
//		new Updater(this, projectID, this.getFile(), Updater.UpdateType.DEFAULT, false);
		reloadConfig();
		onEvEnable();
	}

	@Override public final void onDisable(){
		onEvDisable();
	}

	public void onEvEnable(){}
	public void onEvDisable(){}
}