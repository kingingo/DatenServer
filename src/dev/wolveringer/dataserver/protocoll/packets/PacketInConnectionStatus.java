package dev.wolveringer.dataserver.protocoll.packets;

import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketInConnectionStatus extends Packet{
	public static enum Status {
		CONNECTED,
		DISCONNECTED;
		private Status() {
			// TODO Auto-generated constructor stub
		}
	}
	@Getter
	private UUID player;
	@Getter
	private Status status;
	
	@Override
	public void read(DataBuffer buffer) {
		player = buffer.readUUID();
		status = Status.values()[buffer.readByte()];
	}
}
