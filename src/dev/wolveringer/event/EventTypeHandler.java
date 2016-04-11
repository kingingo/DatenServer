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
		System.out.println("Active: "+active+":"+type);
		if(!active)
			return;
		HashMap<EventConditions, Object[]> cons = new HashMap<>();
		for(EventConditionKey c : conditions)
			cons.put(c.getType(), c.getValue());
		System.out.println("Checking conditions");
		for(EventCondition c : activeConditions){
			System.out.println("Check: "+c.getCondition()+" Contains: "+cons.containsKey(c.getCondition())+" CCheck: "+ConditionChckerBoss.checkOr(c, cons.get(c.getCondition())));
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
			System.out.println("Update conditions: "+update.getType());
			if(update.getType() == EventConditions.PLAYERS_WHITELIST){
				System.out.println("Players 2 whitelist: "+update.getCondition().getValues());
			}
		}else
			for(EventCondition c : new ArrayList<>(activeConditions))
				if(c.getCondition() == update.getType())
					activeConditions.remove(c);
	}
	public void handUpdate(PacketEventTypeSettings update){
		active = update.isActive();
		System.out.println("Set active: "+active+":"+type);
		if(update.isActive()){
			activeConditions.clear();
			activeConditions.addAll(update.getConditions());
			for(EventCondition c : update.getConditions()){
				if(c.getCondition() == EventConditions.PLAYERS_WHITELIST){
					System.out.println("Players whitelist: "+c.getValues());
				}
			}
			System.out.println("Active event handler: "+update.getType());
		}
		else{
			activeConditions.clear();
		}
	}
}
