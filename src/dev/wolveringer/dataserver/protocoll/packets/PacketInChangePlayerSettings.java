package dev.wolveringer.dataserver.protocoll.packets;

import java.util.UUID;

import dev.wolveringer.dataserver.player.OnlinePlayer.Setting;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketInChangePlayerSettings extends Packet{
	@Getter
	private UUID player;
	@Getter
	private Setting setting;
	@Getter
	private String value;
	
	@Override
	public void read(DataBuffer buffer) {
		player = buffer.readUUID();
		setting = Setting.values()[buffer.readByte()];
		value = buffer.readString();
	}
}
