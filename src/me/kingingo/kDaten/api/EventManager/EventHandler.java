package me.kingingo.kDaten.api.EventManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
	public EventPriority priority() default EventPriority.MEDIUM;
}
