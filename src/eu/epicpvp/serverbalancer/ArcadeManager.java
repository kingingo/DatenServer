package eu.epicpvp.serverbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import eu.epicpvp.connection.server.ServerThread;
import eu.epicpvp.dataserver.connection.Client;
import eu.epicpvp.datenserver.definitions.arrays.CachedArrayList;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameState;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ArcadeManager {

	@AllArgsConstructor
	@Getter
	public static class ServerType {

		private GameType type;
		private String modifier;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((modifier == null) ? 0 : modifier.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ServerType other = (ServerType) obj;
			if (modifier == null) {
				if (other.modifier != null)
					return false;
			} else if (!modifier.equals(other.modifier))
				return false;
			if (type != other.type)
				return false;
			return true;
		}
	}

	private static final int MIN_FREE_SERVER = 2;

	private static HashMap<ServerType, ArrayList<Client>> lastCalculated = new HashMap<>();
	private static ArrayList<ServerType> types = new ArrayList<>();
	private static HashMap<GameType, ArrayList<String>> blacklist = new HashMap<GameType, ArrayList<String>>() {
		public java.util.ArrayList<String> get(Object key) {
			ArrayList<String> out = super.get(key);
			if (out == null)
				super.put((GameType) key, out = new ArrayList<>());
			return out;
		}

		;
	};

	static {
		//types.add(new ServerType(GameType.BedWars, "2x1"));
		//types.add(new ServerType(GameType.BedWars, "2x2"));
		//types.add(new ServerType(GameType.BedWars, "2x4"));
		//types.add(new ServerType(GameType.BedWars, "4x4"));
	}

	public static HashMap<ServerType, ArrayList<Client>> getLastCalculated() {
		return lastCalculated;
	}

	@SuppressWarnings("serial")
	private static HashMap<ServerType, CachedArrayList<UUID>> serverChanging = new HashMap<ServerType, CachedArrayList<UUID>>() {
		public CachedArrayList<UUID> get(Object key) {
			CachedArrayList<UUID> arraylist = super.get(key);
			if (arraylist == null) {
				arraylist = new CachedArrayList<>(1, TimeUnit.MINUTES);
				super.put((ServerType) key, arraylist);
			}
			return arraylist;
		}

		;
	};
	private static CachedArrayList<String> notFree = new CachedArrayList<>(1, TimeUnit.MINUTES);

	public static void serverConnected(ServerType game) {
		if (serverChanging.get(game).size() > 0)
			serverChanging.get(game).remove(0);
	}

	public static void serverDisconnected(String name) {
		notFree.remove(name);
	}

	@SuppressWarnings("unchecked")
	public static HashMap<ServerType, ArrayList<Client>> balance() {
		HashMap<ServerType, ArrayList<Client>> servers = buildGameServerLobbyIndex();
		if (servers.isEmpty()) //Keine server registriert
			return null;
		sortLobbysByPlayers(servers);
		ArrayList<Client> var0 = buildFreeServerIndex(servers); //Cut server > MIN_FREE_SERVER
		servers = shortByNeeded(servers); //Short games by needed Server
		int freeServersLeft = var0.size();
		Iterator<Client> freeServer = var0.iterator();

		int oneeded = calculateNeededServers(servers); //Orginal needed
		int needed = oneeded;
		if (needed == 0)
			needed = servers.size(); //Adding left servers to games

		while (needed > 0 && freeServersLeft > 0) {
			int filled = 0;
			for (ServerType game : types) {
				if (!game.getType().isArcade())
					continue;
				if (blacklist.get(game.getType()).contains(game.getModifier())) {
					System.out.println("Skipping: " + game.getType() + "-" + game.getModifier());
					continue;
				}
				if (freeServersLeft <= 0)
					continue;
				if (!servers.containsKey(game))
					servers.put(game, new CachedArrayList<>(1, TimeUnit.MINUTES));
				if (servers.get(game).size() + serverChanging.get(game).size() < getMinServer(game)) {
					filled++;
					needed -= 1;
					freeServersLeft--;
					if (!freeServer.hasNext())
						continue;
					Client next = freeServer.next();
					System.out.println("Setting " + next.getName() + " to " + game.getType() + "[" + game.getModifier() + "]");
					serverChanging.get(game).add(UUID.randomUUID());
					notFree.add(next.getName());
					next.setGame(game.getType(), game.getModifier());
				}
			}
			if (filled == 0)
				break;
			filled = 0;
			System.out.println(needed + ":" + freeServersLeft);
		}
		lastCalculated = (HashMap<ServerType, ArrayList<Client>>) servers.clone();
		if (oneeded != 0 || freeServersLeft <= 5) {
			System.out.println("Free Server left: " + freeServersLeft + " Orginal needed Servers: " + oneeded);
		}
		return servers;
	}

	private static int calculateNeededServers(HashMap<ServerType, ArrayList<Client>> servers) {
		int out = 0;
		for (ServerType g : servers.keySet())
			out += calculateNeeded(servers, g);
		return out;
	}

	private static HashMap<ServerType, ArrayList<Client>> shortByNeeded(HashMap<ServerType, ArrayList<Client>> servers) {
		return (HashMap<ServerType, ArrayList<Client>>) sortByValue((Map) servers, servers);
	}

	public static <K, V> Map<K, V> sortByValue(Map<K, V> map, HashMap<ServerType, ArrayList<Client>> servers) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				ServerType game1 = (ServerType) o1.getKey();
				ServerType game2 = (ServerType) o2.getKey();
				return Integer.compare(calculateNeeded(servers, game1), calculateNeeded(servers, game2));
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	private static int calculateNeeded(HashMap<ServerType, ArrayList<Client>> server, ServerType game) {
		return Math.max(0, getMinServer(game) - (server.get(game).size()) - serverChanging.get(game).size());
	}

	private static int getMinServer(ServerType game) {
		return MIN_FREE_SERVER;
	}

	private static void sortLobbysByPlayers(HashMap<ServerType, ArrayList<Client>> servers) {
		for (ServerType g : servers.keySet())
			Collections.sort(servers.get(g), new Comparator<Client>() {
				@Override
				public int compare(Client o1, Client o2) {
					return Integer.compare(o2.getStatus().getPlayers(), o1.getStatus().getPlayers()); //Short 3>2>1
				}
			});
	}

	private static ArrayList<Client> buildFreeServerIndex(HashMap<ServerType, ArrayList<Client>> servers) {
		ArrayList<Client> out = new ArrayList<>();
		for (ServerType g : servers.keySet()) {
			ArrayList<Client> i = servers.get(g);
			if (i.size() > getMinServer(g)) {
				List<Client> ft = new ArrayList<>(i.subList(getMinServer(g), i.size())); //Serers > 3 (min-Server anzahl) werden nicht benötigt (Auf diesem Server dürften sowieso keine Spieler sein)
				i.removeAll(ft);
				out.addAll(ft); //Adding as free-server
			}
		}
		return out;
	}

	private static HashMap<ServerType, ArrayList<Client>> buildGameServerLobbyIndex() {
		HashMap<ServerType, ArrayList<Client>> out = new HashMap<ServerType, ArrayList<Client>>() {
			public ArrayList<Client> get(Object key) {
				ArrayList<Client> arraylist = super.get(key);
				if (arraylist == null) {
					arraylist = new CachedArrayList<>(1, TimeUnit.MINUTES);
					super.put((ServerType) key, arraylist);
				}
				return arraylist;
			}

			;
		};

		for (Client c : ServerThread.getServer(ClientType.ACARDE)) {
			if (c.isConnected() && c.getStatus() != null && c.getStatus().getTyp() != null && c.getStatus().isVisiable() && c.getStatus().getState() == GameState.LobbyPhase && !notFree.contains(c.getName())) {
				out.get(getType(c.getStatus().getTyp(), c.getStatus().getSubType())).add(c);
			}
		}

		return out;
	}

	public static void writeServers() {
		if (lastCalculated.isEmpty()) //Keine Server registriert
			System.out.println("No servers");
		List<Map.Entry<ServerType, ArrayList<Client>>> entries = new ArrayList<>(lastCalculated.entrySet());
		entries.sort((o1, o2) -> o1.getKey().getType().toString().compareTo(o2.getKey().getType().toString()));
		for (Map.Entry<ServerType, ArrayList<Client>> entry : entries) {
			ServerType serverType = entry.getKey();
			if (blacklist.get(serverType.getType()).contains(serverType.getModifier()))
				System.out.println("§cGame: §6" + serverType.getType() + "§7[§e" + serverType.getModifier() + "§7] §cServer: §6" + entry.getValue());
			else
				System.out.println("§aGame: §6" + serverType.getType() + "§7[§e" + serverType.getModifier() + "§7] §aServer: §6" + entry.getValue());
		}
		List<Client> arcadeServers = ServerThread.getServer(ClientType.ACARDE);
		System.out.println("§aConnected: §6" + arcadeServers.size() + " §aList: §f" + arcadeServers);
		for (GameState state : GameState.values()) {
			List<Client> list = arcadeServers.stream().filter(client -> client.getStatus().getState() == state).collect(Collectors.toList());
			if (!list.isEmpty()) {
				System.out.println("§a" + state.toString() + ": §6" + list.size() + "§a List: §f" + list);
			}
		}
	}

	private static ServerType getType(GameType state, String modifier) {
		ServerType currunt = new ServerType(state, modifier);
		if (!types.contains(currunt))
			types.add(currunt);
		return currunt;
	}

	public static HashMap<GameType, ArrayList<String>> getBlackList() {
		return blacklist;
	}
}
