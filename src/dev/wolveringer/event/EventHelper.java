package dev.wolveringer.event;

import java.util.UUID;

import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.events.EventConditions;
import dev.wolveringer.events.player.PlayerServerSwitchEvent;

public class EventHelper {
	public static void callServerSwitchEvent(UUID player,Client client,String serverOld,String serverNew){
		PlayerServerSwitchEvent event = new PlayerServerSwitchEvent(player, serverOld, serverNew);
		EventConditionKey[] keys = new EventConditionKey[]{createClientType(client.getType()),createGameType(client.getStatus().getTyp()),createServer(client.getStatus().getServerId()),createPlayers(player, true),createPlayers(player, false)};
		for(Client c : ServerThread.getServer(ClientType.ALL))
			c.getEventHander().callEvent(event,keys);
	}
	
	
	private static EventConditionKey createClientType(ClientType client){
		return EventConditionKey.create(EventConditions.CLIENT_TYPE_ARRAY, client);
	}
	private static EventConditionKey createPlayers(UUID player,boolean blacklist){
		return EventConditionKey.create(blacklist ? EventConditions.PLAYERS_BACKLIST : EventConditions.PLAYERS_WHITELIST, player);
	}
	private static EventConditionKey createGameType(GameType client){
		return EventConditionKey.create(EventConditions.GAME_TYPE_ARRAY, client);
	}
	private static EventConditionKey createServer(String client){
		return EventConditionKey.create(EventConditions.SERVER_NAME_ARRAY, client);
	}
	private static EventConditionKey createSetting(String client){
		return EventConditionKey.create(EventConditions.SETTING_ARRAY, client);
	}
}
