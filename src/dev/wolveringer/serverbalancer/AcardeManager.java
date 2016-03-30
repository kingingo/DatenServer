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
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.connection.ServerStatus;
import dev.wolveringer.dataserver.gamestats.GameState;
import dev.wolveringer.dataserver.gamestats.GameType;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class AcardeManager {
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
	private static final int MIN_FREE_SERVER = 3;

	private static HashMap<ServerType, ArrayList<Client>> lastCalculated = new HashMap<>();
	private static ArrayList<ServerType> types = new ArrayList<>();
	
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
		};
	};
	private static CachedArrayList<String> notFree = new CachedArrayList<>(1, TimeUnit.MINUTES);
	
	public static void serverConnected(ServerType game){
		if(serverChanging.get(game).size() > 0)
			serverChanging.get(game).remove(0);
	}
	public static void serverDisconnected(String name){
		notFree.remove(name);
	}

	public static HashMap<ServerType, ArrayList<Client>> balance() {
		HashMap<ServerType, ArrayList<Client>> servers = buildGameServerLobbyIndex();
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
		
		while (needed > 0 && freeServersLeft > 0) {
			int filled = 0;
			for (ServerType game : types) {
				if(!game.getType().isArcade())
					continue;
				if(freeServersLeft <= 0)
					continue;
				if(!servers.containsKey(game))
					servers.put(game, new CachedArrayList<>(1, TimeUnit.MINUTES));
				if (servers.get(game).size() < getMinServer(game)) {
					filled++;
					needed -= 1;
					freeServersLeft--;
					//System.out.println("Fills: "+fills+" Game: "+game);
					if(!freeServer.hasNext())
						continue;
					Client next = freeServer.next();
					System.out.println("Setting "+next.getName()+" to "+game.getType()+"["+game.getModifier()+"]");
					serverChanging.get(game).add(UUID.randomUUID());
					notFree.add(next.getName());
					next.setGame(game.getType(),game.getModifier());
				}
			}
			if(filled == 0)
				break;
			filled = 0;
			System.out.println(needed+":"+freeServersLeft);
		}
		lastCalculated = (HashMap<ServerType, ArrayList<Client>>) servers.clone();
		System.out.println("Free Server left: "+freeServersLeft+" Orginal needed Servers: "+oneeded);
		return servers;
	}

	private static int calculateNeededServers(HashMap<ServerType, ArrayList<Client>> servers) {
		int out = 0;
		for (ServerType g : servers.keySet())
			out += calculateNeeded(servers, g);
		return out;
	}

	private static HashMap<ServerType, ArrayList<Client>> shortByNeeded(HashMap<ServerType, ArrayList<Client>> servers) {
		return (HashMap<ServerType, ArrayList<Client>>) sortByValue((Map)servers,servers);
	}

	public static <K, V> Map<K, V> sortByValue(Map<K, V> map,HashMap<ServerType, ArrayList<Client>> servers) {
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
	
	private static int calculateNeeded(HashMap<ServerType, ArrayList<Client>> server,ServerType game){
		return Math.max(0, getMinServer(game)-(server.get(game).size())-serverChanging.get(game).size());
	}

	/*
	private static int calculateMinServerPerGame(int needed, int freeservers) {
		return (int) Math.min(MIN_FREE_SERVER, Math.ceil((double) freeservers / (double) needed)); //cell = upround
	}*/
	
	private static int getMinServer(ServerType game){
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
			};
		};

		for (Client c : ServerThread.getServer(ClientType.ACARDE)) {
			if (c.getStatus() != null && c.getStatus().getTyp() != null && c.getStatus().isVisiable() && c.getStatus().getState() == GameState.LobbyPhase && !notFree.contains(c.getName())){
				out.get(getType(c.getStatus().getTyp(), c.getStatus().getSubType())).add(c);
			}
		}

		return out;
	}

	public static void writeServers(){
		if (lastCalculated.size() == 0) //Keine server registriert
			System.out.println("No servers");
		for(ServerType g : lastCalculated.keySet()){
			System.out.println("game: "+g.getType()+"["+g.getModifier()+"] Server: "+lastCalculated.get(g));
		}
		System.out.println("Connected:: "+ServerThread.getServer(ClientType.ACARDE));
	}
	
	private static ServerType getType(GameType state,String modifier){
		ServerType currunt = new ServerType(state, modifier);
		if(!types.contains(currunt))
			types.add(currunt);
		return currunt;
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
				status.setTyp(GameType.BedWars);
				status.setSubType("NONE");
				status.setState(GameState.LobbyPhase);
				this.name = name;
			}
			
			public ClientAdapter(String name,int player,GameType type,String subtype) {
				super(null, null);
				status = new ServerStatus(null);
				status.setVisiable(true);
				status.setServerId(name);
				status.setMaxPlayers(100);
				status.setMots("");
				status.setPlayers(player);
				status.setTyp(type);
				status.setSubType(subtype);
				status.setState(GameState.LobbyPhase);
				this.name = name;
			}
			
			@Override
			public void setGame(GameType game,String type) {
				status.setTyp(game);
				status.setSubType(type);
				disconnect();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						ServerThread.registerTestServer(new ClientAdapter(name, status.getPlayers(), game, type));
						serverConnected(new ServerType(game, type));
					}
				}).start();
			}
			
			public void disconnect(){
				ServerThread.removeServer(this);
				serverDisconnected(name);
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
				return "lobby: "+status.isVisiable()+" Name: "+name+"["+status.getSubType()+"]";
			}
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		for(int i = 0;i<20;i++){
			ServerThread.registerTestServer(new ClientAdapter("server_"+i, 0));
		}
		
		int loop = 0;
		
		while (true) {
			loop++;
			if(loop%10==0){
				List<Client> servers = ServerThread.getServer(ClientType.ACARDE);
				Client c;
				(c = servers.get(new Random().nextInt(servers.size()))).disconnect();
				System.out.println("Disconnect: "+c.getName());
			}
				
			balance();
			writeServers();
			Thread.sleep(1000);
		}
	}
}
