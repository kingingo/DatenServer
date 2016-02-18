package dev.wolveringer.dataserver.player;

import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.dataserver.connection.Client;

public class PlayerManager {
	private static HashMap<UUID, OnlinePlayer> players = new HashMap<>();
	
	public static void loadPlayer(UUID player,Client owner){
		OnlinePlayer var0 = null;
		if(!players.containsKey(player))
			players.put(player,var0 = new OnlinePlayer(player,owner));
		if(var0 != null)
			var0.load();
	}
	
	public static void savePlayer(UUID player){
		System.out.println("Playersaving not implimented yet!");
	}
	
	public static OnlinePlayer getPlayer(UUID player){
		return players.get(player);
	}
	
	public static boolean isOnline(UUID player){
		return players.containsKey(player);
	}
}
