package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.gamestats.Game;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PacketOutGammodeChange extends Packet{
	private Game game;
	
	@Override
	public void write(DataBuffer buffer) {
		buffer.writeByte(game.ordinal());
	}
	
	@Override
	public void read(DataBuffer buffer) {
		game = Game.values()[buffer.readInt()];
	}
}
