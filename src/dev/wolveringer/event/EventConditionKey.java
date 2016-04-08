package dev.wolveringer.event;

import dev.wolveringer.events.EventConditions;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName="create")
@Getter
public class EventConditionKey {
	private EventConditions type;
	private Object value;
}
