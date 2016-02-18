package dev.wolveringer.connection.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import dev.wolveringer.dataserver.connection.Client;

public class ServerThread {
	private static ArrayList<Client> clients = new ArrayList<>();
	private ServerSocket socket;
	private InetSocketAddress localAddr;
	private Thread acceptThread;
	
	
	
	public ServerThread(InetSocketAddress localAddr) {
		this.localAddr = localAddr;
	}
	
	public void start(){
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
	
}
