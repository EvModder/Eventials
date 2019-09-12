package Eventials.mailbox;

import java.util.UUID;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.sockets.ClientMain;
import net.evmodder.EvLib.sockets.Connection;
import net.evmodder.EvLib.sockets.ServerMain;

public final class ShippingService{
	final UUID MAILBOX_UUID;
	final boolean IS_HOST;
	private Connection mailServer;
	private MailboxFetcher mailboxFetcher;

	public ShippingService(EvPlugin pl){
		String MAILBOX_IP = pl.getConfig().getString("mailbox-host", "me");
		int PORT = pl.getConfig().getInt("mailbox-port", 9565);
		IS_HOST = MAILBOX_IP.equalsIgnoreCase("me");
		MAILBOX_UUID = UUID.randomUUID();

		//TODO: figure out a clever way to clean up this trashy 'MailboxAccessor' structure
		if(IS_HOST){
			int MAX_SERVERS = pl.getConfig().getInt("mailbox-max-connections", 100);
			mailboxFetcher = new MailboxFetcherServer(pl);
			new ServerMain((MailboxFetcherServer)mailboxFetcher, PORT, MAX_SERVERS);
		}
		else{
			mailboxFetcher = new MailboxFetcherClient(pl);
			mailServer = new ClientMain((MailboxFetcherClient)mailboxFetcher, MAILBOX_IP, PORT);
			((MailboxFetcherClient)mailboxFetcher).setOutputConnection((ClientMain)mailServer);
		}
//		new CommandMailbox(mailboxFetcher);
	}

	public void onDisable(){
//		for(UUID playerUUID : waitingCallbacks.keySet()){}
		//TODO: Force-save/close any open mailboxes (to prevent item lose/duplication)
		//Need to (1) return items put into an unsaved mailbox, (2) delete items take out of an unsaved mailbox
		mailServer.close();
	}
}