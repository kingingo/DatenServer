package dev.wolveringer.dataserver.protocoll.packets;

import java.util.UUID;

import dev.wolveringer.dataserver.player.OnlinePlayer.Setting;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;

public class PacketInPlayerSettingsRequest extends Packet{
	@Getter
	private UUID player;
	@Getter
	private Setting[] settings;
	
	@Override
	public void read(DataBuffer buffer) {
		player = buffer.readUUID();
		settings = new Setting[buffer.readByte()];
		for(int i = 0;i<settings.length;i++)
			settings[i] = Setting.values()[buffer.readByte()];
	}
}
