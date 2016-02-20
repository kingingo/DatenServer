package dev.wolveringer.dataserver.protocoll.packets;

import java.lang.reflect.Constructor;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.Getter;

public abstract class Packet {
	static enum PacketDirection {
		TO_CLIENT,
		TO_SERVER;
		private PacketDirection() {}
	}
	
	private static Constructor<? extends Packet> inPackets[] = new Constructor[256];
	private static Constructor<? extends Packet> outPackets[] = new Constructor[256];
	
	public static Packet createPacket(int id, DataBuffer buffer) {
		try {
			Constructor<? extends Packet> c = inPackets[id];
			if (c != null) {
				Packet packet = c.newInstance();
				packet.packetUUID = buffer.readUUID();
				packet.read(buffer);
				return packet;
			} else
				System.out.println("Packet 0x" + (Integer.toHexString(id).toUpperCase()) + " not found");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getPacketId(Packet packet) {
		int i = 0;
		for (Constructor<?> c : outPackets) {
			if (c != null)
				if (c.getDeclaringClass().equals(packet.getClass()))
					return i;
			i++;
		}
		return -1;
	}
	
	protected static void registerPacket(int id,Class<? extends Packet> packet,PacketDirection direction){
		try {
			if(direction == PacketDirection.TO_CLIENT)
				outPackets[id] = packet.getConstructors().length == 1 ? (Constructor<? extends Packet>) packet.getConstructors()[0] : packet.getConstructor();
			else
				inPackets[id] = packet.getConstructors().length == 1 ? (Constructor<? extends Packet>) packet.getConstructors()[0] : packet.getConstructor();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	static {
		registerPacket(0xFF, PacketDisconnect.class, PacketDirection.TO_CLIENT);
		registerPacket(0xFF, PacketDisconnect.class, PacketDirection.TO_SERVER);
		
		registerPacket(0x00, PacketHandschakeInStart.class, PacketDirection.TO_SERVER);
		
		registerPacket(0x01, PacketInBanStatsRequest.class, PacketDirection.TO_SERVER);
		registerPacket(0x02, PacketInChangePlayerSettings.class, PacketDirection.TO_SERVER);
		registerPacket(0x03, PacketInPlayerSettingsRequest.class, PacketDirection.TO_SERVER);
		registerPacket(0x04, PacketInConnectionStatus.class,  PacketDirection.TO_SERVER);
		registerPacket(0x05, PacketInServerSwitch.class, PacketDirection.TO_SERVER);
		registerPacket(0x06, PacketInStatsEdit.class, PacketDirection.TO_SERVER);
		registerPacket(0x07, PacketInStatsRequest.class, PacketDirection.TO_SERVER);
		registerPacket(0x08, PacketInUUIDRequest.class, PacketDirection.TO_SERVER);
		
		registerPacket(0xF0, PacketOutPacketStatus.class, PacketDirection.TO_CLIENT);
		registerPacket(0x00, PacketOutHandschakeAccept.class, PacketDirection.TO_CLIENT);
		registerPacket(0x01, PacketOutStats.class, PacketDirection.TO_CLIENT);
		registerPacket(0x02, PacketOutPlayerSettings.class, PacketDirection.TO_CLIENT);
		registerPacket(0x03, PacketOutUUIDResponse.class, PacketDirection.TO_CLIENT);
		
		registerPacket(0xFF, PacketDisconnect.class, PacketDirection.TO_CLIENT);
		registerPacket(0xFF, PacketDisconnect.class, PacketDirection.TO_SERVER);
	}

	@Getter
	private UUID packetUUID = UUID.randomUUID();
	
	public Packet() {
	}
	
	public void read(DataBuffer buffer){
		throw new NullPointerException("Packet is write only");
	}
	public void write(DataBuffer buffer){
		throw new NullPointerException("Packet is read only");
	}
	//TODO
	//Messages
	//Bann
}
