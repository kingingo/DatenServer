package eu.epicpvp.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import eu.epicpvp.connection.server.ServerThread;
import eu.epicpvp.dataserver.connection.Client;
import eu.epicpvp.dataserver.player.OnlinePlayer;
import eu.epicpvp.datenserver.definitions.booster.BoosterType;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.events.Event;
import eu.epicpvp.datenserver.definitions.events.EventConditions;
import eu.epicpvp.datenserver.definitions.events.booster.BoosterStatusChangeEvent;
import eu.epicpvp.datenserver.definitions.events.player.PlayerServerSwitchEvent;
import eu.epicpvp.datenserver.definitions.events.teamspeak.TeamspeakLinkRequestEvent;

public class EventHelper {
	public static void callServerSwitchEvent(OnlinePlayer player, Client client, String serverOld, String serverNew) {
		PlayerServerSwitchEvent event = new PlayerServerSwitchEvent(player.getPlayerId(), serverOld, serverNew);
		EventConditionKeyBuilder builder = new EventConditionKeyBuilder();
		Client temp;

		temp = ServerThread.getServer(serverOld);
		if (temp != null)
			builder.put(EventConditions.GAME_TYPE_ARRAY, temp.getStatus().getTyp());

		temp = ServerThread.getServer(serverNew);
		if (temp != null)
			builder.put(EventConditions.GAME_TYPE_ARRAY, temp.getStatus().getTyp());

		builder.put(EventConditions.CLIENT_TYPE_ARRAY, client.getType()).put(EventConditions.SERVER_NAME_ARRAY, serverOld, serverNew);
		builder.put(EventConditions.PLAYERS_BACKLIST, player.getUuid()).put(EventConditions.PLAYERS_WHITELIST, player.getUuid());

		EventConditionKey[] keys = builder.build();
		for (Client c : ServerThread.getServer(ClientType.ALL))
			c.getEventHander().callEvent(event, keys);
	}

	public static void callNetworkBoosterUpdateEvent(BoosterType type, boolean active) {
		BoosterStatusChangeEvent event = new BoosterStatusChangeEvent(type, active);
		EventConditionKeyBuilder builder = new EventConditionKeyBuilder();
		builder.put(EventConditions.BOOSTER_TYPE, type);
		EventConditionKey[] keys = builder.build();
		for (Client c : ServerThread.getServer(ClientType.ALL))
			c.getEventHander().callEvent(event, keys);
	}

	public static void callGildEvent(UUID gilde, Event event) {
		EventConditionKeyBuilder builder = new EventConditionKeyBuilder();
		builder.put(EventConditions.GILDE_UUID, gilde);
		EventConditionKey[] keys = builder.build();
		for (Client c : ServerThread.getServer(ClientType.ALL))
			c.getEventHander().callEvent(event, keys);
	}

	public static void callTeamspeakLinkRequestEvent(String target, com.github.theholywaffle.teamspeak3.api.wrapper.Client info, UUID token) {
		TeamspeakLinkRequestEvent event = new TeamspeakLinkRequestEvent(target, info.getNickname(), info.getIp(), info.getPlatform(), token);
		EventConditionKey[] keys = new EventConditionKey[0];
		for (Client c : ServerThread.getServer(ClientType.ALL))
			c.getEventHander().callEvent(event, keys);
	}

	private static class EventConditionKeyBuilder {
		private HashMap<EventConditions, ArrayList<Object>> objects = new HashMap<>();

		private ArrayList<Object> getObjects(EventConditions con) {
			if (!objects.containsKey(con))
				objects.put(con, new ArrayList<>());
			return objects.get(con);
		}

		public EventConditionKeyBuilder put(EventConditions con, Object... objects) {
			getObjects(con).addAll(Arrays.asList(objects));
			return this;
		}

		public EventConditionKey[] build() {
			ArrayList<EventConditionKey> keys = new ArrayList<>();
			for (EventConditions c : objects.keySet())
				keys.add(new EventConditionKey(c, getObjects(c).toArray(new Object[0])));
			return keys.toArray(new EventConditionKey[0]);
		}
	}
}
