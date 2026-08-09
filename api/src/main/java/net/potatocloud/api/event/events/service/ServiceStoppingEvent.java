package net.potatocloud.api.event.events.service;

import net.potatocloud.api.event.Event;

/**
 * Event sent when a service stops.
 *
 * @param serviceName the service name
 */
public record ServiceStoppingEvent(String serviceName) implements Event {
}
