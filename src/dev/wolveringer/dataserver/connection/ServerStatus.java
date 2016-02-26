package dev.wolveringer.dataserver.connection;

import dev.wolveringer.dataserver.gamestats.Game;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatus.GameState;
import dev.wolveringer.serverbalancer.AcardeManager;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerStatus {
	private int players = -1;
	private int maxPlayers = -1;
	private String mots = "underknown"; //Message of the server :D equals <-> Message of the day (MOTD)
	private Game typ;
	private GameState state;
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
		if(!registered && typ != null && owner.getType() == ClientType.ACARDE){
			AcardeManager.serverConnected(typ);
			registered = true;
		}
	}
}
