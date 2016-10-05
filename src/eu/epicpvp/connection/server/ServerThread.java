package eu.epicpvp.connection.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import eu.epicpvp.dataserver.connection.Client;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;

public class ServerThread {
	private static ArrayList<Client> clients = new ArrayList<>();

	public static void registerTestServer(Client client){
		synchronized (clients) {
			clients.add(client);
		}
	}

	public static ArrayList<Client> getBungeecords(){
		return getServer(ClientType.BUNGEECORD);
	}

	public static ArrayList<Client> getServer(ClientType type){
		ArrayList<Client> out = new ArrayList<>();
		for(Client c : new ArrayList<>(clients)){
			if(!c.isConnected() || c.getEventHander() == null){
				clients.remove(c);
				continue;
			}
			if(c.getType() == type || type == ClientType.ALL)
				out.add(c);
		}
		return out;
	}

	public static ArrayList<Client> getServer(GameType type){
		ArrayList<Client> out = new ArrayList<>();
		for(Client c : new ArrayList<>(clients))
			if(c.getStatus().getTyp() == type)
				out.add(c);
		return out;
	}


	public static Client getServer(String name){
		if(name == null)
			return null;
		for(Client c : new ArrayList<>(clients))
			if(c.getName() != null)
			if(c.getName().equalsIgnoreCase(name))
				return c;
		return null;
	}

	public static void removeServer(Client client) {
		synchronized (clients) {
			clients.remove(client);
		}
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
							if(System.currentTimeMillis()-c.getLastPingTime()>7500 && c.isConnected()){
								System.out.println("Client timed out "+c.getName()+"] ("+(System.currentTimeMillis()-c.getLastPingTime())+")");
								try{
									c.disconnect("Server -> Client | Timeout!");
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
	public void stop() {
		for(Client c : new ArrayList<>(clients))
			c.disconnect("Datenserver is shutting down!");
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		acceptThread.interrupt();
		timeoutThread.interrupt();
	}
}
