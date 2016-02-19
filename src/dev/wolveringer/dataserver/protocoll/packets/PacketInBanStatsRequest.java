package dev.wolveringer.dataserver.protocoll.packets;

import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketInBanStatsRequest extends Packet{
	@Getter
	private UUID player;
	@Getter
	private String ip;
	
	@Override
	public void read(DataBuffer buffer) {
		player = buffer.readUUID();
		ip = buffer.readString();
	}
}
