package me.kingingo.kDaten.api.EventManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;

public class EventManager {
	@Getter
	public HashMap<EventListener, HashMap<Integer, ArrayList<Method>>> handlers = new HashMap<EventListener, HashMap<Integer, ArrayList<Method>>>();

	// unregestiert die Class
	public boolean unregister(Class c) {
		for (int i = 0; i < handlers.size(); i++)
			if (((EventListener) handlers.keySet().toArray()[i]).getClass().equals(c)) {
				handlers.remove(i);
				return true;
			}
		return false;
	}

	// Regestriert die Class mit den Events
	public void register(EventListener listener) {
		if (this.handlers.containsKey(listener))
			return;
		if (!this.handlers.containsKey(listener))
			this.handlers.put(listener, new HashMap<Integer, ArrayList<Method>>());
		// Sucht alle Methoden in dem Listener raus
		Method[] methods = listener.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; ++i) {
			// Filtert alle EventHandler raus!
			EventHandler eventHandler = methods[i].getAnnotation(EventHandler.class);

			if (eventHandler != null) {
				// Fügt ihn zur Liste hinzu
				if (!this.handlers.get(listener).containsKey(eventHandler.priority().getPriority()))
					this.handlers.get(listener).put(eventHandler.priority().getPriority(), new ArrayList<Method>());
				this.handlers.get(listener).get(eventHandler.priority().getPriority()).add(methods[i]);
			}
		}
	}

	// Feuert das Event ab
	public void callEvent(final Event event) {
		for (EventListener listener : getHandlers().keySet()) {
			for (int i = 0; i < EventPriority.values().length; i++) {
				if (getHandlers().get(listener).containsKey(i) && !getHandlers().get(listener).get(i).isEmpty()) {
					for (Method method : getHandlers().get(listener).get(i)) {
						if (!event.getClass().getSimpleName().equals(method.getParameterTypes()[0].getSimpleName()))
							continue;
						try {
							method.invoke(listener, new Object[] { event });
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

}
