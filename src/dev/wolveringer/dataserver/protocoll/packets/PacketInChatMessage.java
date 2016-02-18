package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketInChatMessage extends Packet{
	@AllArgsConstructor
	@Getter
	public static class Target {
		private TargetType type;
		private String permission;
	}
	public static enum TargetType {
		PLAYER,
		SERVER,
		CONSOLE;
	}
	@Getter
	private Target[] targets;
	@Getter
	private String message;
	
	@Override
	public void read(DataBuffer buffer) {
		targets = new Target[buffer.readByte()];
		for(int i = 0;i<targets.length;i++)
			targets[i] = new Target(TargetType.values()[buffer.readByte()], buffer.readString());
		message = buffer.readString();
	}
}
