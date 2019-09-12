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
import Eventials.mailbox.Connection.MessageReceiver;
import Eventials.mailbox.Connection.MessageSender;

public final class ShippingService implements MessageReceiver{
	final private EvPlugin plugin;
	final UUID MAILBOX_UUID;
	final boolean IS_HOST;
	private Connection mailServer;

	// Host-only
	private HashMap<UUID, UUID> currentLocks;
	private String MAIL_DIR;
	private String EMPTY_MAIL_PLAYERDATA;

	// Client-only
	private HashMap<UUID, MailListener> waitingCallbacks;

	abstract class MailListener{
		public abstract void playerMailboxLoaded(UUID playerUUID, File mailbox, String message);
		public abstract void playerMailboxSaved(UUID playerUUID, String message);
	};

	public ShippingService(EvPlugin pl){
		plugin = pl;
		String MAILBOX_IP = pl.getConfig().getString("mailbox-host", "me");
		int PORT = pl.getConfig().getInt("mailbox-port", 9565);
		IS_HOST = MAILBOX_IP.equalsIgnoreCase("me");
		MAILBOX_UUID = UUID.randomUUID();

		if(IS_HOST){
			EMPTY_MAIL_PLAYERDATA = FileIO.loadResource(pl, "empty_inv_playerdata.dat");
			MAIL_DIR = FileIO.getEvFolder()+"mailbox/";
			File mailDir = new File(MAIL_DIR);
			if(!mailDir.exists()){mailDir.mkdir(); pl.getLogger().info("Setting up mailbox: "+MAIL_DIR);}
			currentLocks = new HashMap<UUID, UUID>();
			int MAX_SERVERS = pl.getConfig().getInt("mailbox-max-connections", 100);
			mailServer = new ServerMain(this, PORT, MAX_SERVERS);
		}
		else{
			mailServer = new ClientMain(this, MAILBOX_IP, PORT);
			waitingCallbacks = new HashMap<UUID, MailListener>();
		}
//		new CommandMailbox(this);
	}

	public void onDisable(){
//		for(UUID playerUUID : waitingCallbacks.keySet()){}
		//TODO: Force-save/close any open mailboxes (to prevent item lose/duplication)
		//Need to (1) return items put into an unsaved mailbox, (2) delete items take out of an unsaved mailbox
	}

	public void loadMailbox(UUID playerUUID, MailListener callback, boolean lock){
		if(IS_HOST){
			if(lock){
				UUID currentLock = currentLocks.get(playerUUID);
				if(currentLock != null) callback.playerMailboxLoaded(playerUUID, null, currentLock.toString());
				else currentLocks.put(playerUUID, MAILBOX_UUID);
			}
			//else: Doesn't check for (or acquire) a lock
			callback.playerMailboxLoaded(playerUUID, new File(MAIL_DIR+playerUUID+".dat"), "loaded");
		}
		else{
			waitingCallbacks.put(playerUUID, callback);
			((ClientMain)mailServer).sendMessage("load "+(lock ? "lock " : "")+playerUUID.toString());
		}
	}
	public void saveMailbox(UUID playerUUID, File mailboxFile, MailListener callback, boolean lock){
		if(IS_HOST){
			if(lock){
				UUID currentLock = currentLocks.get(playerUUID);
				if(currentLock != null){
					if(currentLock.equals(MAILBOX_UUID)){
						callback.playerMailboxSaved(playerUUID, currentLock.toString());
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
		else{
			try{
				String data = new String(Files.readAllBytes(Paths.get(mailboxFile.getPath())), StandardCharsets.ISO_8859_1);
				waitingCallbacks.put(playerUUID, callback);
				((ClientMain)mailServer).sendMessage("save "+(lock ? "lock " : "")+playerUUID.toString()+'\n'+data);
			}
			catch(IOException e){
				plugin.getLogger().warning("Failed to save mail file for player: "+playerUUID);
				callback.playerMailboxSaved(playerUUID, "failed");
			}
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

		if(IS_HOST){ // Answer request from a client
			if(lock){
				UUID currentLock = currentLocks.get(playerUUID);
				if(currentLock != null) {
					conn.sendMessage("fail lock "+playerUUID);
					return;
				}
				else currentLocks.put(playerUUID, MAILBOX_UUID);
			}
			if(load){
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
		else{ // Receive message from host
			if(lock) plugin.getLogger().warning("Mailbox is currently locked (opened elsewhere)");
			if(failure){
				plugin.getLogger().warning("Failure response from server");
				if(save) waitingCallbacks.remove(playerUUID).playerMailboxSaved(playerUUID, "failed");
				if(load) waitingCallbacks.remove(playerUUID).playerMailboxLoaded(playerUUID, null, "failed");
			}
			else{
				if(save) waitingCallbacks.remove(playerUUID).playerMailboxSaved(playerUUID, "saved");
				if(load){
					String filename = FileIO.getEvFolder()+playerUUID+"_mail_tmp.dat";
					try{
						Files.write(Paths.get(filename),
								message.getBytes(StandardCharsets.ISO_8859_1), StandardOpenOption.CREATE);
						waitingCallbacks.remove(playerUUID).playerMailboxLoaded(playerUUID, new File(filename), "loaded");
					}
					catch(IOException e){
						plugin.getLogger().warning("Failed to load mail file for player: "+playerUUID);
						waitingCallbacks.remove(playerUUID).playerMailboxLoaded(playerUUID, null, "failed");
					}
				}
			}
		}
	}
}