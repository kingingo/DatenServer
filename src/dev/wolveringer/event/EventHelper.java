package dev.wolveringer.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import dev.wolveringer.booster.BoosterType;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.events.EventConditions;
import dev.wolveringer.events.booster.BoosterStatusChangeEvent;
import dev.wolveringer.events.player.PlayerServerSwitchEvent;

public class EventHelper {
	public static void callServerSwitchEvent(OnlinePlayer player,Client client,String serverOld,String serverNew){
		PlayerServerSwitchEvent event = new PlayerServerSwitchEvent(player.getPlayerId(), serverOld, serverNew);
		EventConditionKeyBuilder builder = new EventConditionKeyBuilder();
		Client temp;
		
		temp = ServerThread.getServer(serverOld);
		if(temp != null)
			builder.put(EventConditions.GAME_TYPE_ARRAY, temp.getStatus().getTyp());
		
		temp = ServerThread.getServer(serverNew);
		if(temp != null)
			builder.put(EventConditions.GAME_TYPE_ARRAY, temp.getStatus().getTyp());
		
		builder.put(EventConditions.CLIENT_TYPE_ARRAY, client.getType()).put(EventConditions.SERVER_NAME_ARRAY, serverOld,serverNew);
		builder.put(EventConditions.PLAYERS_BACKLIST, player.getUuid()).put(EventConditions.PLAYERS_WHITELIST, player.getUuid());

		EventConditionKey[] keys = builder.build();
		for(Client c : ServerThread.getServer(ClientType.ALL))
			c.getEventHander().callEvent(event,keys);
	}
	
	public static void callNetworkBoosterUpdateEvent(BoosterType type,boolean active){
		BoosterStatusChangeEvent event = new BoosterStatusChangeEvent(type, active);
		EventConditionKeyBuilder builder = new EventConditionKeyBuilder();
		builder.put(EventConditions.BOOSTER_TYPE, type);
		EventConditionKey[] keys = builder.build();
		for(Client c : ServerThread.getServer(ClientType.ALL))
			c.getEventHander().callEvent(event,keys);
	}
	
	private static class EventConditionKeyBuilder {
		private HashMap<EventConditions, ArrayList<Object>> objects = new HashMap<>();
		
		private ArrayList<Object> getObjects(EventConditions con){
			if(!objects.containsKey(con))
				objects.put(con, new ArrayList<>());
			return objects.get(con);
		}
		
		public EventConditionKeyBuilder put(EventConditions con,Object...objects){
			getObjects(con).addAll(Arrays.asList(objects));
			return this;
		}
		
		public EventConditionKey[] build(){
			ArrayList<EventConditionKey> keys = new ArrayList<>();
			for(EventConditions c : objects.keySet())
				keys.add(new EventConditionKey(c, getObjects(c).toArray(new Object[0])));
			return keys.toArray(new EventConditionKey[0]);
		}
	}
}
