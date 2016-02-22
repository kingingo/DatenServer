package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.gamestats.Game;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PacketInServerStatus extends Packet{
	private int bitmask = 0; //TODO minimize data
	private int players;
	private int maxPlayers;
	private String mots; //Message of the server :D equals <-> Message of the day (MOTD)
	private Game typ;
	private boolean lobby;
	
	@Override
	public void read(DataBuffer buffer) {
		bitmask = buffer.readByte();
		players = buffer.readInt();
		maxPlayers = buffer.readInt();
		mots = buffer.readString();
		typ = Game.values()[buffer.readByte()];
		lobby = buffer.readBoolean();
	}
	@Override
	public void write(DataBuffer buffer) {
		buffer.writeInt(players);
		buffer.writeInt(maxPlayers);
		buffer.writeString(mots);
		buffer.writeByte(typ.ordinal());
		buffer.writeBoolean(lobby);
	}
}
