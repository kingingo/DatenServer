package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.gamestats.Game;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PacketInServerStatus extends Packet{
	private int bitmask = 0; //TODO minimize data

	public static enum GameState {
		Laden("Laden"), LobbyPhase("LobbyPhase"), SchutzModus("SchutzModus"), DeathMatch("DeathMatch"), StartDeathMatch("StartDeathMatch"), StartGame("StartGame"), InGame("InGame"), Restart("Restart"), MultiGame("MultiGame"), NONE("NONE");

		String state;

		private GameState(String state) {
			this.state = state;
		}

		public String string() {
			return state;
		}

	}

	private int players;
	private int maxPlayers;
	private String mots; //Message of the server :D equals? <-> Message of the day (MOTD)
	private Game typ;
	private GameState state;
	private boolean listed;
	private String serverId;
	
	@Override
	public void read(DataBuffer buffer) {
		bitmask = buffer.readByte();
		players = buffer.readInt();
		maxPlayers = buffer.readInt();
		mots = buffer.readString();
		typ = Game.values()[buffer.readByte()];
		state = GameState.values()[buffer.readByte()];
		serverId = buffer.readString();
		listed = buffer.readBoolean();
	}

	@Override
	public void write(DataBuffer buffer) {
		buffer.writeByte(bitmask);
		buffer.writeInt(players);
		buffer.writeInt(maxPlayers);
		buffer.writeString(mots);
		buffer.writeByte(typ.ordinal());
		buffer.writeByte(state.ordinal());
		buffer.writeString(serverId);
		buffer.writeBoolean(listed);
	}
}
