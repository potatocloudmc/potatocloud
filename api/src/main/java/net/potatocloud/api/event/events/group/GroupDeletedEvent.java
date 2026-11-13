package net.potatocloud.api.event.events.group;

import net.potatocloud.api.event.Event;

/**
 * Event sent after a group was deleted.
 *
 * @param groupName the group name
 */
public record GroupDeletedEvent(String groupName) implements Event {
}
