package dev.wolveringer.event;

import dev.wolveringer.events.EventCondition;

public interface ConditionChecker<T> {
	default boolean isClassValid(EventCondition condition,Object value){
		return condition.getCondition().getConditionType().isAssignableFrom(value.getClass());
	}
	public boolean isValid(EventCondition<T> condition,T value);
}
