package net.potatocloud.api.event;

public interface EventListener<T extends Event> {

    /**
     * Called then the event was fired
     *
     * @param event the event
     */
    void onEvent(T event);

}
