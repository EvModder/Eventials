package net.evmodder.EvLib;

import org.bukkit.Location;

public class Section {
	public final int maxX, minX;
	public final int maxY, minY;
	public final int maxZ, minZ;
	public final String world;

	public Section(String world, int maxX, int minX, int maxY, int minY, int maxZ, int minZ){
		this.world = world;
		if(maxX > minX){this.maxX = maxX; this.minX = minX;}
		else{this.maxX = minX; this.minX = maxX;}
		
		if(maxY > minY){this.maxY = maxY; this.minY = minY;}
		else{this.maxY = minY; this.minY = maxY;}
		
		if(maxZ > minZ){this.maxZ = maxZ; this.minZ = minZ;}
		else{this.maxZ = minZ; this.minZ = maxZ;}
	}

	public boolean contains(Location l){
		if(l.getWorld().getName().equals(world) &&
			maxX >= l.getBlockX() && minX <= l.getBlockX() &&
			maxY >= l.getBlockY() && minY <= l.getBlockY() &&
			maxZ >= l.getBlockZ() && minZ <= l.getBlockZ()) return true;
		else return false;
	}

	@Override
	public String toString(){
		return new StringBuilder(world)
				.append(',').append(minX).append(',').append(maxX)
				.append(',').append(minY).append(',').append(maxY)
				.append(',').append(minZ).append(',').append(maxZ).toString();
	}
	
	public static Section fromString(String str){
		String[] data = str.split(",");
		if(data.length != 7) return null;
		try{
			return new Section(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]),
										Integer.parseInt(data[3]), Integer.parseInt(data[4]),
										Integer.parseInt(data[5]), Integer.parseInt(data[6]));
		}
		catch(NumberFormatException ex){return null;}
	}
}