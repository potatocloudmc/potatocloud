package net.potatocloud.eventbus;

import net.potatocloud.api.event.Event;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.EventHandler;
import net.potatocloud.api.event.PublishTarget;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultEventBus implements EventBus {

    private final Map<Class<?>, CopyOnWriteArrayList<EventHandler<?>>> handlers = new ConcurrentHashMap<>();

    @Override
    public <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        handlers.computeIfAbsent(eventType, _ -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    public <T extends Event> boolean unsubscribe(Class<T> eventType, EventHandler<T> handler) {
        final CopyOnWriteArrayList<EventHandler<?>> handlerList = handlers.get(eventType);
        if (handlerList == null) {
            return false;
        }

        final boolean removed = handlerList.remove(handler);
        if (handlerList.isEmpty()) {
            handlers.remove(eventType, handlerList);
        }
        return removed;
    }

    @Override
    public <T extends Event> void publish(T event) {
        dispatch(event);
    }

    @Override
    public <T extends Event> void publish(T event, PublishTarget target) {
        if (target == PublishTarget.LOCAL || target == PublishTarget.BOTH) {
            dispatch(event);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void dispatch(T event) {
        final List<EventHandler<T>> handlerList = (List<EventHandler<T>>) (List<?>) handlers.get(event.getClass());

        if (handlerList == null) {
            return;
        }

        for (EventHandler<T> handler : handlerList) {
            handler.handle(event);
        }
    }
}