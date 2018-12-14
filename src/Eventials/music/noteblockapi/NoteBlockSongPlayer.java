package Eventials.music.noteblockapi;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class NoteBlockSongPlayer extends SongPlayer{
	private Block noteBlock;

	public NoteBlockSongPlayer(Song song){
		super(song);
	}

	public Block getNoteBlock(){return noteBlock;}
	public void setNoteBlock(Block noteBlock){this.noteBlock = noteBlock;}

	@Override public void playTick(Player p, int tick){
		if(noteBlock.getType() != Material.NOTE_BLOCK) return;
		if(!p.getWorld().getName().equals(noteBlock.getWorld().getName())) return;

		byte playerVolume = NoteBlockPlayerMain.getPlayerVolume(p);

		for(Layer l : song.getLayerHashMap().values()){
			Note note = l.getNote(tick);
			if(note == null) continue;

			p.playNote(noteBlock.getLocation(), Instrument.getBukkitInstrument(note.getInstrument()), new org.bukkit.Note(note.getKey() - 33));
			p.playSound(noteBlock.getLocation(), Instrument.getInstrument(note.getInstrument()), (l.getVolume() * (int)volume * (int)playerVolume) / 1000000f,
					NotePitch.getPitch(note.getKey() - 33));
		}
	}
}