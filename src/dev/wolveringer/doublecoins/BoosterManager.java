package dev.wolveringer.doublecoins;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.arrays.CachedArrayList.UnloadListener;
import dev.wolveringer.booster.BoosterType;
import dev.wolveringer.booster.NetworkBooster;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.StatsKey;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit.Action;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit.EditStats;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.gamestats.Statistic;
import lombok.AllArgsConstructor;

public class BoosterManager implements UnloadListener<Entry<BoosterType, NetworkBooster>>{
	private static BoosterManager manager;
	
	public static BoosterManager getManager() {
		return manager;
	}
	public static void setManager(BoosterManager manager) {
		BoosterManager.manager = manager;
	}
	
	@SuppressWarnings("serial")
	private CachedArrayList<Entry<BoosterType, NetworkBooster>> times = new CachedArrayList<>(1, TimeUnit.MINUTES);
	
	public BoosterManager() {
		times.addUnloadListener(this);
	}
	
	
	public void activeBooster(OnlinePlayer player, int time,BoosterType type){
		player.getStatsManager().applayChanges(new PacketInStatsEdit(player.getPlayerId(), new EditStats[]{new EditStats(GameType.BOOSTER, Action.REMOVE, StatsKey.BOOSTER_TIME, time)}));
		for(Entry<BoosterType, NetworkBooster> e : new ArrayList<>(times))
			if(e.getKey() == type)
				times.remove(e);
		times.add(new WriteThrowEntry(type, new NetworkBooster(System.currentTimeMillis(), time, player.getPlayerId(), type, true)));
		EventHelper.callNetworkBoosterUpdateEvent(type, true);
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
		for(Entry<BoosterType, NetworkBooster> e :  times)
			if(e.getKey() == type)
				return e.getValue();
		return new NetworkBooster.NotActiveBooster(type, -1, -1);
	}
	@Override
	public boolean canUnload(Entry<BoosterType, NetworkBooster> element) {
		EventHelper.callNetworkBoosterUpdateEvent(element.getKey(), false);
		return true;
	}
}
@AllArgsConstructor
class WriteThrowEntry<K, V> implements Entry<K, V> {
	private K key;
	private V value;

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		return value;
	}
	
}