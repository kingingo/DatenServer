package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.arrays.CachedArrayList.UnloadListener;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.connection.Client;

public class PlayerManager{
	private static CachedArrayList<OnlinePlayer> players = new CachedArrayList<>(20, TimeUnit.MINUTES);
	
	static {
		players.addUnloadListener(new UnloadListener<OnlinePlayer>() {
			@Override
			public boolean canUnload(OnlinePlayer player) {
				return player.getServer() == null;
			}
		});
	}
	
	public static ArrayList<OnlinePlayer> getPlayers(){
		synchronized (players) {
			return new ArrayList<>(players);
		}
	}
	
	public static OnlinePlayer getPlayer(String player){
		for(OnlinePlayer p : getPlayers())
			if(p.getName() != null)
				if(p.getName().equalsIgnoreCase(player)){
					p.waitWhileLoading();
					if(!p.isLoaded())
						p.load();
					synchronized (players) {
						players.resetTime(p);
					}
					return p;
				}
		OnlinePlayer var0 = new OnlinePlayer(player);
		var0.load();
		synchronized (players) {
			players.add(var0);
		}
		return var0;
	}
	
	public static OnlinePlayer getPlayer(int player){
		for(OnlinePlayer p : getPlayers())
			if(p.getPlayerId() == player){
				p.waitWhileLoading();
				if(!p.isLoaded())
					p.load();
				synchronized (players) {
					players.resetTime(p);
				}
				return p;
			}
		OnlinePlayer var0 = new OnlinePlayer(player);
		var0.load();
		synchronized (players) {
			players.add(var0);
		}
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
		synchronized (players) {
			players.add(var0);
		}
		return var0;
	}

	public static List<String> getPlayersFromServer(String value) {
		ArrayList<String> players = new ArrayList<>();
		ArrayList<String> playerLow = new ArrayList<>();
		
		if(value == null){
			for(Client bungee : ServerThread.getBungeecords()){
				players.addAll(bungee.getPlayers());
			}
			return players;
		}
		for(OnlinePlayer p : getPlayers())
			if((p.getServer() != null && p.getServer().equalsIgnoreCase(value) && p.isPlaying()) || (value == null && p.isPlaying())){
				if(playerLow.contains(p.getName().toLowerCase()))
					continue;
				playerLow.add(p.getName().toLowerCase());
				players.add(p.getName());
			}
				
		return players;
	}

	public static void unloadAll(){
		for(OnlinePlayer p : getPlayers())
			try{
				p.save();
			}catch(Exception e){
				e.printStackTrace();
			}
		synchronized (players) {
			players.clear();
		}
	}
}
