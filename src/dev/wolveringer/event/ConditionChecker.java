package dev.wolveringer.event;

import dev.wolveringer.events.EventCondition;

public interface ConditionChecker<T> {
	default boolean isClassValid(EventCondition condition,Object value){
		return condition.getCondition().getConditionType().isAssignableFrom(value.getClass());
	}
	default boolean isOrValid(EventCondition<T> condition,T... value){
		for(T a : value)
			if(isValid(condition, a))
				return true;
		return false;
	}
	public boolean isValid(EventCondition<T> condition,T value);
}
