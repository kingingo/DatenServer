package dev.wolveringer.dataserver.player;

import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.dataserver.connection.Client;

public class PlayerManager {
	private static HashMap<UUID, OnlinePlayer> players = new HashMap<>();
	
	public static void loadPlayer(String player,Client owner){
		OnlinePlayer var0 = new OnlinePlayer(player,owner);
		
		if(!players.containsKey(var0.getUuid()))
			players.put(var0.getUuid(), var0);
		if(var0 != null)
			var0.load();
	}
	
	public static void savePlayer(String player){
		System.out.println("Playersaving not implimented yet!");
	}
	
	public static OnlinePlayer getPlayer(UUID player){
		return players.get(player);
	}
	
	public static boolean isOnline(UUID player){
		return players.containsKey(player);
	}
	
	protected static void changeUUID(UUID old,UUID _new){
		if(old == _new)
			return;
		OnlinePlayer player = players.get(old);
		if(player != null){
			players.remove(player);
			players.put(_new, player);
		}
	}
}
