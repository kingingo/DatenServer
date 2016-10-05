package eu.epicpvp.event;

import eu.epicpvp.datenserver.definitions.events.EventConditions;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EventConditionKey {
	private EventConditions type;
	private Object[] value;
	
	public static EventConditionKey create(EventConditions cond,Object...objects){
		return new EventConditionKey(cond,objects);
	}
}
