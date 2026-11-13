package net.potatocloud.api.event.events.group;

import net.potatocloud.api.event.Event;

/**
 * Event sent after a group was updated.
 *
 * @param groupName the group name
 */
public record GroupUpdatedEvent(String groupName) implements Event {
}
