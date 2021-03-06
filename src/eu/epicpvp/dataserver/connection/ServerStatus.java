package eu.epicpvp.dataserver.connection;

import eu.epicpvp.dataserver.protocoll.packets.PacketInServerStatus;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameState;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;
import eu.epicpvp.serverbalancer.ArcadeManager;
import eu.epicpvp.serverbalancer.ArcadeManager.ServerType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerStatus {
	private int players = -1;
	private int maxPlayers = -1;
	private String mots = "unknown"; //Message of the server :D equals <-> Message of the day (MOTD)
	private GameType typ;
	private GameState state;
	private String subType;
	private boolean visiable = false;
	private Client owner;
	private String serverId = "unknown";

	private boolean registered = false;

	public ServerStatus(Client owner){
		this.owner = owner;
	}

	public void applyPacket(PacketInServerStatus packet) { //TODO Bitmask
		this.players = packet.getPlayers();
		this.maxPlayers = packet.getMaxPlayers();
		this.mots = packet.getMots();
		this.typ = packet.getTyp();
		this.visiable = packet.isListed();
		this.state = packet.getState();
		this.serverId = packet.getServerId();
		this.subType = packet.getSubstate();
		if(!registered && typ != null && owner.getType() == ClientType.ACARDE){
			ArcadeManager.serverConnected(new ServerType(typ, subType));
			registered = true;
		}
	}

	@Override
	public String toString() {
		return "ServerStatus [players=" + players + ", maxPlayers=" + maxPlayers + ", mots=" + mots + ", typ=" + typ + ", state=" + state + ", subType=" + subType + ", visiable=" + visiable + ", owner=" + owner + ", serverId=" + serverId + ", registered=" + registered + "]";
	}
}
