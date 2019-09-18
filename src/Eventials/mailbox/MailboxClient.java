package Eventials.mailbox;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;
import Eventials.Eventials;
import Eventials.bridge.EvBridgeClient;
import Eventials.bridge.Connection.ChannelReceiver;
import net.evmodder.EvLib.FileIO;

public final class MailboxClient implements ChannelReceiver{
	public interface MailListener{
		public abstract void playerMailboxLoaded(UUID playerUUID, File mailbox, String message);
		public abstract void playerMailboxSaved(UUID playerUUID, String message);
	};

	final Logger logger;
	final EvBridgeClient bridge;
	final HashMap<UUID, MailListener> waitingCallbacks;
	final CommandMailbox mailboxCommand;

	public MailboxClient(Eventials pl){
		waitingCallbacks = new HashMap<UUID, MailListener>();
		bridge = Eventials.getBridge();
		logger = pl.getLogger();
		Eventials.getBridge().registerChannel(this, "mailbox");
		mailboxCommand = new CommandMailbox(pl, this);
	}

	public void onDisable(){
		//TODO: force close mailboxes?
	}

	public void loadMailbox(UUID playerUUID, MailListener callback, boolean lock){
		if(lock && waitingCallbacks.containsKey(playerUUID)){
			callback.playerMailboxLoaded(playerUUID, null, "locked");
			return;
		}
		waitingCallbacks.put(playerUUID, callback);
		bridge.sendMessage(this, "load "+(lock ? "lock " : "")+playerUUID);
		logger.info("[DEBUG] mailbox load request sent to mail server");
	}
	public void saveMailbox(UUID playerUUID, File mailboxFile, MailListener callback, boolean lock){
		String mailFileData = MailboxUtils.readBinaryFileAsString(mailboxFile);
		if(mailFileData != null){
			waitingCallbacks.put(playerUUID, callback);
			bridge.sendMessage(this, "save "+(lock ? "lock " : "")+playerUUID+"|"+mailFileData);
			logger.info("[DEBUG] mailbox save request sent to server");
		}
		else{
			logger.warning("Failed to save mail file for player: "+playerUUID);
			callback.playerMailboxSaved(playerUUID, "failed");
		}
	}

	@Override public void receiveMessage(UUID hostUUID, String message){
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
				if(MailboxUtils.saveBinaryStringToFile(new File(filename), message)){
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