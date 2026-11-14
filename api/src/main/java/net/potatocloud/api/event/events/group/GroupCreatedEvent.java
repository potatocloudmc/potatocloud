package net.potatocloud.api.event.events.group;

import net.potatocloud.api.event.Event;

/**
 * Event sent after a group was created.
 *
 * @param groupName the group name
 */
public record GroupCreatedEvent(String groupName) implements Event {
}
