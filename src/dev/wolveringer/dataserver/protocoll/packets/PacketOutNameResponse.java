package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutUUIDResponse.UUIDKey;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PacketOutNameResponse extends Packet{
	private UUIDKey[] response;
	
	@Override
	public void write(DataBuffer buffer) {
		buffer.writeByte(response.length);
		for(UUIDKey key : response){
			buffer.writeString(key.getName());
			buffer.writeUUID(key.getUuid());
		}
	}
}
