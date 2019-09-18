package Eventials.mailbox;

import Eventials.Eventials;

public final class ShippingService{
	final MailboxClient mailboxClient;
	final CommandMailbox mailboxCommand;

	public ShippingService(Eventials pl){
		mailboxClient = new MailboxClient(pl.getLogger(), Eventials.getBridge());
		mailboxCommand = new CommandMailbox(pl, mailboxClient);
	}

	public void onDisable(){
//		for(UUID playerUUID : waitingCallbacks.keySet()){}
		//TODO: Force-save/close any open mailboxes (to prevent item lose/duplication)
		//Need to (1) return items put into an unsaved mailbox, (2) delete items take out of an unsaved mailbox
		//if(IS_HOST) ((MailboxHoster)mailboxFetcher).conn.close();
		//else ((MailboxClient)mailboxFetcher).mailServer.close();
		mailboxCommand.closeAllMailboxes();
	}
}