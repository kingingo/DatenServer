package dev.wolveringer.doublecoins;

import java.util.HashMap;

import dev.wolveringer.booster.BoosterType;
import dev.wolveringer.booster.NetworkBooster;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.StatsKey;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit.Action;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit.EditStats;
import dev.wolveringer.gamestats.Statistic;
import dev.wolveringer.hashmaps.InitHashMap;

public class BoosterManager {
	private static BoosterManager manager;
	
	public static BoosterManager getManager() {
		return manager;
	}
	public static void setManager(BoosterManager manager) {
		BoosterManager.manager = manager;
	}
	
	@SuppressWarnings("serial")
	private HashMap<BoosterType, NetworkBooster> times = new InitHashMap<BoosterType, NetworkBooster>() {
		@Override
		public NetworkBooster defaultValue(BoosterType key) {
			return new NetworkBooster.NotActiveBooster(key);
		}
	};
	
	public void activeBooster(OnlinePlayer player, int time,BoosterType type){
		player.getStatsManager().applayChanges(new PacketInStatsEdit(player.getPlayerId(), new EditStats[]{new EditStats(GameType.BOOSTER, Action.REMOVE, StatsKey.BOOSTER_TIME, time)}));
		times.put(type, new NetworkBooster(System.currentTimeMillis(), time, player.getPlayerId(), type, true));
	}
	public NetworkBooster getBooster(BoosterType type,OnlinePlayer player){
		for(Statistic c : player.getStatsManager().getStats(GameType.BOOSTER).getStats()){
			if(c.getStatsKey() == StatsKey.BOOSTER_TIME){
				return new NetworkBooster.NotActiveBooster(BoosterType.NONE,player.getPlayerId(), c.asInt());
			}
		}
		return new NetworkBooster.NotActiveBooster(type, player.getPlayerId(), -1);
	}
	public NetworkBooster getBooster(BoosterType type){
		return times.get(type);
	}
}
