package Eventials.mailbox;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.UUID;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.FileIO;
import net.evmodder.EvLib.sockets.Connection.MessageReceiver;
import net.evmodder.EvLib.sockets.Connection.MessageSender;

public final class MailboxFetcherServer extends MailboxFetcher implements MessageReceiver{
	final private EvPlugin plugin;
	final UUID MAILBOX_UUID;

	// Host-only
	final HashMap<UUID, UUID> currentLocks;
	final String MAIL_DIR;
	final String EMPTY_MAIL_PLAYERDATA;

	public MailboxFetcherServer(EvPlugin pl){
		plugin = pl;
		MAILBOX_UUID = UUID.randomUUID();
		currentLocks = new HashMap<UUID, UUID>();
		EMPTY_MAIL_PLAYERDATA = FileIO.loadResource(pl, "empty_inv_playerdata.dat");
		MAIL_DIR = FileIO.getEvFolder()+"mailbox/";
		if(!new File(MAIL_DIR).exists()){new File(MAIL_DIR).mkdir(); pl.getLogger().info("Setting up mailbox: "+MAIL_DIR);}
	}

	@Override public void loadMailbox(UUID playerUUID, MailListener callback, boolean lock){
		if(lock){
			UUID currentLock = currentLocks.get(playerUUID);
			if(currentLock != null) callback.playerMailboxLoaded(playerUUID, null, currentLock.toString());
			else currentLocks.put(playerUUID, MAILBOX_UUID);
		}
		//else: Doesn't check for (or acquire) a lock
		callback.playerMailboxLoaded(playerUUID, new File(MAIL_DIR+playerUUID+".dat"), "loaded");
	}
	@Override public void saveMailbox(UUID playerUUID, File mailboxFile, MailListener callback, boolean lock){
		if(lock){
			UUID currentLock = currentLocks.get(playerUUID);
			if(currentLock != null){
				if(currentLock.equals(MAILBOX_UUID)){
					callback.playerMailboxSaved(playerUUID, "locked");
					return;
				}
				currentLocks.remove(playerUUID);
			}
		}
		try{
			com.google.common.io.Files.copy(mailboxFile, new File(MAIL_DIR+playerUUID+".dat"));
			callback.playerMailboxSaved(playerUUID, "saved");
		}
		catch(IOException e){
			plugin.getLogger().warning("Failed to save mail file for player: "+playerUUID);
			callback.playerMailboxSaved(playerUUID, "failed");
		}
	}

	@Override public void receiveMessage(MessageSender conn, String message){
		plugin.getLogger().info("[DEBUG] Received from conn: "+message);
		String metadata;
		int idx = message.indexOf('\n');
		if(idx == -1){metadata = message.substring(0, idx); message = message.substring(idx+1);}
		else metadata = message;

		boolean failure = metadata.startsWith("fail ");
		if(failure) metadata = metadata.substring(5);
		boolean load = metadata.startsWith("load ");
		if(load) metadata = metadata.substring(5);
		boolean save = metadata.startsWith("save ");
		if(save) metadata = metadata.substring(5);
		boolean lock = metadata.startsWith("lock ");
		if(lock) metadata = metadata.substring(5);
		UUID playerUUID = null;
		try{playerUUID = UUID.fromString(metadata);}
		catch(IllegalArgumentException ex){
			plugin.getLogger().warning("Illegal UUID from mail server client: "+metadata);
			return;
		}

		if(load){
			if(lock){
				UUID currentLock = currentLocks.get(playerUUID);
				if(currentLock != null){
					conn.sendMessage("fail lock "+playerUUID);
					return;
				}
				else currentLocks.put(playerUUID, MAILBOX_UUID);
			}
			if(new File(MAIL_DIR+playerUUID+".dat").exists()){
				try{conn.sendMessage("load "+playerUUID+" "+new String(
						Files.readAllBytes(Paths.get(MAIL_DIR+playerUUID+".dat")),
						StandardCharsets.ISO_8859_1));}
				catch(IOException e){
					plugin.getLogger().warning("Failed to load mail file for player: "+playerUUID);
					conn.sendMessage("fail load "+playerUUID);
				}
			}
			else conn.sendMessage("load "+playerUUID+" "+EMPTY_MAIL_PLAYERDATA);
		}
		else{ // save
			if(lock){
				//TODO: Ensure that this is the same client who is holding the lock
				currentLocks.remove(playerUUID);
			}
			try{
				Files.write(Paths.get(MAIL_DIR+playerUUID+".dat"),
					message.getBytes(StandardCharsets.ISO_8859_1), StandardOpenOption.CREATE);
				conn.sendMessage("save "+playerUUID);
			}
			catch(IOException e){
				plugin.getLogger().warning("Failed to save mail file for player: "+playerUUID);
				conn.sendMessage("fail save "+playerUUID);
			}
		}
	}
}