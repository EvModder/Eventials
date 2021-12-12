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
			@Override
			public void receiveMessage(MessageSender server, String message) {
				System.out.println("Received: "+message);
			}
		};
		//Connect to server
		ClientMain connection = new ClientMain(receiver, "localhost", 42374, Logger.getLogger(ClientMain.class.getName()));
		
		Scanner scan = new Scanner(System.in);
		while(scan.hasNextLine()){
			String message = scan.nextLine();
			connection.sendMessage(receiver, message);
			System.out.println("Sent: "+message);
		}
		scan.close();
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

	public ClientMain(MessageReceiver recv, String host, int port, Logger logger){
		HOST_ADDRESS = host;
		PORT = port;
		receiver = recv;
		try{
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			logger.info("Connected to server "+host+":"+port);
		}
		catch(IOException e){
			logger.info("Unable to connect to server! (address="+host+":"+port+")");
			socket = null;
			return;
		}

		//ioThread
		new Thread(){
			@Override public void run(){
				while(!socket.isClosed()){
					try{
						if(in.ready()){
							StringBuilder builder = new StringBuilder("");
							for(char c = (char)in.read(); c != '\n'; c = (char)in.read()) builder.append(c);
							receiver.receiveMessage(ClientMain.this, builder.toString());
						}
					}
					catch(IOException e){e.printStackTrace();}
				}
				logger.info("Connection closed.");
			}
		}.start();
	}

	@Override
	public boolean isClosed(){
		return socket == null || socket.isClosed();
	}

	@Override
	public void close(){
		if(socket != null) try{socket.close();}
		catch(IOException e){e.printStackTrace();}
	}
}