package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerManager {
	private static ArrayList<OnlinePlayer> players = new ArrayList<>();
	
	public static ArrayList<OnlinePlayer> getPlayers(){
		return new ArrayList<>(players);
	}
	
	public static OnlinePlayer getPlayer(String player){
		for(OnlinePlayer p : getPlayers())
			if(p.getName().equalsIgnoreCase(player)){
				if(!p.isLoaded())
					p.load();
				return p;
			}
		OnlinePlayer var0 = new OnlinePlayer(player);
		players.add(var0);
		var0.load();
		return var0;
	}
	
	public static OnlinePlayer getPlayer(int player){
		for(OnlinePlayer p : getPlayers())
			if(p.getPlayerId() == player){
				if(!p.isLoaded())
					p.load();
				return p;
			}
		OnlinePlayer var0 = new OnlinePlayer(player);
		players.add(var0);
		var0.load();
		return var0;
	}
	
	public static OnlinePlayer getPlayer(UUID player){
		for(OnlinePlayer p : getPlayers())
			if(p.getUuid() == player){
				if(!p.isLoaded())
					p.load();
				return p;
			}
		OnlinePlayer var0 = new OnlinePlayer(player);
		players.add(var0);
		var0.load();
		return var0;
	}

	public static List<String> getPlayersFromServer(String value) {
		ArrayList<String> players = new ArrayList<>();
		for(OnlinePlayer p : getPlayers())
			if((p.getServer() != null && p.getServer().equalsIgnoreCase(value)) || value == null)
				players.add(p.getName());
		return players;
	}

	public static void unloadAll(){
		for(OnlinePlayer p : getPlayers())
			p.save();
		players.clear();
	}
}
