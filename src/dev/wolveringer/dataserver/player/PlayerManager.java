package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerManager {
	private static ArrayList<OnlinePlayer> players = new ArrayList<>();
	
	public static ArrayList<OnlinePlayer> getPlayers(){
		return new ArrayList<>(players);
	}
	
	public static synchronized OnlinePlayer getPlayer(String player){
		for(OnlinePlayer p : getPlayers())
			if(p.getName() != null)
				if(p.getName().equalsIgnoreCase(player)){
					if(!p.isLoaded())
						p.load();
					return p;
				}
		OnlinePlayer var0 = new OnlinePlayer(player);
		var0.load();
		players.add(var0);
		return var0;
	}
	
	public static synchronized OnlinePlayer getPlayer(int player){
		for(OnlinePlayer p : getPlayers())
			if(p.getPlayerId() == player){
				if(!p.isLoaded())
					p.load();
				return p;
			}
		OnlinePlayer var0 = new OnlinePlayer(player);
		var0.load();
		players.add(var0);
		return var0;
	}
	
	public static synchronized OnlinePlayer getPlayer(UUID player){
		for(OnlinePlayer p : getPlayers())
			if(p.getUuid() != null)
				if(p.getUuid().equals(player)){
					if(!p.isLoaded())
						p.load();
					return p;
				}
		OnlinePlayer var0 = new OnlinePlayer(player);
		var0.load();
		players.add(var0);
		return var0;
	}

	public static List<String> getPlayersFromServer(String value) {
		ArrayList<String> players = new ArrayList<>();
		for(OnlinePlayer p : getPlayers())
			if((p.getServer() != null && p.getServer().equalsIgnoreCase(value) && p.isPlaying()) || (value == null && p.isPlaying()))
				players.add(p.getName());
		return players;
	}

	public static void unloadAll(){
		for(OnlinePlayer p : getPlayers())
			p.save();
		players.clear();
	}
}
