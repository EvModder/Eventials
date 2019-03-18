package Eventials.splitworlds;

import java.util.UUID;

import org.bukkit.Location;

public class Teleport {
	UUID playerUUID;
	Location toLoc;
	String fromWorld;
	
	public Teleport(UUID uuid, Location to, String from){
		playerUUID = uuid;
		toLoc = to; fromWorld = from;
	}
}
