package dev.wolveringer.event;

import java.util.ArrayList;
import java.util.HashMap;

import dev.wolveringer.dataserver.protocoll.packets.PacketEventCondition;
import dev.wolveringer.dataserver.protocoll.packets.PacketEventFire;
import dev.wolveringer.dataserver.protocoll.packets.PacketEventTypeSettings;
import dev.wolveringer.events.Event;
import dev.wolveringer.events.EventCondition;
import dev.wolveringer.events.EventConditions;
import dev.wolveringer.events.EventType;

public class EventTypeHandler {
	private EventType type;
	private EventHandlerBoss handle;
	private ArrayList<EventCondition> activeConditions = new ArrayList<>();
	private boolean active;
	
	public EventTypeHandler(EventType type,EventHandlerBoss handle) {
		this.type = type;
		this.handle = handle;
	}
	
	public void callEvent(Event event,EventConditionKey... conditions){
		HashMap<EventConditions, Object> cons = new HashMap<>();
		for(EventConditionKey c : conditions)
			cons.put(c.getType(), c.getValue());
		for(EventCondition c : activeConditions){
			if(!cons.containsKey(c.getCondition())) //Needed 
				return;
			if(!ConditionChckerBoss.check(c, cons.get(c.getCondition())))
				return;
		}
		handle.getHandle().writePacket(new PacketEventFire(event));
	}
	
	public void handUpdate(PacketEventCondition update){
		if(update.isActive()){
			for(EventCondition c : new ArrayList<>(activeConditions))
				if(c.getCondition() == update.getType())
					activeConditions.remove(c);
			activeConditions.add(update.getCondition());
		}else
			for(EventCondition c : new ArrayList<>(activeConditions))
				if(c.getCondition() == update.getType())
					activeConditions.remove(c);
	}
	public void handUpdate(PacketEventTypeSettings update){
		active = update.isActive();
		if(update.isActive()){
			activeConditions.clear();
			activeConditions.addAll(update.getConditions());
		}
		else{
			activeConditions.clear();
		}
	}
}
