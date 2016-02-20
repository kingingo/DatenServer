package dev.wolveringer.dataserver.protocoll.packets;

import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PacketOutUUIDResponse extends Packet{
	@Getter
	@AllArgsConstructor
	public static class UUIDKey {
		private String name;
		private UUID uuid;
	}
	
	private UUIDKey[] uuids = null;
	
	@Override
	public void write(DataBuffer buffer) {
		buffer.writeByte(uuids.length);
		for(UUIDKey k : uuids){
			buffer.writeString(k.name);
			buffer.writeUUID(k.uuid);
		}
	}
}
