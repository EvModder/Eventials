package Eventials.bridge.basics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class ServerMain extends Connection{
	//=========== Added main function =============================================
	public static void main(String[] args){
		ServerMain server = new ServerMain(new MessageReceiver(){
		@Override public void receiveMessage(MessageSender client, String message) {
			System.out.println("Received from client: "+message);
		}}, 42374, 2);

		Scanner scan = new Scanner(System.in);
		while(scan.hasNextLine()){
			server.sendToAll(scan.nextLine());
		}
		scan.close();
	}
	//=============================================================================

	final int PORT, MAX_CLIENTS;
	ServerSocket socket;
	List<Client> clients;
	Thread connectionWaitThread, ioThread;
	StringBuilder outgoing;

	class Client implements MessageSender{
		Socket socket;
		PrintWriter out;
		BufferedReader in;
		Client(Socket connection){
			socket = connection;
			try{
				out = new PrintWriter(connection.getOutputStream());
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			}
			catch(IOException e){e.printStackTrace();}
		}

		@Override public void sendMessage(MessageReceiver source, String message){
			out.println(message);
			out.flush();
		}
	}

	public ServerMain(MessageReceiver recv, int port, int maxClients){
		PORT = port;
		MAX_CLIENTS = maxClients;
		receiver = recv;
		try{socket = new ServerSocket(port);}
		catch(IOException e){e.printStackTrace();return;}

		clients = new ArrayList<Client>();
		connectionWaitThread = new Thread(){
			@Override public void run(){
				try{
					while(true){
						Socket connection = socket.accept();
						if(clients.size() == MAX_CLIENTS){
							PrintWriter temp = new PrintWriter(connection.getOutputStream());
							temp.println("Server is full!");
							temp.flush();
							connection.close();
							continue;
						}
						synchronized(clients){
							clients.add(new Client(connection));
						}
						System.out.println("Got a connection to a client");
					}
				}
				catch(IOException e){e.printStackTrace();}
			}
		};
		connectionWaitThread.start();

		ioThread = new Thread(){
			@Override public void run(){
				while(!socket.isClosed()){
					loop();
				}
			}
		};
		ioThread.start();

		System.out.println("Server opened on port "+port);
	}

	void loop(){
		synchronized(clients){
			Iterator<Client> it = clients.iterator();
			while(it.hasNext()){
				Client client = it.next();
				try{
					if(client.socket.isClosed()){
						it.remove();
						System.out.println("A client left the server");
					}
					else{
						if(client.in.ready()){
							StringBuilder builder = new StringBuilder("");
							boolean windums = false;
							for(char c = (char)client.in.read(); c != '\n'; c = (char)client.in.read()){
								if(windums) builder.append('\r');
								if(!(windums = (c == '\r'))) builder.append(c);
							}
							receiver.receiveMessage(client, builder.toString());
						}
						if(outgoing != null){
							client.out.print(outgoing.toString());
							client.out.flush();
						}
					}
				}
				catch(IOException e){e.printStackTrace();}
			}
			outgoing = null;
		}
	}

	public int numClients(){
		return clients.size();
	}

	public void sendToAll(String message){
		if(outgoing == null) outgoing = new StringBuilder(message).append('\n');
		else outgoing.append(message).append('\n');
	}

	@Override @SuppressWarnings("deprecation")
	public void close(){
		connectionWaitThread.stop();
		if(socket != null) try{socket.close();} catch(IOException e){}
	}

	@Override
	public boolean isClosed(){
		return socket == null || socket.isClosed() || clients == null || clients.isEmpty();
	}
}