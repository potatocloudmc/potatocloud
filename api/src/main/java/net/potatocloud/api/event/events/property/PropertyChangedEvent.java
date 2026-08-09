package net.potatocloud.api.event.events.property;

import net.potatocloud.api.event.Event;

/**
 * Event sent when a property value changes.
 *
 * @param holderName the name of the property holder
 * @param propertyName the property name
 * @param oldValue the old value
 * @param newValue the new value
 */
public record PropertyChangedEvent(
        String holderName,
        String propertyName,
        Object oldValue,
        Object newValue
) implements Event {}
