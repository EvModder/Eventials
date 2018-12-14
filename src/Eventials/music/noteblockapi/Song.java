package Eventials.music.noteblockapi;

import java.io.File;
import java.util.HashMap;

public class Song{
	private HashMap<Integer, Layer> layerHashMap = new HashMap<Integer, Layer>();
	private short songHeight, length;
	private File path;
	private String author, title, description;
	private float speed, delay;

	public Song(Song other){
		this.speed = other.getSpeed();
		delay = 20 / speed;
		layerHashMap = other.getLayerHashMap();
		songHeight = other.getSongHeight();
		length = other.getLength();
		title = other.getTitle();
		author = other.getAuthor();
		description = other.getDescription();
		path = other.getPath();
	}

	public Song(float speed, HashMap<Integer, Layer> layerHashMap, short songHeight,
			final short length, String title, String author, String description, File path){
		this.speed = speed;
		delay = 20 / speed;
		this.layerHashMap = layerHashMap;
		this.songHeight = songHeight;
		this.length = length;
		this.title = title;
		this.author = author;
		this.description = description;
		this.path = path;
	}

	public HashMap<Integer, Layer> getLayerHashMap(){return layerHashMap;}
	public short getSongHeight(){return songHeight;}
	public short getLength(){return length;}
	public String getTitle(){return title;}
	public String getAuthor(){return author;}
	public File getPath(){return path;}
	public String getDescription(){return description;}
	public float getSpeed(){return speed;}
	public float getDelay(){return delay;}
}