package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.dataserver.connection.Client;

public class PlayerManager {
	private static HashMap<UUID, OnlinePlayer> players = new HashMap<>();
	
	private static ArrayList<OnlinePlayer> cc = new ArrayList<>();
	
	public static ArrayList<OnlinePlayer> getPlayer(){
		return new ArrayList<>(players.values());
	}
	
	public static OnlinePlayer loadPlayer(String player,Client owner){
		for(OnlinePlayer p : getPlayer())
			if(p.getName().equalsIgnoreCase(player))
				return null;
		for(OnlinePlayer p : cc)
			if(p.getName().equalsIgnoreCase(player)){
				p.load();
				players.put(p.getUuid(), p);
				return null;
			}
		OnlinePlayer var0 = new OnlinePlayer(player,owner);
		players.put(var0.getUuid(), var0);
		if(var0 != null)
			var0.load();
		return var0;
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
			players.remove(old);
			players.put(_new, player);
		}
	}

	public static void unload(String player) {
		OnlinePlayer players = getPlayer(player);
		if(players.isDisableUnload())
			cc.add(players);
		PlayerManager.players.remove(players.getUuid());
	}
	
	public static List<String> getPlayers(String server){
		ArrayList<String> out = new ArrayList<>();
		for(OnlinePlayer c : getPlayer()){
			if(c.isPlaying())
				if(server == null || c.getServer().equalsIgnoreCase(server))
					out.add(c.getName());
		}
		return out;
	}
}
