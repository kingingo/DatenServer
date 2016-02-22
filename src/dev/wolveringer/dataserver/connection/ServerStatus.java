package dev.wolveringer.dataserver.connection;

import dev.wolveringer.dataserver.gamestats.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerStatus {
	private int players;
	private int maxPlayers;
	private String mots; //Message of the server :D equals <-> Message of the day (MOTD)
	private Game typ;
	private boolean lobby;
	private Client owner;
	
	public ServerStatus(Client owner){
		this.owner = owner;
	}
}
