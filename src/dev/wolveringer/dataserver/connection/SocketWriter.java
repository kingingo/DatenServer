package dev.wolveringer.dataserver.connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.dataserver.protocoll.packets.Packet;

public class SocketWriter {
	private Client owner;
	private OutputStream out;

	public SocketWriter(Client owner, OutputStream os) {
		this.owner = owner;
		this.out = os;
	}

	public void write(Packet packet) throws IOException {
		int id = Packet.getPacketId(packet);

		if (id == -1) {
			System.out.println("Cant find Packet: " + packet);
			return;
		}

		DataBuffer dbuffer = new DataBuffer();
		dbuffer.writeInt(id);
		dbuffer.writeUUID(packet.getPacketUUID());
		packet.write(dbuffer);
		dbuffer.resetReaderIndex();

		ByteArrayOutputStream os = new ByteArrayOutputStream(4 + dbuffer.writerIndex()); // [INT(Length)|4][DATA|~]
		os.write(new byte[] { (byte) (dbuffer.writerIndex() >>> 24), (byte) (dbuffer.writerIndex() >>> 16), (byte) (dbuffer.writerIndex() >>> 8), (byte) dbuffer.writerIndex() });
		os.write(dbuffer.array(),0,dbuffer.writerIndex());
		out.write(os.toByteArray());
		out.flush();
	}

	public void close() {
		try{
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
