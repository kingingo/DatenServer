package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketForward extends Packet{
	private Packet packet;
	
	@Getter
	private String target;
	@Getter
	private ClientType ctarget;
	
	private byte[] data;
	
	@Override
	public void read(DataBuffer buffer) {
		target = buffer.readString();
		int var0 = buffer.readByte();
		if(var0 != -1)
			ctarget = ClientType.values()[var0];
		data = new byte[buffer.readInt()];
		buffer.readBytes(data);
	}
	
	@Override
	public void write(DataBuffer buffer) {
		buffer.writeString(target);
		if(ctarget == null)
			buffer.writeByte(-1);
		else
			buffer.writeByte(ctarget.ordinal());
		buffer.writeInt(data.length);
		buffer.writeBytes(data);
	}
	
	public Packet getPacket(){
		return packet;
	}
}
