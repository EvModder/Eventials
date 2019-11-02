package Eventials.mailbox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;
import Eventials.bridge.EvBridgeHost;
import Eventials.bridge.basics.Connection.ChannelReceiver;

public final class MailboxHoster implements ChannelReceiver{
	final String MAIL_DIR;
	final String EMPTY_MAIL_PLAYERDATA;
	final HashMap<UUID, UUID> currentLocks;
	final EvBridgeHost evHost;
	final Logger logger;

	public MailboxHoster(EvBridgeHost host, Logger logger, String emptyPdataFile, String mailDir){
		currentLocks = new HashMap<UUID, UUID>();
		this.logger = logger;
		MAIL_DIR = mailDir == null ? "./mailbox/" : mailDir;
		if(!new File(MAIL_DIR).exists()){new File(MAIL_DIR).mkdir(); logger.info("Setting up mailbox: "+MAIL_DIR);}
		if(emptyPdataFile != null) EMPTY_MAIL_PLAYERDATA = emptyPdataFile;
		else{
			String playerdataFile = MAIL_DIR+"empty_inv_playerdata.dat";
			try(InputStream is = MailboxHoster.class.getResourceAsStream("/empty_inv_playerdata.dat")){
				Files.copy(is, Paths.get(playerdataFile), StandardCopyOption.REPLACE_EXISTING);
			}
			catch(IOException e){System.err.println("Failed to load empty_inv resource");}
			EMPTY_MAIL_PLAYERDATA = MailboxUtils.readBinaryFileAsString(new File(playerdataFile));
		}
		evHost = host;
		evHost.registerChannel(this, "mailbox");
		logger.info("Mailbox Manager registered");
	}

	@Override public void receiveMessage(UUID clientUUID, String message){
		String metadata;
		int idx = message.indexOf('|');
		if(idx != -1){metadata = message.substring(0, idx); message = message.substring(idx+1);}
		else metadata = message;
		System.out.println("[DEBUG] Received from conn: "+metadata);

		boolean failure = metadata.startsWith("fail ");
		if(failure) metadata = metadata.substring(5);
		boolean load = metadata.startsWith("load ");
		if(load) metadata = metadata.substring(5);
		boolean save = metadata.startsWith("save ");
		if(save) metadata = metadata.substring(5);
		boolean lock = metadata.startsWith("lock ");
		if(lock) metadata = metadata.substring(5);
		UUID playerUUID;
		try{playerUUID = UUID.fromString(metadata);}
		catch(IllegalArgumentException ex){
			System.out.println("Illegal UUID from mail server client: '"+metadata+"'");
			return;
		}

		if(load){
			if(lock){
				UUID currentLock = currentLocks.get(playerUUID);
				if(currentLock != null){
					logger.info("[DEBUG] response to load request: LOCKED");
					evHost.sendMessage(this, clientUUID, "fail lock "+playerUUID);
					return;
				}
				else currentLocks.put(playerUUID, clientUUID);
			}
			File mailFile = new File(MAIL_DIR+playerUUID+".dat");
			if(mailFile.exists()){
				String mailFileData = MailboxUtils.readBinaryFileAsString(mailFile);
				if(mailFileData != null){
					evHost.sendMessage(this, clientUUID, "load "+playerUUID+"|"+mailFileData);
					logger.info("[DEBUG] response to load request: SUCCESS");

					// If lock is never released (TODO: ensure client receives mailbox), assume mailbox has been emptied
					mailFile.renameTo(new File(MAIL_DIR+playerUUID+"_tmp.dat"));
				}
				else{
					logger.warning("Failed to load mail file for player: "+playerUUID);
					evHost.sendMessage(this, clientUUID, "fail load "+playerUUID);
				}
			}
			else evHost.sendMessage(this, clientUUID, "load "+playerUUID+"|"+EMPTY_MAIL_PLAYERDATA);
		}
		if(save){
			if(lock){
				//TODO: Ensure that this is the same client who is holding the lock
				currentLocks.remove(playerUUID);
				logger.info("[DEBUG] Unlocked: "+playerUUID);
			}
			// If message is empty, save nothing (leave mailbox file deleted; last contents are in *_tmp.dat)
			if(message != null && !message.trim().isEmpty() &&
					MailboxUtils.saveBinaryStringToFile(new File(MAIL_DIR+playerUUID+".dat"), message)){
				new File(MAIL_DIR+playerUUID+"_tmp.dat").delete();
				evHost.sendMessage(this, clientUUID, "save "+playerUUID);
				logger.info("[DEBUG] responded to mailbox save request");
			}
			else{
				if(message != null && !message.trim().isEmpty())
					logger.warning("Received empty save from client for player: "+playerUUID);
				else logger.warning("Failed to save mail file for player: "+playerUUID);
				evHost.sendMessage(this, clientUUID, "fail save "+playerUUID);
			}
		}
	}
}