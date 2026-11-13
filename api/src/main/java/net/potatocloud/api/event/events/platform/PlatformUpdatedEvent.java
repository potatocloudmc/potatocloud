package net.potatocloud.api.event.events.platform;

import net.potatocloud.api.event.Event;

/**
 * Event sent after a platform was updated on this node.
 *
 * @param platformName the platform name
 */
public record PlatformUpdatedEvent(String platformName) implements Event {
}
