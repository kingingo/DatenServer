package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PacketInUUIDRequest extends Packet {
	private String[] players;

	@Override
	public void read(DataBuffer buffer) {
		players = new String[buffer.readByte()];
		for(int i = 0;i<players.length;i++)
			players[i] = buffer.readString();
	}
}
