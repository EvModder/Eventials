package Eventials.bridge;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import Eventials.bridge.Connection.ChannelReceiver;
import Eventials.bridge.Connection.MessageReceiver;
import Eventials.bridge.Connection.MessageSender;
import Eventials.mailbox.MailboxHoster;

public final class EvBridgeHost implements MessageReceiver{
	final Logger logger;
	final UUID HOST_UUID;
	final HashMap<String, ChannelReceiver> activeChannels;
	final HashMap<ChannelReceiver, String> channelNameLookup;

	// Host-only
	final ServerMain conn;
	final HashMap<UUID, MessageSender> connectedClients;

	public EvBridgeHost(Logger logger, int port, int MAX_SERVERS){
		HOST_UUID = UUID.randomUUID();
		activeChannels = new HashMap<String, ChannelReceiver>();
		channelNameLookup = new HashMap<ChannelReceiver, String>();
		connectedClients = new HashMap<UUID, MessageSender>();
		this.logger = logger;
		conn = new ServerMain(this, port, MAX_SERVERS);
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){@Override public void run(){heartbeat();}}, 15000, 15000);
	}
	EvBridgeHost(){
		this(Logger.getLogger("EvHost"), 42374, 100);
	}
	public static void main(String[] args){
		EvBridgeHost evHost = new EvBridgeHost();
		new MailboxHoster(evHost, evHost.logger, null, null);

		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		handler.setLevel(Level.ALL);
		evHost.logger.addHandler(handler);
	}

	private int hbResponses;
	void heartbeat(){
		if(hbResponses != 0) logger.info("HB responses: "+hbResponses);
		hbResponses = 0;
		conn.sendToAll("hb");
		logger.info("Sent heartbeat to "+conn.clients.size()+" clients");
	}

	public ChannelReceiver registerChannel(ChannelReceiver channel, String channelName){
		logger.info("Registering channel: "+channelName);
		channelNameLookup.put(channel, channelName);
		return activeChannels.put(channelName, channel);
	}

	@Override public void receiveMessage(MessageSender conn, String message){
		UUID uuid;
		String channel;
		int idx = message.indexOf('|');
		if(idx != -1){
			try{uuid = UUID.fromString(message.substring(0, idx));}
			catch(IllegalArgumentException ex){uuid = null;}
			message = message.substring(idx+1);
			idx = message.indexOf('|');
			if(idx != -1){channel = message.substring(0, idx); message = message.substring(idx+1);}
			else{channel = message; message = "";}
		}
		else{uuid = null; channel = null; message = null;}
		if(uuid == null) logger.severe("Unable to parse client UUID from message");
		connectedClients.put(uuid, conn);
		logger.info("[DEBUG] Received message from client("+uuid+") on channel: "+channel);

		ChannelReceiver receiver = activeChannels.get(channel);
		if(receiver == null){
			logger.warning("Got message from unregistered channel: "+channel);
		}
		else{
			receiver.receiveMessage(uuid, message);
		}
	}

	public void sendMessage(ChannelReceiver sourceChannel, UUID dest, String message){
		MessageSender conn = connectedClients.get(dest);
		String channel = channelNameLookup.get(sourceChannel);
		conn.sendMessage(this, HOST_UUID+"|"+channel+"|"+message);
	}
}