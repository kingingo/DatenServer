package dev.wolveringer.connection.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.connection.ClientType;

public class ServerThread {
	private static ArrayList<Client> clients = new ArrayList<>();
	
	public static ArrayList<Client> getBungeecords(){
		ArrayList<Client> out = new ArrayList<>();
		for(Client c : new ArrayList<>(clients))
			if(c.getType() == ClientType.BUNGEECORD)
				out.add(c);
		return out;
	}
	
	private ServerSocket socket;
	private InetSocketAddress localAddr;
	private Thread acceptThread;
	
	
	
	public ServerThread(InetSocketAddress localAddr) {
		this.localAddr = localAddr;
	}
	
	public void start() throws IOException{
		socket = new ServerSocket(localAddr.getPort(), 0, localAddr.getAddress());
		acceptThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!socket.isClosed()) {
					try{
						Socket csocket = socket.accept();
						Client client = new Client(csocket, ServerThread.this);
						clients.add(client);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		});
		acceptThread.start();
	}
	
	
	public static void main(String[] args) throws IOException {
		System.out.println("Stating test server");
		ServerThread server = new ServerThread(new InetSocketAddress("localhost", 1111));
		server.start();
	}
}
