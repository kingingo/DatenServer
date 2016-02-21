package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.dataserver.connection.Client;

public class PlayerManager {
	private static HashMap<UUID, OnlinePlayer> players = new HashMap<>();
	
	public static ArrayList<OnlinePlayer> getPlayer(){
		return new ArrayList<>(players.values());
	}
	
	public static void loadPlayer(String player,Client owner){
		OnlinePlayer var0 = new OnlinePlayer(player,owner);
		
		if(!players.containsKey(var0.getUuid()))
			players.put(var0.getUuid(), var0);
		if(var0 != null)
			var0.load();
	}
	
	public static OnlinePlayer getPlayer(UUID player){
		return players.get(player);
	}
	
	@Deprecated
	public static OnlinePlayer getPlayer(String player){
		for(OnlinePlayer p : players.values())
			if(p.getName().equalsIgnoreCase(player))
				return p;
		return null;
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

	public static void unload(String player) {
		OnlinePlayer players = getPlayer(player);
		PlayerManager.players.remove(players.getUuid());
	}
}
