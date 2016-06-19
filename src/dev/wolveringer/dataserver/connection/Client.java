package dev.wolveringer.dataserver.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
import dev.wolveringer.dataserver.protocoll.packets.PacketPong;
import dev.wolveringer.event.EventHandlerBoss;
import dev.wolveringer.serverbalancer.ArcadeManager;
import lombok.Getter;

public class Client {
	private Socket socket;
	private ReaderThread reader;
	private SocketWriter writer;
	@Getter
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
	@Getter
	private EventHandlerBoss eventHander;
	
	public Client(Socket socket,ServerThread owner) {
		this.socket = socket;
		this.server = owner;
		this.connected = true;
		this.status = new ServerStatus(this);
		this.boss = new PacketHandlerBoss(this);
		try{
			this.writer = new SocketWriter(this, socket.getOutputStream());
			this.reader = new ReaderThread(this, socket.getInputStream());
		}catch(Exception e){
			closePipeline();
			e.printStackTrace();
			return;
		}
		this.reader.start();
		this.eventHander = new EventHandlerBoss(this);
		lastPingTime = System.currentTimeMillis();
	}	
	
	public synchronized void disconnect(){
		disconnect(null);
	}
	
	public synchronized void disconnect(String message){
		writePacket(new PacketDisconnect(message));
		closePipeline();
	}
	
	protected synchronized void closePipeline(){
		if(!connected){
			reader.close();
			writer.close();
			System.out.println("invoked closePipeline() -> alredy closed!");
			return;
		}
		connected = false;
		try{
			reader.close();
		}catch(Exception e){
			e.getMessage();
		}
		try{
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ServerThread.removeServer(this);
		ArcadeManager.serverDisconnected(name);
	}
	
	public void writePacket(Packet packet){
		if(server == null && server == null)
			return; //TESTING MODE
		if(!connected)
			return;
		try {
			writer.write(packet);
		} catch (IOException e) {
			if(e.getMessage().equalsIgnoreCase("Broken pipe") || e.getMessage().equalsIgnoreCase("Connection reset")){
				closePipeline();
				return;
			}
			if(e.getMessage().equalsIgnoreCase("Socket closed")){
				closePipeline();
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
		for(OnlinePlayer c : PlayerManager.getPlayers()){
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
	
	public InetAddress getRemoteAdress(){
		return ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress();
	}
	
	public boolean isReachable(int timeout) {
		long sended = System.currentTimeMillis();
		writePacket(new PacketPong(sended));
		while (sended > lastPingTime) {
			if(sended+timeout<System.currentTimeMillis())
				return false;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
		return true;
	}
}
