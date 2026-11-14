package net.potatocloud.api.event.events.platform;

import net.potatocloud.api.event.Event;

/**
 * Event sent after a platform was created on this node.
 *
 * @param platformName the platform name
 */
public record PlatformCreatedEvent(String platformName) implements Event {
}
