package me.kingingo.kDaten.api.EventManager;

import lombok.Getter;

public enum EventPriority {
LOWEST(0), //Wird als erstes ausgeführt
LOW(1),
MEDIUM(2),
HIGH(3),
HIGHEST(4); //Wird als letztes ausgeführt

@Getter
private int priority=0;
EventPriority(int priority){
	this.priority=priority;
}

}
