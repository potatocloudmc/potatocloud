package net.potatocloud.api.event;

public interface EventManager {

    /**
     * Registers a listener for the given event class.
     *
     * @param eventClass the class of the event to listen for
     * @param listener   the listener to handle the event
     * @param <T>        the type of the event
     */
    <T extends Event> void on(Class<T> eventClass, EventListener<T> listener);

    /**
     * Calls an event locally on this api instance.
     *
     * @param event the event to call
     * @param <T> the type of the event
     */
    <T extends Event> void callLocal(T event);

    /**
     * Calls an event globally on all api instances.
     *
     * @param event the event to call
     * @param <T> the type of the event
     */
    <T extends Event> void call(T event);

}
