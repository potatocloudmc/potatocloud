package net.potatocloud.api.event.events.platform;

import net.potatocloud.api.event.Event;

/**
 * Event sent after a platform was deleted on this node.
 *
 * @param platformName the platform name
 */
public record PlatformDeletedEvent(String platformName) implements Event {
}
