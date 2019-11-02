package Eventials.bridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.stream.Collectors;
import Eventials.bridge.basics.ServerMain;
import Eventials.bridge.basics.Connection.ChannelReceiver;
import Eventials.bridge.basics.Connection.MessageReceiver;
import Eventials.bridge.basics.Connection.MessageSender;
import Eventials.mailbox.MailboxHoster;

public final class EvBridgeHost implements MessageReceiver{
	final Logger logger;
	final UUID HOST_UUID;
	final HashMap<String, ChannelReceiver> activeChannels;
	final HashMap<ChannelReceiver, String> channelNameLookup;

	// Host-only
	final ServerMain conn;
	final HashMap<UUID, MessageSender> connectedClients;
	final HashMap<MessageSender, String> clientNames;

	public EvBridgeHost(Logger logger, int port, int MAX_SERVERS){
		HOST_UUID = UUID.randomUUID();
		activeChannels = new HashMap<String, ChannelReceiver>();
		channelNameLookup = new HashMap<ChannelReceiver, String>();
		connectedClients = new HashMap<UUID, MessageSender>();
		clientNames = new HashMap<MessageSender, String>();
		this.logger = logger;
		conn = new ServerMain(this, port, MAX_SERVERS);
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){@Override public void run(){heartbeat();}}, 45000, 45000);
	}
	EvBridgeHost(){
		this(Logger.getLogger("EvHost"), 42374, 100);
		//logger.setUseParentHandlers(false);
	}
	public static void main(String[] args){
		EvBridgeHost evHost = new EvBridgeHost();
		new MailboxHoster(evHost, evHost.logger, null, null);

		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new java.util.logging.Formatter(){
			@Override public String format(LogRecord record){
				return "> "+record.getLevel()+": "+record.getMessage();
			}
		});
		handler.setLevel(Level.ALL);
		evHost.logger.addHandler(handler);
	}

	private List<String> hbResponses = new ArrayList<String>();
	void heartbeat(){
		if(conn.numClients() != 0){
			logger.info("Heartbeat clients: ["+hbResponses.stream().collect(Collectors.joining(", "))+"]");
			conn.sendToAll("hb");
//			logger.info("Sent HB to "+conn.clients.size()+" clients");
			hbResponses.clear();
		}
	}

	public ChannelReceiver registerChannel(ChannelReceiver channel, String channelName){
		logger.info("Registering channel: "+channelName);
		channelNameLookup.put(channel, channelName);
		return activeChannels.put(channelName, channel);
	}

	@Override public void receiveMessage(MessageSender conn, String message){
		String name = clientNames.getOrDefault(conn, "xxx");
		if(message.equals("hb")){hbResponses.add(name); return;}
		else if(message.startsWith("name:")){clientNames.put(conn, message.substring(5)); return;}
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
		logger.info("[DEBUG] Received message from client '"+name+"'("+uuid+") on channel: "+channel);

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