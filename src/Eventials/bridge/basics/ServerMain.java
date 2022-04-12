package Eventials.bridge.basics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class ServerMain extends Connection{
	//=========== Added main function =============================================
	public static void main(String[] args){
		ServerMain server = new ServerMain(new MessageReceiver(){
			Logger logger = Logger.getLogger(ServerMain.class.getName());

			@Override public void receiveMessage(MessageSender client, String message){logger.info("Received from client: "+message);}
			@Override public void serverStarted(){logger.info("Server opened");}
			@Override public void clientConnected(MessageSender client){logger.info("Got a connection to a client");}
			@Override public void clientDisconnected(MessageSender client){logger.info("A client left the server");}
		}, /*port=*/42374, /*MAX_CLIENTS=*/2);

		Scanner scan = new Scanner(System.in);
		String message;
		while(scan.hasNextLine() && !(message=scan.nextLine()).isBlank()){
			server.sendToAll(message);
			System.out.println("Sent: "+message);
		}
		scan.close();
		server.close();
	}
	//=============================================================================

	final int PORT, MAX_CLIENTS;
	ServerSocket socket;
	List<Client> clients;
//	Thread connectionWaitThread;
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

		clients = new ArrayList<>();
		//connectionWaitThread
		new Thread(){
			@Override public void run(){
				try{
					while(!isClosed()){
						Socket connection = socket.accept();
						if(clients.size() == MAX_CLIENTS){
							PrintWriter temp = new PrintWriter(connection.getOutputStream());
							temp.println("Server is full!");
							temp.flush();
							connection.close();
							continue;
						}
						synchronized(clients){
							Client client = new Client(connection);
							clients.add(client);
							recv.clientConnected(client);
						}
					}
				}
				catch(SocketException e){/*server closed*/}
				catch(IOException e){e.printStackTrace();}
			}
		}.start();

		//ioThread
		new Thread(){
			@Override public void run(){
				while(!isClosed()){
					synchronized(clients){
						Iterator<Client> it = clients.iterator();
						while(it.hasNext()){
							Client client = it.next();
							try{
								if(client.socket.isClosed()){
									it.remove();
									receiver.clientDisconnected(client);
								}
								else{
									if(client.in.ready()){
										StringBuilder builder = new StringBuilder("");
										boolean windums = false;
										for(char c = (char)client.in.read(); c != '\n'; c = (char)client.in.read()){
											if(windums) builder.append('\r');
											if(!(windums = (c == '\r'))) builder.append(c);
										}
										final String message = builder.toString();
										if(message.equals(DISCONNECT_KEYWORD)) client.socket.close();
										else receiver.receiveMessage(client, message);
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
			}
		}.start();

		recv.serverStarted();
	}

	public int numClients(){
		return clients.size();
	}

	public void sendToAll(String message){
		if(outgoing == null) outgoing = new StringBuilder(message).append('\n');
		else outgoing.append(message).append('\n');
	}

	@Override
	public void close(){
//		connectionWaitThread.stop();
		sendToAll(DISCONNECT_KEYWORD);
		if(socket != null) try{socket.close();} catch(IOException e){}
		for(Client client : clients) try{client.socket.close();} catch(IOException e){}
	}

	@Override
	public boolean isClosed(){
		return socket == null || socket.isClosed() || clients == null;
	}
}