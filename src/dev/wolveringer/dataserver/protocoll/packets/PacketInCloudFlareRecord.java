package dev.wolveringer.dataserver.protocoll.packets;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketInCloudFlareRecord extends Packet{
	public static enum Action {
		ADD,
		REMOVE;
		private Action() {
			// TODO Auto-generated constructor stub
		}
	}
	
	private Action action;
	private String record;
}
