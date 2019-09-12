package Eventials.mailbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import Eventials.mailbox.Connection.MessageSender;

public class ClientMain extends Connection implements MessageSender{
	//=========== Added main function =============================================
	public static void main(String[] args){
		//Connect to server
		ClientMain connection = new ClientMain(
			new MessageReceiver(){
				@Override
				public void receiveMessage(MessageSender server, String message) {
					System.out.println("Received: "+message);
				}
			}, "localhost", 42374
		);
		
		Scanner scan = new Scanner(System.in);
		while(scan.hasNextLine()){
			String message = scan.nextLine();
			connection.sendMessage(message);
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

	@Override public void sendMessage(String message){
		out.print(message);
		out.flush();
	}

	public ClientMain(MessageReceiver recv, String host, int port){
		super(recv);
		HOST_ADDRESS = host;
		PORT = port;
		try{
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("Connected to server");
		}
		catch(IOException e){
			System.out.println("Unable to connect to server!");
			return;
		}

		//ioThread
		new Thread(){
			@Override public void run(){
				while(!socket.isClosed()){
					try{
						if(in.ready()){
							StringBuilder builder = new StringBuilder();
							for(String line = in.readLine(); line != null; line = in.readLine())
								builder.append(line).append('\n');
							receiver.receiveMessage(ClientMain.this, builder.substring(0, builder.length()-1));
						}
					}
					catch(IOException e){e.printStackTrace();}
				}
				System.out.print("Connection closed.");
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