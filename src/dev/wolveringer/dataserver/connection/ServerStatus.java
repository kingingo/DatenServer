package dev.wolveringer.dataserver.connection;

import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.gamestats.GameState;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatus;
import dev.wolveringer.serverbalancer.AcardeManager;
import dev.wolveringer.serverbalancer.AcardeManager.ServerType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerStatus {
	private int players = -1;
	private int maxPlayers = -1;
	private String mots = "underknown"; //Message of the server :D equals <-> Message of the day (MOTD)
	private GameType typ;
	private GameState state;
	private String subType;
	private boolean visiable = false;
	private Client owner;
	private String serverId = "underknown";
	
	private boolean registered = false;
	
	public ServerStatus(Client owner){
		this.owner = owner;
	}

	public void applayPacket(PacketInServerStatus packet) { //TODO Bitmask
		this.players = packet.getPlayers();
		this.maxPlayers = packet.getMaxPlayers();
		this.mots = packet.getMots();
		this.typ = packet.getTyp();
		this.visiable = packet.isListed();
		this.state = packet.getState();
		this.serverId = packet.getServerId();
		this.subType = packet.getSubstate();
		if(!registered && typ != null && owner.getType() == ClientType.ACARDE){
			AcardeManager.serverConnected(new ServerType(typ, subType));
			registered = true;
		}
	}

	@Override
	public String toString() {
		return "ServerStatus [players=" + players + ", maxPlayers=" + maxPlayers + ", mots=" + mots + ", typ=" + typ + ", state=" + state + ", subType=" + subType + ", visiable=" + visiable + ", owner=" + owner + ", serverId=" + serverId + ", registered=" + registered + "]";
	}
}
