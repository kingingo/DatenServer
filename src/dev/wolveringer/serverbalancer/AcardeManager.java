package dev.wolveringer.serverbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.connection.ClientType;
import dev.wolveringer.dataserver.gamestats.Game;

public class AcardeManager {
	private static final int MIN_FREE_SERVER = 3;

	private static HashMap<Game, CachedArrayList<UUID>> serverChanging = new HashMap<Game, CachedArrayList<UUID>>() {
		public CachedArrayList<UUID> get(Object key) {
			CachedArrayList<UUID> arraylist = super.get(key);
			if (arraylist == null) {
				arraylist = new CachedArrayList<>(1, TimeUnit.MINUTES);
				super.put((Game) key, arraylist);
			}
			return arraylist;
		};
	};

	public static void balance() {
		HashMap<Game, ArrayList<Client>> servers = buildGameServerLobbyIndex();
		if (servers.size() == 0) //Keine server registriert
			return;
		sortLobbysByPlayers(servers);
		ArrayList<Client> var0 = buildFreeServerIndex(servers); //Cut server > MIN_FREE_SERVER
		servers = shortByNeeded(servers); //SHort games by needed Server
		int freeServersLeft = var0.size();
		Iterator<Client> freeServer = var0.iterator();

		int needed = calculateNeededServers(servers);

		for (Game game : servers.keySet()) {
			if (servers.get(game).size() < MIN_FREE_SERVER) {
				int fills = calculateMinServerPerGame(needed, freeServersLeft) - servers.get(game).size();
				freeServersLeft -= fills;
				needed -= fills;
				for (int i = 0; i < fills; i++) {
					Client next = freeServer.next();
					
					serverChanging.get(game).add(UUID.randomUUID());
					//Move freeServer.next() to gamemode game
				}
			}
		}
		System.out.println("Free Server left: "+freeServersLeft);
	}

	private static int calculateNeededServers(HashMap<Game, ArrayList<Client>> servers) {
		int out = 0;
		for (Game g : servers.keySet())
			out += calculateNeeded(servers, g);
		return out;
	}

	private static HashMap<Game, ArrayList<Client>> shortByNeeded(HashMap<Game, ArrayList<Client>> servers) {
		return (HashMap<Game, ArrayList<Client>>) sortByValue((Map)servers,servers);
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map,HashMap<Game, ArrayList<Client>> servers) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				Game game1 = (Game) o1.getKey();
				Game game2 = (Game) o2.getKey();
				return Integer.compare(calculateNeeded(servers, game1), calculateNeeded(servers, game2));
			}
			
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	private static int calculateNeeded(HashMap<Game, ArrayList<Client>> server,Game game){
		return Math.max(0, MIN_FREE_SERVER-(server.get(game).size())-serverChanging.get(game).size());
	}

	private static int calculateMinServerPerGame(int needed, int freeservers) {
		return (int) Math.min(MIN_FREE_SERVER, Math.ceil((double) freeservers / (double) needed)); //cell = upround
	}

	private static void sortLobbysByPlayers(HashMap<Game, ArrayList<Client>> servers) {
		for (Game g : servers.keySet())
			Collections.sort(servers.get(g), new Comparator<Client>() {
				@Override
				public int compare(Client o1, Client o2) {
					return Integer.compare(o2.getStatus().getPlayers(), o1.getStatus().getPlayers()); //Short 3>2>1
				}
			});
	}

	private static ArrayList<Client> buildFreeServerIndex(HashMap<Game, ArrayList<Client>> servers) {
		ArrayList<Client> out = new ArrayList<>();
		for (Game g : servers.keySet()) {
			ArrayList<Client> i = servers.get(g);
			if (i.size() > 3) {
				List<Client> ft = i.subList(3, i.size()); //Serers > 3 (min-Server anzahl) werden nicht benötigt (Auf diesem Server dürften sowieso keine Spieler sein)
				i.removeAll(ft);
				out.addAll(ft); //Adding as free-server
			}
		}
		return out;
	}

	private static HashMap<Game, ArrayList<Client>> buildGameServerLobbyIndex() {
		HashMap<Game, ArrayList<Client>> out = new HashMap<Game, ArrayList<Client>>() {
			public ArrayList<Client> get(Object key) {
				ArrayList<Client> arraylist = super.get(key);
				if (arraylist == null) {
					arraylist = new CachedArrayList<>(1, TimeUnit.MINUTES);
					super.put((Game) key, arraylist);
				}
				return arraylist;
			};
		};

		for (Client c : ServerThread.getServer(ClientType.ACARDE)) {
			if (c.getStatus() != null && c.getStatus().getTyp() != null && c.getStatus().isLobby())
				out.get(c.getStatus().getTyp()).add(c);
		}

		return out;
	}

	public static void main(String[] args) {
		int loop = 0;

		int free = 7;

		int[] currunt = { 1, 2, 1, 3 };
		int[] needed = { 3, 2, 2, 1 };
		int neededAll = 2 + 1 + 2 + 3;

		while (neededAll > 0 && free > 0) {
			long add = calculateMinServerPerGame(neededAll, free);
			free -= add;
			System.out.println((add));
			neededAll -= needed[loop];
			loop++;
		}
		System.out.println("Left: " + free);

		while (neededAll > 0 && free > 0) {
			long add = calculateMinServerPerGame(neededAll, free);
			free -= add;
			System.out.println((add));
			neededAll -= needed[loop];
			loop++;
		}
		System.out.println("Left: " + free);

		ArrayList<Integer> a = new ArrayList<>();
		for (int i = 0; i < 50000; i++)
			a.add(new Random().nextInt());
		long start = System.currentTimeMillis();
		Collections.sort(a);
		System.out.println("Needed: " + (System.currentTimeMillis() - start));
	}
}
