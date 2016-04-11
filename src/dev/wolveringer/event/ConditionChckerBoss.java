package dev.wolveringer.event;

import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.player.Setting;
import dev.wolveringer.events.EventCondition;
import dev.wolveringer.events.EventConditions;

public class ConditionChckerBoss {
	private static final HashMap<EventConditions, ConditionChecker> checkers = new HashMap<>();
	
	static  {
		checkers.put(EventConditions.CLIENT_TYPE_ARRAY, new ConditionChecker<ClientType>() {
			@Override
			public boolean isValid(EventCondition<ClientType> condition, ClientType value) {
				return condition.hasValue(value);
			}
		});
		checkers.put(EventConditions.GAME_TYPE_ARRAY, new ConditionChecker<GameType>() {
			@Override
			public boolean isValid(EventCondition<GameType> condition, GameType value) {
				return condition.hasValue(value);
			}
		});
		checkers.put(EventConditions.PLAYERS_BACKLIST, new ConditionChecker<UUID>() {
			@Override
			public boolean isValid(EventCondition<UUID> condition, UUID value) {
				return !condition.hasValue(value);
			}
		});
		checkers.put(EventConditions.PLAYERS_WHITELIST, new ConditionChecker<UUID>() {
			@Override
			public boolean isValid(EventCondition<UUID> condition, UUID value) {
				System.out.println("Checking whitelist: "+condition.getValues()+":"+value);
				return condition.hasValue(value);
			}
		});
		checkers.put(EventConditions.SERVER_NAME_ARRAY, new ConditionChecker<String>() {
			@Override
			public boolean isValid(EventCondition<String> condition, String value) {
				return condition.hasValue(value);
			}
		});
		checkers.put(EventConditions.SETTING_ARRAY, new ConditionChecker<Setting>() {
			@Override
			public boolean isValid(EventCondition<Setting> condition, Setting value) {
				return condition.hasValue(value);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public static boolean checkOr(EventCondition<?> con,Object[] value){
		System.out.println("Checking OR");
		for(Object o : value){
			if(checkers.get(con.getCondition()).isClassValid(con, o)){
				if(checkers.get(con.getCondition()).isValid(con, o)){
					return true;
				}
				else
					System.out.println("Permission not valid");
			}
			else
				System.out.println("Class invalid");
		}
		return false;
	}
}
