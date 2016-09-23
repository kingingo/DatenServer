package dev.wolveringer.dataserver.connection;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.util.logging.Level;

import com.google.common.primitives.UnsignedInteger;

import dev.wolveringer.dataserver.Main;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.dataserver.protocoll.packets.Packet;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPacketStatus;
import dev.wolveringer.dataserver.protocoll.packets.Packet.PacketDirection;

public class ReaderThread {
	private Client client;
	private InputStream in;
	private DataInputStream din;
	private Thread reader;
	private boolean active;

	public ReaderThread(Client client, InputStream in) {
		this.client = client;
		this.in = in;
		this.din = new DataInputStream(in);
		init();
	}

	private void init() {
		reader = new Thread(new Runnable() {
			@Override
			public void run() {
				//System.out.println("§aReaded thread started!");
				try {
					while (active) {
						if (in.available() > 0 || true)
							readPacket();
						else
							Thread.sleep(10);
					}
				} catch (Exception e) {
					if (!active)
						return;
					System.err.println("Broken reader! Exception message: " + e.getMessage());
					e.printStackTrace();
					close0();
				}
				//System.out.println("§cReaded thread closed!");
			}
		});
	}

	private void readPacket() throws IOException {
		int length = readInt();
		if (length <= 0) {
			int contollLength = readInt();
			if (contollLength != Math.abs(length)) {
				throw new RuntimeException("Reader index wrong (Wrong length (" + length + "/" + contollLength + "), Info [Name: "+client.getName()+", IP: "+client.getHost()+"])");
			}
			length = contollLength;
		}
		if (length > 65536) {
			System.out.println("Try to read a too long packet (Length: " + length + ")");
			this.in.skip(length);
			return;
		}
		final byte[] bbuffer = new byte[length];
		this.din.readFully(bbuffer, 0, length);
		ThreadHandleManager.join(new Runnable() {
			public void run() {
				if (!ReaderThread.this.active) {
					return;
				}
				DataBuffer buffer = new DataBuffer(bbuffer);
				int id = 0;
				Packet packet = Packet.createPacket(id = buffer.readInt(), buffer, Packet.PacketDirection.TO_SERVER);
				if (packet == null) {
					System.out.println("packet is null! (Seems like packet not found!) (IP: "
							+ ReaderThread.this.client.host + "/" + ReaderThread.this.client.getRemoteAdress() + ")");
				}
				try {
					ReaderThread.this.client.getHandlerBoss().handle(packet);
				} catch (Exception e) {
					int length = Math.min(e.getStackTrace().length + 1, 10);
					PacketOutPacketStatus.Error[] stack = new PacketOutPacketStatus.Error[length];
					stack[0] = new PacketOutPacketStatus.Error(1, "Exception: " + e.getMessage());
					for (int i = 1; i < length; i++) {
						stack[i] = new PacketOutPacketStatus.Error(2, e.getStackTrace()[(i - 1)].toString());
					}
					ReaderThread.this.client.writePacket(new PacketOutPacketStatus(packet, stack));
					System.err.println("Error while handeling packet" + Integer.toHexString(id) + " (Client: "
							+ ReaderThread.this.client.getName() + ")");
					e.printStackTrace();
				}
			}
		});
	}
	
	 public final int readInt() throws IOException {
	        int ch1 = in.read();
	        int ch2 = in.read();
	        int ch3 = in.read();
	        int ch4 = in.read();
	        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
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
