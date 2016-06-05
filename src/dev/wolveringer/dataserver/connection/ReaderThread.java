package dev.wolveringer.dataserver.connection;

import java.io.IOException;
import java.io.InputStream;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.dataserver.protocoll.packets.Packet;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPacketStatus;
import dev.wolveringer.dataserver.protocoll.packets.Packet.PacketDirection;

public class ReaderThread {
	private Client client;
	private InputStream in;
	private Thread reader;
	private boolean active;

	public ReaderThread(Client client, InputStream in) {
		this.client = client;
		this.in = in;
		init();
	}

	private void init() {
		reader = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (active) {
						if (in.available() > 0)
							readPacket();
						else
							Thread.sleep(10);
					}
				} catch (Exception e) {
					if (!active)
						return;
					System.err.println("Broken reader! Exception message: "+e.getMessage());
					e.printStackTrace();
					close0();
				}
			}
		});
	}

	private void readPacket() throws IOException {
		int length = (in.read() << 24) & 0xff000000 | (in.read() << 16) & 0x00ff0000 | (in.read() << 8) & 0x0000ff00 | (in.read() << 0) & 0x000000ff;
		if (length <= 0) {
			throw new RuntimeException("Reader index wrong (Wrong length (" + length + "))");
		}
		byte[] bbuffer = new byte[length];
		in.read(bbuffer, 0, length);
		ThreadHandleManager.join(new Runnable() {
			@Override
			public void run() {
				if (!active) //Drop packet
					return;
				DataBuffer buffer = new DataBuffer(bbuffer);
				int id = 0;
				Packet packet = Packet.createPacket(id = buffer.readInt(), buffer, PacketDirection.TO_SERVER);
				try {
					client.getHandlerBoss().handle(packet);
				} catch (Exception e) {
					int length = Math.min(e.getStackTrace().length+1, 10);
					PacketOutPacketStatus.Error[] stack = new PacketOutPacketStatus.Error[length];
					stack[0] = new PacketOutPacketStatus.Error(1, "Exception: " + e.getMessage());
					for (int i = 1; i < length; i++)
						stack[i] = new PacketOutPacketStatus.Error(2, e.getStackTrace()[i-1].toString());
					client.writePacket(new PacketOutPacketStatus(packet, stack));
					System.err.println("Error while handeling packet" + Integer.toHexString(id)+" (Client: "+client.getName()+")");
					e.printStackTrace();
				}
			}
		});
	}

	public void start() {
		if (!active) {
			active = true;
			reader.start();
		}
	}

	public void close() {
		if (active)
			close0();
	}

	private void close0() {
		active = false;
		if (in != null)
			try {
				in.close();
			} catch (Exception e) {
			}
		if (reader != null) {
			reader.interrupt();
		}
	}

}
