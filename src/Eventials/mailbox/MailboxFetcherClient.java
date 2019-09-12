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
import net.evmodder.EvLib.sockets.ClientMain;
import net.evmodder.EvLib.sockets.Connection.MessageReceiver;
import net.evmodder.EvLib.sockets.Connection.MessageSender;

public final class MailboxFetcherClient extends MailboxFetcher implements MessageReceiver{
	final EvPlugin plugin;
	final UUID MAILBOX_UUID;
	private ClientMain mailServer;

	// Client-only
	final HashMap<UUID, MailListener> waitingCallbacks;

	public MailboxFetcherClient(EvPlugin pl){
		plugin = pl;
		MAILBOX_UUID = UUID.randomUUID();
		waitingCallbacks = new HashMap<UUID, MailListener>();
	}
	public void setOutputConnection(ClientMain conn){mailServer = conn;}

	@Override public void loadMailbox(UUID playerUUID, MailListener callback, boolean lock){
		if(lock && waitingCallbacks.containsKey(playerUUID)){
			callback.playerMailboxSaved(playerUUID, "locked");
		}
		waitingCallbacks.put(playerUUID, callback);
		mailServer.sendMessage("load "+(lock ? "lock " : "")+playerUUID.toString());
	}
	@Override public void saveMailbox(UUID playerUUID, File mailboxFile, MailListener callback, boolean lock){
		try{
			String data = new String(Files.readAllBytes(Paths.get(mailboxFile.getPath())), StandardCharsets.ISO_8859_1);
			waitingCallbacks.put(playerUUID, callback);
			mailServer.sendMessage("save "+(lock ? "lock " : "")+playerUUID.toString()+'\n'+data);
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

		if(lock){
			plugin.getLogger().warning("Mailbox is currently locked (opened elsewhere)");
			if(save) waitingCallbacks.remove(playerUUID).playerMailboxSaved(playerUUID, "locked");
			if(load) waitingCallbacks.remove(playerUUID).playerMailboxLoaded(playerUUID, null, "locked");
		}
		if(failure){
			plugin.getLogger().warning("Failure response from server");
			if(save) waitingCallbacks.remove(playerUUID).playerMailboxSaved(playerUUID, "failed");
			if(load) waitingCallbacks.remove(playerUUID).playerMailboxLoaded(playerUUID, null, "failed");
		}
		else{
			String filename = FileIO.getEvFolder()+playerUUID+"_mail_tmp.dat";
			if(save){
				waitingCallbacks.remove(playerUUID).playerMailboxSaved(playerUUID, "saved");
				new File(filename).delete();
			}
			if(load){
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