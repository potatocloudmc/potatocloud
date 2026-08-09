package net.potatocloud.api.event;

/**
 * Represents the event system.
 */
public interface EventBus {

    /**
     * Subscribes an event handler to a specific event type.
     *
     * @param eventType the type of event to subscribe to
     * @param handler the handler that will be called when the event is published
     * @param <T> the event type
     */
    <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> handler);

    /**
     * Publishes an event to all subscribed handlers.
     * <p>
     * The default publish target is {@link PublishTarget#BOTH}.
     *
     * @param event the event to publish
     * @param <T> the type of event
     */
    <T extends Event> void publish(T event);

    /**
     * Publishes an event using a specific {@link PublishTarget}.
     *
     * @param event the event to publish
     * @param target the target where the event should be dispatched
     * @param <T> the type of event
     */
    <T extends Event> void publish(T event, PublishTarget target);
}