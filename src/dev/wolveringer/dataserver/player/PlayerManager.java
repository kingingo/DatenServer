package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.Validate;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.arrays.CachedArrayList.UnloadListener;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.connection.Client;
import lombok.NonNull;

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
	
	protected static synchronized void deleteDuplicated(OnlinePlayer current){
		synchronized (players) {
			for(OnlinePlayer p : getPlayers())
				if(p != null && !p.equals(current) && !p.isDeleted())
					if(p.getPlayerId() == current.getPlayerId()){
						System.out.println("Deleting dublicate of "+current.getName());
						players.remove(p);
						p.setDeleted(true);
					}
		}
	}
	
	public static ArrayList<OnlinePlayer> getPlayers(){
		synchronized (players) {
			return new ArrayList<>(players);
		}
	}
	
	public static OnlinePlayer getPlayer(@NonNull String player){
		return getPlayer(player, true);
	}
	
	public static OnlinePlayer getPlayer(@NonNull String player, boolean load){
		for(OnlinePlayer p : getPlayers())
			if(p.getName() != null)
				if(p.getName().equalsIgnoreCase(player)){
					if(load){
						p.waitWhileLoading();
						if(!p.isLoaded())
							p.load();
					}
					synchronized (players) {
						players.resetTime(p);
					}
					return p;
				}
		if(load){
			OnlinePlayer var0 = new OnlinePlayer(player);
			var0.load();
			synchronized (players) {
				players.add(var0);
			}
			return var0;
		}
		return null;
	}
	
	public static OnlinePlayer getPlayer(int player){
		Validate.isTrue(player>0, "Player ID isnt valid! (PlayerID: "+player+")");
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
	
	public static OnlinePlayer getPlayer(@NonNull UUID player){
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
