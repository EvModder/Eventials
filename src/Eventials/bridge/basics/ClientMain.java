package Eventials.bridge.basics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;
import Eventials.bridge.basics.Connection.MessageSender;

public class ClientMain extends Connection implements MessageSender{
	//=========== Added main function =============================================
	public static void main(String[] args){
		MessageReceiver receiver = new MessageReceiver(){
			Logger logger = Logger.getLogger(ClientMain.class.getName());

			@Override public void receiveMessage(MessageSender server, String message){logger.info("Received: "+message);}
			@Override public void failedToConnect(){logger.info("Unable to connect to server!");}
			@Override public void clientConnected(MessageSender client){logger.info("Connected to server");}
			@Override public void clientDisconnected(MessageSender client){logger.info("Connection closed");}
		};
		//Connect to server
		ClientMain connection = new ClientMain(receiver, "localhost", 42374);
		
		Scanner scan = new Scanner(System.in);
		String message;
		while(scan.hasNextLine() && !(message=scan.nextLine()).isBlank()){
			connection.sendMessage(receiver, message);
			System.out.println("Sent: "+message);
		}
		scan.close();
		connection.close();
	}
	//=============================================================================

	final int PORT;
	final String HOST_ADDRESS;
	Socket socket;
	PrintWriter out;
	BufferedReader in;

	@Override public void sendMessage(MessageReceiver source, String message){
		out.print(message);
		out.print('\n');
		out.flush();
	}

	public ClientMain(MessageReceiver recv, String host, int port){
		HOST_ADDRESS = host;
		PORT = port;
		receiver = recv;
		try{
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(IOException e){
			recv.failedToConnect();
			socket = null;
			return;
		}

		//ioThread
		new Thread(){
			@Override public void run(){
				while(!isClosed()){
					try{
						if(in.ready()){
							StringBuilder builder = new StringBuilder("");
							for(char c = (char)in.read(); c != '\n'; c = (char)in.read()) builder.append(c);
							final String message = builder.toString();
							if(message.equals(DISCONNECT_KEYWORD)) close();
							else receiver.receiveMessage(ClientMain.this, message);
						}
					}
					catch(IOException e){e.printStackTrace();}
				}
				receiver.clientDisconnected(ClientMain.this);
			}
		}.start();

		recv.clientConnected(this);
	}

	@Override
	public boolean isClosed(){
		return socket == null || socket.isClosed();
	}

	@Override
	public void close(){
		if(socket != null){
			sendMessage(null, DISCONNECT_KEYWORD);
			try{socket.close();}
			catch(IOException e){e.printStackTrace();}
		}
		socket = null;
	}
}