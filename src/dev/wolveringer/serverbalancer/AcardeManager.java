package dev.wolveringer.serverbalancer;

import java.lang.invoke.LambdaConversionException;
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

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.connection.ClientType;
import dev.wolveringer.dataserver.connection.ServerStatus;
import dev.wolveringer.dataserver.gamestats.Game;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatus.GameState;

public class AcardeManager {
	private static final int MIN_FREE_SERVER = 3;

	private static HashMap<Game, ArrayList<Client>> lastCalculated = new HashMap<>();
	
	@SuppressWarnings("serial")
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
	private static CachedArrayList<String> notFree = new CachedArrayList<>(1, TimeUnit.MINUTES);
	
	public static void serverConnected(Game game){
		if(serverChanging.get(game).size() > 0)
			serverChanging.get(game).remove(0);
	}
	public static void serverDisconnected(String name){
		notFree.remove(name);
	}

	public static HashMap<Game, ArrayList<Client>> balance() {
		HashMap<Game, ArrayList<Client>> servers = buildGameServerLobbyIndex();
		if (servers.size() == 0) //Keine server registriert
			return null;
		sortLobbysByPlayers(servers);
		ArrayList<Client> var0 = buildFreeServerIndex(servers); //Cut server > MIN_FREE_SERVER
		servers = shortByNeeded(servers); //Short games by needed Server
		int freeServersLeft = var0.size();
		Iterator<Client> freeServer = var0.iterator();

		int oneeded = calculateNeededServers(servers); //Orginal needed
		int needed = oneeded;
		if(needed == 0)
			needed = servers.size(); //Adding left servers to games
		for (Game game : Game.values()) {
			if(!game.isArcade())
				continue;
			if(freeServersLeft <= 0)
				continue;
			if(!servers.containsKey(game))
				servers.put(game, new CachedArrayList<>(1, TimeUnit.MINUTES));
			if (servers.get(game).size() < MIN_FREE_SERVER) {
				int fills = calculateMinServerPerGame(needed, freeServersLeft);
				needed -= fills;
				//System.out.println("Fills: "+fills+" Game: "+game);
				for (int i = 0; i < fills; i++) {
					if(!freeServer.hasNext())
						continue;
					freeServersLeft--;
					Client next = freeServer.next();
					System.out.println("Setting "+next.getName()+" to "+game);
					serverChanging.get(game).add(UUID.randomUUID());
					notFree.add(next.getName());
					next.setGame(game);
					//Move freeServer.next() to gamemode game
				}
			}
		}
		lastCalculated = (HashMap<Game, ArrayList<Client>>) servers.clone();
		System.out.println("Free Server left: "+freeServersLeft+" Orginal needed Servers: "+oneeded);
		return servers;
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

	public static <K, V> Map<K, V> sortByValue(Map<K, V> map,HashMap<Game, ArrayList<Client>> servers) {
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
				List<Client> ft = new ArrayList<>(i.subList(3, i.size())); //Serers > 3 (min-Server anzahl) werden nicht benötigt (Auf diesem Server dürften sowieso keine Spieler sein)
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
			if (c.getStatus() != null && c.getStatus().getTyp() != null && c.getStatus().isVisiable() && c.getStatus().getState() == GameState.LobbyPhase && !notFree.contains(c.getName()))
				out.get(c.getStatus().getTyp()).add(c);
		}

		return out;
	}

	public static void writeServers(){
		if (lastCalculated.size() == 0) //Keine server registriert
			System.out.println("No servers");
		for(Game g : lastCalculated.keySet()){
			System.out.println("game: "+g+" Server: "+lastCalculated.get(g));
		}
		
	}
	
	private static class ClientAdapter extends Client{
			private String name;
			private ServerStatus status;
			public ClientAdapter(String name,int player) {
				super(null, null);
				status = new ServerStatus(null);
				status.setVisiable(true);
				status.setServerId(name);
				status.setMaxPlayers(100);
				status.setMots("");
				status.setPlayers(player);
				status.setTyp(Game.BedWars);
				this.name = name;
			}
			
			@Override
			public void setGame(Game game) {
				//System.out.println("Setgame");
				status.setTyp(game);
			}
			@Override
			public String getName() {
				return name;
			}
			@Override
			public ServerStatus getStatus() {
				return status;
			}
			@Override
			public ClientType getType() {
				return ClientType.ACARDE;
			}
			@Override
			public String toString() {
				return "lobby: "+status.isVisiable()+" Name: "+name;
			}
	}
}
