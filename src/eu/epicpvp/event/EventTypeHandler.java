package eu.epicpvp.event;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketEventCondition;
import eu.epicpvp.dataserver.protocoll.packets.PacketEventFire;
import eu.epicpvp.dataserver.protocoll.packets.PacketEventTypeSettings;
import eu.epicpvp.datenserver.definitions.events.Event;
import eu.epicpvp.datenserver.definitions.events.EventCondition;
import eu.epicpvp.datenserver.definitions.events.EventConditions;
import eu.epicpvp.datenserver.definitions.events.EventType;

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
		if(!active)
			return;
		HashMap<EventConditions, Object[]> cons = new HashMap<>();
		for(EventConditionKey c : conditions)
			cons.put(c.getType(), c.getValue());
		for(EventCondition c : activeConditions){
			if(!cons.containsKey(c.getCondition())) //Needed 
				return;
			if(!ConditionChckerBoss.checkOr(c, cons.get(c.getCondition())))
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
