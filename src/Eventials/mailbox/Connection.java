package Eventials.mailbox;

//An abstract representation of a network connection (either a server or a client)
public abstract class Connection{
	MessageReceiver receiver;

	// A receiver to handle incoming messages
	public interface MessageReceiver{
		void receiveMessage(MessageSender sender, String message);
	}

	// A connections to which outgoing message may be sent
	public interface MessageSender{
		void sendMessage(String message);
	}

	public Connection(MessageReceiver recv){
		receiver = recv;
	}

	public final void setReceiver(MessageReceiver newRecv){
		receiver = newRecv;
	}

	public abstract boolean isClosed();

	public abstract void close();
}