package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketInConnectionStatus extends Packet{
	public static enum Status {
		CONNECTED,
		DISCONNECTED;
		private Status() {}
	}
	@Getter
	private String player;
	@Getter
	private Status status;
	
	@Override
	public void read(DataBuffer buffer) {
		player = buffer.readString();
		status = Status.values()[buffer.readByte()];
	}
}
