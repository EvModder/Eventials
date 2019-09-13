package Eventials.mailbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.evmodder.EvLib.EvPlugin;
import net.evmodder.EvLib.FileIO;

public final class ShippingService{
	final boolean IS_HOST;
	final MailboxFetcher mailboxFetcher;
	final CommandMailbox mailboxCommand;

	public ShippingService(EvPlugin pl){
		String MAILBOX_IP = pl.getConfig().getString("mailbox-host", "localhost");
		int PORT = pl.getConfig().getInt("mailbox-port", 9565);
		IS_HOST = MAILBOX_IP.equalsIgnoreCase("me");

		//TODO: figure out a clever way to clean up this trashy 'MailboxAccessor' structure
		if(IS_HOST){
			int MAX_SERVERS = pl.getConfig().getInt("mailbox-max-connections", 100);
			File playerdataFile = new File(pl.getClass().getResource("/empty_inv_playerdata.dat").getFile());
			String EMPTY_PLAYERDATA = readBinaryFileAsString(playerdataFile);
			String MAIL_DIR = FileIO.getEvFolder()+"mailbox/";
			mailboxFetcher = new MailboxHoster(pl.getLogger(), EMPTY_PLAYERDATA, MAIL_DIR, PORT, MAX_SERVERS);
		}
		else{
			mailboxFetcher = new MailboxClient(pl.getLogger(), MAILBOX_IP, PORT);
			if(((MailboxClient)mailboxFetcher).mailServer.isClosed()){
				pl.getLogger().warning("Failed to connect to mail host. Restart the server to try again.");
				pl.getLogger().warning("Set 'mailbox-enabled: false' in config.yml to disable this message");
			}
		}
		mailboxCommand = new CommandMailbox(pl, mailboxFetcher);
	}

	public void onDisable(){
//		for(UUID playerUUID : waitingCallbacks.keySet()){}
		//TODO: Force-save/close any open mailboxes (to prevent item lose/duplication)
		//Need to (1) return items put into an unsaved mailbox, (2) delete items take out of an unsaved mailbox
		if(IS_HOST) ((MailboxHoster)mailboxFetcher).conn.close();
		else ((MailboxClient)mailboxFetcher).mailServer.close();
		mailboxCommand.closeAllMailboxes();
	}

	static String readBinaryFileAsString(File file){
		try{
			String data = new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
			return data.replace("|", " --pipesep-- *").replace("\n", " --linebreak-- ");
		}
		catch(IOException e){return null;}
	}
	static boolean saveBinaryStringToFile(File file, String data){
		data = data.replace(" --pipesep-- ", "|").replace(" --linebreak-- ", "\n");
		try(FileOutputStream fos = new FileOutputStream(file)){
			fos.write(data.getBytes(StandardCharsets.ISO_8859_1));
			return true;
		}
		catch(IOException e){return false;}
	}
}