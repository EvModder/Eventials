package Eventials.bridge;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;
import Eventials.Eventials;
import Eventials.bridge.basics.ClientMain;
import Eventials.bridge.basics.Connection.ChannelReceiver;
import Eventials.bridge.basics.Connection.MessageReceiver;
import Eventials.bridge.basics.Connection.MessageSender;

public final class EvBridgeClient implements MessageReceiver{
	final Logger logger;
	final UUID CLIENT_UUID;
	final HashMap<String, ChannelReceiver> activeChannels;
	final HashMap<ChannelReceiver, String> channelNameLookup;
	long lastHB;

	final ClientMain conn;

	public EvBridgeClient(Logger logger, String HOST, int PORT){
		CLIENT_UUID = UUID.randomUUID();
		activeChannels = new HashMap<>();
		channelNameLookup = new HashMap<>();
		this.logger = logger;
		conn = new ClientMain(this, HOST, PORT);
	}

	public ChannelReceiver registerChannel(ChannelReceiver channel, String channelName){
		logger.fine("Registering channel: "+channelName);
		channelNameLookup.put(channel, channelName);
		return activeChannels.put(channelName, channel);
	}

	@Override public void clientConnected(MessageSender conn){
		logger.info("Sucessfully connected to EvBridge (mail server)");
		conn.sendMessage(this, "name:"+Eventials.getPlugin().getConfig().getString("server-name", "xxx"));
	}
	@Override public void failedToConnect(){
		logger.info("Failed to connect to EvBridge (mail server)");
	}
	@Override public void receiveMessage(MessageSender conn, String message){
		if(message.equals("hb")){
//			logger.info("received heartbeat");
			conn.sendMessage(this, "hb");
			lastHB = System.currentTimeMillis();
			return;
		}
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
		if(uuid == null) logger.severe("Unable to parse host UUID from message");
		logger.info("[DEBUG] Received message from host("+uuid+") on channel: "+channel);

		ChannelReceiver receiver = activeChannels.get(channel);
		if(receiver == null){
			logger.warning("Got message from unregistered channel: "+channel);
		}
		else{
			receiver.receiveMessage(uuid, message);
		}
	}

	public void sendMessage(ChannelReceiver sourceChannel, String message){
		String channel = channelNameLookup.get(sourceChannel);
		conn.sendMessage(this, CLIENT_UUID+"|"+channel+"|"+message);
	}

	public long getLastHeartbeat(){return lastHB;}

	public boolean isClosed(){return conn.isClosed();}
}