package dev.wolveringer.dataserver.protocoll.packets;

import java.util.UUID;

import dev.wolveringer.dataserver.gamestats.Game;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PacketInStatsRequest extends Packet{
	private UUID player;
	private Game game;
	
	@Override
	public void read(DataBuffer buffer) {
		player = buffer.readUUID();
		game = Game.values()[buffer.readByte()];
	}
}
