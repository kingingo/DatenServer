package dev.wolveringer.dataserver.gamestats;

import java.util.ArrayList;
import java.util.HashMap;

import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.mysql.MySQL;

public class TopStatsManager {
	private static TopStatsManager manager;
	public static void setManager(TopStatsManager manager) {
		TopStatsManager.manager = manager;
	}
	public static TopStatsManager getManager() {
		return manager;
	}
	private HashMap<GameType, HashMap<StatsKey, ArrayList<OnlinePlayer>>> topList = new HashMap<>();
	
	@Deprecated
	public ArrayList<OnlinePlayer> getTopTenCached(GameType game,StatsKey key){
		if(!topList.containsKey(game))
			loadGame(game);
		if(!topList.get(game).containsKey(key))
			loadKey(game, key);
		return topList.get(game).get(key);
	}
	
	public ArrayList<String[]> getTopTen(GameType game,StatsKey key){
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `player`,`"+key.getMySQLName()+"` FROM `" + StatsManager.TABLE_PREFIX +game.getShortName()+"` ORDER BY `"+key.getMySQLName()+"` DESC LIMIT 10", -1);
		return query;
	}
	
	private void loadGame(GameType game){
		
	}
	private void loadKey(GameType game,StatsKey key){
		
	}
}
