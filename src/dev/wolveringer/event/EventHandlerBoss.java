package dev.wolveringer.event;

import java.util.HashMap;

import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.protocoll.packets.PacketEventCondition;
import dev.wolveringer.dataserver.protocoll.packets.PacketEventTypeSettings;
import dev.wolveringer.events.Event;
import dev.wolveringer.events.EventType;
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
