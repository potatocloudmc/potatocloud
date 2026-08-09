package net.potatocloud.api.event;

/**
 * Handles one event.
 *
     * @param <T> the event type
 */
public interface EventHandler<T extends Event> {

    /**
     * Called when the event is fired.
     *
     * @param event the event
     */
    void handle(T event);

}
