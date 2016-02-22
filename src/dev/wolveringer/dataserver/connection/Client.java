package dev.wolveringer.dataserver.connection;

import java.io.IOException;
import java.net.Socket;

import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.gamestats.Game;
import dev.wolveringer.dataserver.protocoll.packets.Packet;
import dev.wolveringer.dataserver.protocoll.packets.PacketDisconnect;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatus;
import lombok.Getter;

public class Client {
	private Socket socket;
	private ReaderThread reader;
	private SocketWriter writer;
	private PacketHandlerBoss boss;
	@Getter
	protected ClientType type;
	@Getter
	protected String host;
	@Getter
	protected String name;
	private ServerThread server;
	private ServerStatus status;
	
	public Client(Socket socket,ServerThread owner) {
		this.socket = socket;
		this.server = owner;
		if(owner == null && server == null)
			return; //TESTING MODE
		try{
			this.writer = new SocketWriter(this, socket.getOutputStream());
			this.reader = new ReaderThread(this, socket.getInputStream());
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		this.boss = new PacketHandlerBoss(this);
		this.reader.start();
	}	
	
	public void disconnect(){
		if(server == null && server == null)
			return; //TESTING MODE
		disconnect(null);
	}
	
	public void disconnect(String message){
		if(server == null && server == null)
			return; //TESTING MODE
		writePacket(new PacketDisconnect(message));
	}
	
	protected void closePipeline(){
		if(server == null && server == null)
			return; //TESTING MODE
		reader.close();
		writer.close();
	}
	
	public void writePacket(Packet packet){
		if(server == null && server == null)
			return; //TESTING MODE
		try {
			writer.write(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	protected PacketHandlerBoss getHandlerBoss(){
		return boss;
	}
	
	public ServerStatus getStatus() {
		return status;
	}

	public void setGame(Game game) {
		System.out.println("Game setting not implimented");
	}
}
