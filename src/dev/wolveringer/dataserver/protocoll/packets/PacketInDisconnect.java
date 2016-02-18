package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketInDisconnect extends Packet{
	private String reson = null;
	
	@Override
	public void read(DataBuffer buffer) {
		reson = buffer.readString();
	}
	
	public String getReson() {
		return reson;
	}
}
