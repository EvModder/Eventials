package Eventials.bridge;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;
import Eventials.bridge.ClientMain;
import Eventials.bridge.Connection.ChannelReceiver;
import Eventials.bridge.Connection.MessageReceiver;
import Eventials.bridge.Connection.MessageSender;

public final class EvBridgeClient implements MessageReceiver{
	final Logger logger;
	final UUID CLIENT_UUID;
	final HashMap<String, ChannelReceiver> activeChannels;
	final HashMap<ChannelReceiver, String> channelNameLookup;

	final ClientMain conn;

	public EvBridgeClient(Logger logger, String HOST, int PORT){
		CLIENT_UUID = UUID.randomUUID();
		activeChannels = new HashMap<String, ChannelReceiver>();
		channelNameLookup = new HashMap<ChannelReceiver, String>();
		this.logger = logger;
		conn = new ClientMain(this, HOST, PORT);
	}

	public ChannelReceiver registerChannel(ChannelReceiver channel, String channelName){
		logger.info("Registering channel: "+channelName);
		channelNameLookup.put(channel, channelName);
		return activeChannels.put(channelName, channel);
	}

	@Override public void receiveMessage(MessageSender conn, String message){
		if(message.equals("hb")){
			logger.info("received heartbeat");
			conn.sendMessage(this, "hb");
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
}