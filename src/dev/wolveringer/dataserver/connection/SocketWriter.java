package dev.wolveringer.dataserver.connection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.dataserver.protocoll.packets.Packet;

public class SocketWriter {
	private Client owner;
	private OutputStream out;
	private DataOutputStream dos;
	private CopyOnWriteArrayList<Packet> queuedPackets = new CopyOnWriteArrayList();
	private boolean active = true;
	private Thread t = new Thread() {
		public void run() {
			//System.out.println("§aStarting writer thread");
			while (SocketWriter.this.active) {
				Packet packet = null;
				Iterator<Packet> queue = SocketWriter.this.queuedPackets.iterator();
				while (queue.hasNext()) {
					Packet next = (Packet) queue.next();
					SocketWriter.this.queuedPackets.remove(next);
					try {
						SocketWriter.this.write0(next);
					} catch (IOException e) {
						if(e instanceof SocketException){
							if("Socket closed".equalsIgnoreCase(e.getMessage())){
								System.err.println("Closed socket but have to write "+(SocketWriter.this.queuedPackets.size()+1)+" packets... :(");
								SocketWriter.this.queuedPackets.clear();
								return;
							}
						}
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(1L);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//System.out.println("§cStopping writer thread");
		}
	};

	public SocketWriter(Client owner, OutputStream os) {
		this.owner = owner;
		this.out = os;
		this.dos = new DataOutputStream(os);
		this.t.start();
	}

	public void write(Packet packet) throws IOException {
		synchronized (this.queuedPackets) {
			this.queuedPackets.add(packet);
		}
	}

	private synchronized void write0(Packet packet) throws IOException {
		int id = Packet.getPacketId(packet, Packet.PacketDirection.TO_CLIENT);
		if (id == -1) {
			System.out.println("Cant find Packet: " + packet);
			return;
		}
		DataBuffer dbuffer = new DataBuffer();
		dbuffer.writeInt(id);
		dbuffer.writeUUID(packet.getPacketUUID());
		packet.write(dbuffer);
		dbuffer.resetReaderIndex();
		if (dbuffer.writerIndex() != dbuffer.readableBytes()) {
			System.err.println(dbuffer.writerIndex() + "/" + dbuffer.readableBytes() + " are not equal");
		}
		int length = dbuffer.writerIndex();
		byte[] buffer = new byte[length];
		dbuffer.readBytes(buffer);
		if (buffer.length != length) {
			System.err.println(buffer.length + "/" + length + " are not equal");
		}
		this.dos.writeInt(length); //TODO write controll length
		this.dos.write(buffer, 0, length);
		this.out.flush();
	}

	public void close() {
		//System.out.println("§cClose writer");
		this.active = false;
		try {
			this.out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
