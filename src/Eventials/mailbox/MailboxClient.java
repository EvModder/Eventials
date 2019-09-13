package Eventials.mailbox;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;
import Eventials.mailbox.Connection.MessageReceiver;
import Eventials.mailbox.Connection.MessageSender;
import net.evmodder.EvLib.FileIO;

public final class MailboxClient implements MailboxFetcher, MessageReceiver{
	final Logger logger;
	final UUID MAILBOX_UUID;

	// Client-only
	final HashMap<UUID, MailListener> waitingCallbacks;
	final ClientMain mailServer;

	public MailboxClient(Logger logger, String HOST, int PORT){
		MAILBOX_UUID = UUID.randomUUID();
		waitingCallbacks = new HashMap<UUID, MailListener>();

		this.logger = logger;
		mailServer = new ClientMain(this, HOST, PORT);
	}

	@Override public void loadMailbox(UUID playerUUID, MailListener callback, boolean lock){
		if(lock && waitingCallbacks.containsKey(playerUUID)){
			callback.playerMailboxLoaded(playerUUID, null, "locked");
			return;
		}
		waitingCallbacks.put(playerUUID, callback);
		mailServer.sendMessage("load "+(lock ? "lock " : "")+playerUUID);
		logger.info("[DEBUG] mailbox load request sent to mail server");
	}
	@Override public void saveMailbox(UUID playerUUID, File mailboxFile, MailListener callback, boolean lock){
		String mailFileData = ShippingService.readBinaryFileAsString(mailboxFile);
		if(mailFileData != null){
			waitingCallbacks.put(playerUUID, callback);
			mailServer.sendMessage("save "+(lock ? "lock " : "")+playerUUID+"|"+mailFileData);
			logger.info("[DEBUG] mailbox save request sent to server");
			logger.info("[DEBUG] message: "+mailFileData);
		}
		else{
			logger.warning("Failed to save mail file for player: "+playerUUID);
			callback.playerMailboxSaved(playerUUID, "failed");
		}
	}

	@Override public void receiveMessage(MessageSender conn, String message){
		String metadata;
		int idx = message.indexOf('|');
		if(idx != -1){metadata = message.substring(0, idx); message = message.substring(idx+1);}
		else metadata = message;
		logger.info("[DEBUG] Received from conn: "+metadata);

		boolean fail = metadata.startsWith("fail ");
		if(fail) metadata = metadata.substring(5);
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

		if(lock){
			logger.warning("Mailbox is currently locked (opened elsewhere)");
			if(save) waitingCallbacks.remove(playerUUID).playerMailboxSaved(playerUUID, "locked");
			if(load) waitingCallbacks.remove(playerUUID).playerMailboxLoaded(playerUUID, null, "locked");
		}
		if(fail){
			logger.warning("Failure response from server");
			if(save) waitingCallbacks.remove(playerUUID).playerMailboxSaved(playerUUID, "failed");
			if(load) waitingCallbacks.remove(playerUUID).playerMailboxLoaded(playerUUID, null, "failed");
		}
		else{
			String filename = FileIO.getEvFolder()+playerUUID+"_mail_tmp.dat";
			if(save){
				logger.info("[DEBUG] got save confirmation from mail server");
				waitingCallbacks.remove(playerUUID).playerMailboxSaved(playerUUID, "saved");
				new File(filename).delete();
			}
			if(load){
				if(ShippingService.saveBinaryStringToFile(new File(filename), message)){
					logger.info("[DEBUG] got mailbox file from mail server");
					waitingCallbacks.remove(playerUUID).playerMailboxLoaded(playerUUID, new File(filename), "loaded");
				}
				else{
					logger.warning("Failed to load mail file for player: "+playerUUID);
					waitingCallbacks.remove(playerUUID).playerMailboxLoaded(playerUUID, null, "failed");
				}
			}
		}
	}
}