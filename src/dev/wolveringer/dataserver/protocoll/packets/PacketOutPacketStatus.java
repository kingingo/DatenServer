package dev.wolveringer.dataserver.protocoll.packets;

import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public class PacketOutPacketStatus extends Packet {
	@AllArgsConstructor
	public static class Error {
		@Getter
		private int id;
		@Getter
		private String message;
	}

	@Getter
	private UUID packetId;
	@Getter
	@NonNull
	private Error[] errors;

	public PacketOutPacketStatus(Packet receved, Error... errors) {
		this.packetId = receved.getPacketUUID();
		this.errors = errors;
	}

	@Override
	public void write(DataBuffer buffer) {
		buffer.writeUUID(packetId);
		if (errors != null) {
			buffer.writeByte(errors.length);
			for (Error r : errors) {
				buffer.writeInt(r.id);
				buffer.writeString(r.message);
			}
		} else
			buffer.writeByte(0);
	}
}
