package dev.wolveringer.dataserver.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.protocoll.packets.Packet;
import dev.wolveringer.dataserver.protocoll.packets.PacketDisconnect;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutGammodeChange;
import dev.wolveringer.serverbalancer.AcardeManager;
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
	protected long lastPing = -1;
	protected long lastPingTime = -1;
	private boolean connected;
	
	public Client(Socket socket,ServerThread owner) {
		this.socket = socket;
		this.server = owner;
		this.connected = true;
		this.status = new ServerStatus(this);
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
		closePipeline();
	}
	
	protected void closePipeline(){
		if(server == null && server == null)
			return; //TESTING MODE
		if(!connected){
			reader.close();
			writer.close();
			return;
		}
		connected = false;
		reader.close();
		writer.close();
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ServerThread.removeServer(this);
		AcardeManager.serverDisconnected(name);
	}
	
	public void writePacket(Packet packet){
		if(server == null && server == null)
			return; //TESTING MODE
		if(!connected)
			return;
		try {
			writer.write(packet);
		} catch (IOException e) {
			if(e.getMessage().equalsIgnoreCase("Broken pipe"))
				return;
			if(e.getMessage().equalsIgnoreCase("Socket closed")){
				connected = false;
				return;
			}
			e.printStackTrace();
		}
	}
	protected PacketHandlerBoss getHandlerBoss(){
		return boss;
	}
	
	public ServerStatus getStatus() {
		return status;
	}

	public List<String> getPlayers(){
		ArrayList<String> out = new ArrayList<>();
		for(OnlinePlayer c : PlayerManager.getPlayer()){
			if(c.getPlayerBungeecord() == this && c.isPlaying())
				out.add(c.getName());
		}
		return out;
	}
	
	public long getPing() {
		return lastPing;
	}
	public long getLastPingTime() {
		return lastPingTime;
	}
	public void setGame(GameType game,String subType) {
		writePacket(new PacketOutGammodeChange(game,subType));
	}

	@Override
	public String toString() {
		//return "Client [type=" + type + ", host=" + host + ", name=" + name + ", lastPing=" + lastPing + ",spieler="+status.getPlayers()+",maxSpieler="+status.getMaxPlayers()+",mots="+status.getMots()+"]";
		return ""+name+"";
	}

	public boolean isConnected() {
		return connected;
	}
}
