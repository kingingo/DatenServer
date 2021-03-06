package eu.epicpvp.dataserver.gamestats;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.player.OnlinePlayer;
import eu.epicpvp.dataserver.player.PlayerManager;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.StatsKey;
import eu.epicpvp.mysql.MySQL;

public class TopStatsManager {
	private static TopStatsManager manager;
	public static void setManager(TopStatsManager manager) {
		TopStatsManager.manager = manager;
	}
	public static TopStatsManager getManager() {
		return manager;
	}
	private HashMap<GameType, HashMap<StatsKey, ArrayList<OnlinePlayer>>> topList = new HashMap<>();

	public TopStatsManager() {

	}

	@Deprecated
	public ArrayList<OnlinePlayer> getTopTenCached(GameType game,StatsKey key){
		if(!topList.containsKey(game))
			loadGame(game);
		if(!topList.get(game).containsKey(key))
			loadKey(game, key);
		return topList.get(game).get(key);
	}

	public ArrayList<String[]> getTopTen(GameType game,StatsKey key){
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `playerId`,`"+key.getMySQLName()+"` FROM `" + StatsManager.TABLE_PREFIX +game.getShortName()+"` ORDER BY `"+key.getMySQLName()+"` DESC LIMIT 10", -1);
		ArrayList<String[]> out = new ArrayList<>();
		for(String[] in : query)
			out.add(new String[]{PlayerManager.getPlayer(Integer.parseInt(in[0])).getName(),in[1]});
		return out;
	}

	private void loadGame(GameType game){

	}
	private void loadKey(GameType game,StatsKey key){

	}
}
