package dev.wolveringer.event;

import dev.wolveringer.events.EventCondition;

public interface ConditionChecker<T> {
	default boolean isClassValid(EventCondition condition,Object value){
		if(condition == null){
			System.err.println("Condition == null");
			return false;
		}
		if(value == null){
			System.err.println("Condition == null");
			return false;
		}
		if(condition.getCondition() == null){
			System.err.println("condition.getCondition() == null");
			return false;
		}
		if(condition.getCondition().getConditionType() == null){
			System.err.println("condition.getCondition().getConditionType() == null");
			return false;
		}
		return condition.getCondition().getConditionType().isAssignableFrom(value.getClass()) || condition.getCondition().getConditionType().equals(value.getClass());
	}
	default boolean isOrValid(EventCondition<T> condition,T... value){
		for(T a : value)
			if(isValid(condition, a))
				return true;
		return false;
	}
	public boolean isValid(EventCondition<T> condition,T value);
}
