package eu.epicpvp.connection.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.epicpvp.dataserver.connection.Client;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;

public class ServerThread {

	private static final ArrayList<Client> clients = new ArrayList<>();

	public static void registerTestServer(Client client) {
		synchronized (clients) {
			if (client != null) {
				clients.add(client);
			}
		}
	}

	public static List<Client> getBungeecords() {
		return getServer(ClientType.BUNGEECORD);
	}

	public static List<Client> getServer(ClientType type) {
		List<Client> clients = new ArrayList<>(ServerThread.clients);
		Stream<Client> clientStream =
				clients.stream()
						.filter(Objects::nonNull)
						.filter(client -> {
							if (!client.isConnected() || client.getEventHander() == null) {
								ServerThread.clients.remove(client);
								return false;
							}
							return true;
						});
		if (type != ClientType.ALL) {
			clientStream = clientStream.filter(client -> client != null && client.getType() == type);
		}
		return clientStream
				.filter(Objects::nonNull)
				.sorted((client1, client2) -> nullAlternative(client1.getName(), "").compareTo(nullAlternative(client2.getName(), "")))
				.collect(Collectors.toList());
	}

	private static <T> T nullAlternative(T val, T nullAlt) {
		return val != null ? val : nullAlt;
	}
	public static List<Client> getServer(GameType type) {
		return new ArrayList<>(clients).stream()
				.filter(Objects::nonNull)
				.filter(client -> client.getStatus().getTyp() == type)
				.sorted((client1, client2) -> client1.getName().compareTo(client2.getName()))
				.collect(Collectors.toList());
	}

	public static Client getServer(String name) {
		if (name == null)
			return null;
		for (Client client : new ArrayList<>(clients)) {
			String clientName = client.getName();
			if (clientName != null && clientName.equalsIgnoreCase(name))
				return client;
		}
		return null;
	}

	public static void removeServer(Client client) {
		synchronized (clients) {
			clients.remove(client);
		}
	}

	private ServerSocket socket;
	private InetSocketAddress localAddr;
	private Thread acceptThread;
	private Thread timeoutThread;

	public ServerThread(InetSocketAddress localAddr) {
		this.localAddr = localAddr;
	}

	public void start() throws IOException {
		socket = new ServerSocket(localAddr.getPort(), 0, localAddr.getAddress());
		acceptThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!socket.isClosed()) {
					try {
						Socket csocket = socket.accept();
						Client client = new Client(csocket, ServerThread.this);
						clients.add(client);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		timeoutThread = new Thread() {
			public void run() {
				while (!socket.isClosed()) {
					for (Client c : new ArrayList<>(clients)) {
						if (c.getLastPingTime() != -1)
							if (System.currentTimeMillis() - c.getLastPingTime() > 7500 && c.isConnected()) {
								System.out.println("Client timed out " + c.getName() + "] (" + (System.currentTimeMillis() - c.getLastPingTime()) + ")");
								try {
									c.disconnect("Server -> Client | Timeout!");
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			;
		};
		acceptThread.start();
		timeoutThread.start();
	}

	public void stop() {
		for (Client c : new ArrayList<>(clients))
			c.disconnect("Datenserver is shutting down!");
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		acceptThread.interrupt();
		timeoutThread.interrupt();
	}
}
