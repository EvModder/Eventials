package Eventials.mailbox;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.FileIO;
import Eventials.mailbox.Connection.MessageReceiver;
import Eventials.mailbox.Connection.MessageSender;

public final class ShippingService implements MessageReceiver{
//	final private EvPlugin plugin;
	final String MAILBOX_IP;
	final int PORT;
	final UUID MAILBOX_UUID;
	final boolean IS_HOST;

	// Host-only
	private HashMap<UUID, UUID> currentLocks;
	private String MAIL_DIR;
	Connection mailServer;

	abstract class MailListener{
		public abstract void mailServerEvent(File mailbox, String message);
	};

	public ShippingService(EvPlugin pl){
//		plugin = pl;
		MAILBOX_IP = pl.getConfig().getString("mailbox-host", "me");
		PORT = pl.getConfig().getInt("mailbox-port", 9565);
		IS_HOST = MAILBOX_IP.equalsIgnoreCase("me");
		MAILBOX_UUID = UUID.randomUUID();

		if(IS_HOST){
			MAIL_DIR = FileIO.getEvFolder()+"mailbox/";
			File mailDir = new File(MAIL_DIR);
			if(!mailDir.exists()){mailDir.mkdir(); pl.getLogger().info("Setting up mailbox: "+MAIL_DIR);}
			currentLocks = new HashMap<UUID, UUID>();
			int MAX_SERVERS = pl.getConfig().getInt("mailbox-max-connections", 100);
			mailServer = new ServerMain(this, PORT, MAX_SERVERS);
		}
//		new CommandMailbox(this);
	}

	public void onDisable(){
		//TODO: Force-save/close any open mailboxes (to prevent item lose/duplication)
	}

	public void loadMailbox(UUID playerUUID, MailListener callback, boolean lock){
		if(IS_HOST){
			if(lock){
				UUID currentLock = currentLocks.get(playerUUID);
				if(currentLock != null){
					callback.mailServerEvent(null, currentLock.toString());
				}
				else{
					currentLocks.put(playerUUID, MAILBOX_UUID);
				}
			}
			//else: Doesn't check for (or acquire) a lock
			callback.mailServerEvent(new File(MAIL_DIR+playerUUID+".dat"), "Mailbox opened");
		}
		else{
			
		}
	}
	public void saveMailbox(UUID playerUUID, boolean unlock){
		if(IS_HOST){
			if(unlock){
				
			}
		}
	}

	@Override public void receiveMessage(MessageSender conn, String message){
		if(IS_HOST){ // Answer request from a client
			
		}
		else{ // Receive message from host
			
		}
	}
}