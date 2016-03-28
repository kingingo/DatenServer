package dev.wolveringer.connection.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.connection.Client;

//TODO Bungeecord Server management
//TODO Event Setserver

public class ServerThread {
	private static ArrayList<Client> clients = new ArrayList<>();
	
	public static void registerTestServer(Client client){
		clients.add(client);
	}
	
	public static ArrayList<Client> getBungeecords(){
		return getServer(ClientType.BUNGEECORD);
	}
	
	public static ArrayList<Client> getServer(ClientType type){
		ArrayList<Client> out = new ArrayList<>();
		for(Client c : new ArrayList<>(clients))
			if(c.getType() == type)
				out.add(c);
		return out;
	}

	public static Client getServer(String name){
		for(Client c : new ArrayList<>(clients))
			if(c.getName() != null)
			if(c.getName().equalsIgnoreCase(name))
				return c;
		return null;
	}
	public static void removeServer(Client client) {
		clients.remove(client);
	}
	
	private ServerSocket socket;
	private InetSocketAddress localAddr;
	private Thread acceptThread;
	private Thread timeoutThread;
	
	
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
		timeoutThread = new Thread(){
			public void run() {
				while (!socket.isClosed()) {
					for(Client c : new ArrayList<>(clients)){
						if(c.getLastPingTime() != -1)
							if(System.currentTimeMillis()-c.getLastPingTime()>10000 && c.isConnected()){
								System.out.println("Time out: "+c.getHost()+"["+c.getName()+"]");
								try{
									c.disconnect("Timeout");
								}catch(Exception e){
									e.printStackTrace();
								}
							}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		};
		acceptThread.start();
		timeoutThread.start();
	}
	
	
	public static void main(String[] args) throws IOException {
		System.out.println("Stating test server");
		ServerThread server = new ServerThread(new InetSocketAddress("localhost", 1111));
		server.start();
	}
}
