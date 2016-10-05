package eu.epicpvp.event;

import java.util.HashMap;

import eu.epicpvp.dataserver.connection.Client;
import eu.epicpvp.dataserver.protocoll.packets.PacketEventCondition;
import eu.epicpvp.dataserver.protocoll.packets.PacketEventTypeSettings;
import eu.epicpvp.datenserver.definitions.events.Event;
import eu.epicpvp.datenserver.definitions.events.EventType;
import lombok.AccessLevel;
import lombok.Getter;

public class EventHandlerBoss {
	private HashMap<EventType, EventTypeHandler> handler = new HashMap<>();
	@Getter(value=AccessLevel.PACKAGE)
	private Client handle;
	
	public EventHandlerBoss(Client handle) {
		this.handle = handle;
		for(EventType type : EventType.values())
			handler.put(type, new EventTypeHandler(type,this));
	}
	
	public void handUpdate(PacketEventCondition update){
		handler.get(update.getEventType()).handUpdate(update);
	}
	public void handUpdate(PacketEventTypeSettings update){
		handler.get(update.getType()).handUpdate(update);
	}
	public void callEvent(Event event, EventConditionKey... keys) {
		handler.get(event.getType()).callEvent(event, keys);
	}
}
