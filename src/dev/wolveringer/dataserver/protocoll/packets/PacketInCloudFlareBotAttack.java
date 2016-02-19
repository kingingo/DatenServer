package dev.wolveringer.dataserver.protocoll.packets;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketInCloudFlareBotAttack extends Packet{
	private boolean attack;
	
	public void read(dev.wolveringer.dataserver.protocoll.DataBuffer buffer) {
		attack = buffer.readBoolean();
	};
	
	public boolean attackActive() {
		return attack;
	}
}
