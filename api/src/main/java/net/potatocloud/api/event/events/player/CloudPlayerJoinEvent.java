package net.potatocloud.api.event.events.player;

import net.potatocloud.api.event.Event;
import java.util.UUID;

/**
 * Event sent when a player joins.
 *
 * @param playerUniqueId the unique ID of the player
 * @param playerUsername the username of the player
 */
public record CloudPlayerJoinEvent(UUID playerUniqueId, String playerUsername) implements Event {
}
