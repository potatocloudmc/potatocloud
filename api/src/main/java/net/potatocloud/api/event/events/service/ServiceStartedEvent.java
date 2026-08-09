package net.potatocloud.api.event.events.service;

import net.potatocloud.api.event.Event;

/**
 * Event sent after a service started.
 *
 * @param serviceName the service name
 */
public record ServiceStartedEvent(String serviceName) implements Event {
}
