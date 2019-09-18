package Eventials.bridge;

import java.util.UUID;

//An abstract representation of a network connection (either a server or a client)
public abstract class Connection{
	MessageReceiver receiver;

	// A receiver to handle incoming messages
	public interface MessageReceiver{
		// Who sent it + message
		void receiveMessage(MessageSender sender, String message);
	}
	// A connections to which outgoing message may be sent
	public interface MessageSender{
		// Who to respond to + message
		void sendMessage(MessageReceiver source, String message);
	}

	public interface ChannelReceiver{
		// Who sent it + message
		void receiveMessage(UUID sender, String message);
	};

	public abstract boolean isClosed();

	public abstract void close();
}