package Eventials.mailbox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import Eventials.mailbox.Connection.MessageReceiver;
import Eventials.mailbox.Connection.MessageSender;

public final class MailboxHoster implements MailboxFetcher, MessageReceiver{
	final UUID MAILBOX_UUID;
	final Logger logger;

	// Host-only
	final String MAIL_DIR;
	final String EMPTY_MAIL_PLAYERDATA;
	final HashMap<UUID, UUID> currentLocks;
	final Connection conn;

	public MailboxHoster(Logger logger, String emptyPlayerdataFile, String mailDir, int port, int MAX_SERVERS){
		MAILBOX_UUID = UUID.randomUUID();
		currentLocks = new HashMap<UUID, UUID>();

		this.logger = logger;
		MAIL_DIR = mailDir;
		if(!new File(MAIL_DIR).exists()){new File(MAIL_DIR).mkdir(); logger.info("Setting up mailbox: "+MAIL_DIR);}
		EMPTY_MAIL_PLAYERDATA = emptyPlayerdataFile;
		conn = new ServerMain(this, port, MAX_SERVERS);
	}
	MailboxHoster(){
		MAILBOX_UUID = UUID.randomUUID();
		currentLocks = new HashMap<UUID, UUID>();

		logger = Logger.getLogger(getClass().getName());
		StreamHandler handler = new StreamHandler(System.out, new SimpleFormatter());
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		MAIL_DIR = "./mailbox/";
		if(!new File(MAIL_DIR).exists()){new File(MAIL_DIR).mkdir(); logger.info("Setting up mailbox: "+MAIL_DIR);}

		String playerdataFile = MAIL_DIR+"empty_inv_playerdata.dat";
		try(InputStream is = MailboxHoster.class.getResourceAsStream("/empty_inv_playerdata.dat")){
			Files.copy(is, Paths.get(playerdataFile), StandardCopyOption.REPLACE_EXISTING);
		}
		catch(IOException e){logger.warning("Failed to load empty_inv resource");}
		EMPTY_MAIL_PLAYERDATA = ShippingService.readBinaryFileAsString(new File(playerdataFile));
		conn = new ServerMain(this, 9561, 100);
	}
	public static void main(String[] args){new MailboxHoster();}

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
			Files.copy(mailboxFile.toPath(), new File(MAIL_DIR+playerUUID+".dat").toPath());
			callback.playerMailboxSaved(playerUUID, "saved");
		}
		catch(IOException e){
			logger.warning("Failed to save mail file for player: "+playerUUID);
			callback.playerMailboxSaved(playerUUID, "failed");
		}
	}

	@Override public void receiveMessage(MessageSender conn, String message){
		logger.info("[DEBUG] Received from conn: "+message);
		String metadata;
		int idx = message.indexOf('|');
		if(idx != -1){metadata = message.substring(0, idx); message = message.substring(idx+1);}
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
			logger.warning("Illegal UUID from mail server client: "+metadata);
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
			File mailFile = new File(MAIL_DIR+playerUUID+".dat");
			if(mailFile.exists()){
				String mailFileData = ShippingService.readBinaryFileAsString(mailFile);
				if(mailFileData != null){
					conn.sendMessage("load "+playerUUID+'|'+mailFileData);
					logger.info("[DEBUG] responded to mailbox load request");

					// If lock is never released (TODO: ensure client receives mailbox), assume mailbox has been emptied
					new File(MAIL_DIR+playerUUID+"_tmp.dat").delete();
					mailFile.renameTo(new File(MAIL_DIR+playerUUID+"_tmp.dat"));
				}
				else{
					logger.warning("Failed to load mail file for player: "+playerUUID);
					conn.sendMessage("fail load "+playerUUID);
				}
			}
			else conn.sendMessage("load "+playerUUID+'|'+EMPTY_MAIL_PLAYERDATA);
		}
		if(save){
			if(lock){
				//TODO: Ensure that this is the same client who is holding the lock
				currentLocks.remove(playerUUID);
			}
			// If message is empty, save nothing (leave mailbox file deleted; last contents are in *_tmp.dat)
			if(message == null || message.trim().isEmpty() ||
					ShippingService.saveBinaryStringToFile(new File(MAIL_DIR+playerUUID+".dat"), message)){
				conn.sendMessage("save "+playerUUID);
				logger.info("[DEBUG] responded to mailbox save request");
			}
			else{
				logger.warning("Failed to save mail file for player: "+playerUUID);
				conn.sendMessage("fail save "+playerUUID);
			}
		}
	}
}