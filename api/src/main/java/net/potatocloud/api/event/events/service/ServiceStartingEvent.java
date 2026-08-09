package net.potatocloud.api.event.events.service;

import net.potatocloud.api.event.Event;

/**
 * Event sent when a service starts.
 *
 * @param serviceName the service name
 */
public record ServiceStartingEvent(String serviceName) implements Event {
}
