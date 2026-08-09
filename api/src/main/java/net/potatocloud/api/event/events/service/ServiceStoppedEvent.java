package net.potatocloud.api.event.events.service;

import net.potatocloud.api.event.Event;

/**
 * Event sent after a service stops.
 *
 * @param serviceName the service name
 */
public record ServiceStoppedEvent(String serviceName) implements Event {
}
