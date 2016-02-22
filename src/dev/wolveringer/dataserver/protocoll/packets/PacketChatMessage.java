package dev.wolveringer.dataserver.protocoll.packets;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class PacketChatMessage extends Packet{
	@AllArgsConstructor
	@Getter
	public static class Target {
		private TargetType type;
		private String permission = null;
		private String target = null;
	}
	public static enum TargetType {
		PLAYER,
		BROTCAST;
	}
	
	@Getter
	private String message;
	@Getter
	private Target[] targets;
	
	@Override
	public void read(DataBuffer buffer) {
		targets = new Target[buffer.readByte()];
		for(int i = 0;i<targets.length;i++)
			targets[i] = new Target(TargetType.values()[buffer.readByte()], buffer.readString(),buffer.readString());
		message = buffer.readString();
	}
	public void write(DataBuffer buffer) {
		buffer.writeByte(targets.length);
		for(int i = 0;i<targets.length;i++){
			buffer.writeByte(targets[i].type.ordinal());
			buffer.writeString(targets[i].permission);
			buffer.writeString(targets[i].target);
		}
		buffer.writeString(message);
	}
}