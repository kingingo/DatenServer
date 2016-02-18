package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketForward extends Packet{
	//private Packet packet;
	
	@Getter
	private String target;
	private byte[] data;
	
	@Override
	public void read(DataBuffer buffer) {
		target = buffer.readString();
		data = new byte[buffer.readInt()];
		buffer.readBytes(data);
	}
	
	@Override
	public void write(DataBuffer buffer) {
		buffer.writeString(target);
		buffer.writeInt(data.length);
		buffer.writeBytes(data);
	}
}
