package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;

public class PlayerManager {
	private static CachedArrayList<OnlinePlayer> players = new CachedArrayList<>(20, TimeUnit.MINUTES);
	public static ArrayList<OnlinePlayer> getPlayers(){
		return new ArrayList<>(players);
	}
	
	public static OnlinePlayer getPlayer(String player){
		for(OnlinePlayer p : getPlayers())
			if(p.getName() != null)
				if(p.getName().equalsIgnoreCase(player)){
					p.waitWhileLoading();
					if(!p.isLoaded())
						p.load();
					players.resetTime(p);
					return p;
				}
		OnlinePlayer var0 = new OnlinePlayer(player);
		var0.load();
		players.add(var0);
		return var0;
	}
	
	public static OnlinePlayer getPlayer(int player){
		for(OnlinePlayer p : getPlayers())
			if(p.getPlayerId() == player){
				p.waitWhileLoading();
				if(!p.isLoaded())
					p.load();
				players.resetTime(p);
				return p;
			}
		OnlinePlayer var0 = new OnlinePlayer(player);
		var0.load();
		players.add(var0);
		return var0;
	}
	
	public static OnlinePlayer getPlayer(UUID player){
		for(OnlinePlayer p : getPlayers())
			if(p.getUuid() != null)
				if(p.getUuid().equals(player)){
					p.waitWhileLoading();
					if(!p.isLoaded())
						p.load();
					players.resetTime(p);
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
