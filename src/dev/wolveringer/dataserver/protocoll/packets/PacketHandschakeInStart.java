package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;

public class PacketHandschakeInStart extends Packet{
	@Getter
	private String host;
	@Getter
	private String name;
	@Getter
	private byte[] password;
	@Getter
	private ClientType type;
	@Override
	public void read(DataBuffer buffer) {
		host = buffer.readString();
		name = buffer.readString();
		password = new byte[buffer.readByte()];
		buffer.readBytes(password);
		type = ClientType.values()[buffer.readByte()];
	}
}
