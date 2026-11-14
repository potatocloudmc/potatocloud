package net.potatocloud.api.player;

import net.potatocloud.api.service.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Represents the player manager.
 */
public interface CloudPlayerManager {

    /**
     * Gets a player by its unique ID.
     *
     * @param uniqueId the unique ID of the player
     * @return the player, or an empty optional if not found
     */
    Optional<CloudPlayer> find(UUID uniqueId);

    /**
     * Gets a player by its username.
     *
     * @param username the username of the player
     * @return the player, or an empty optional if not found
     */
    Optional<CloudPlayer> find(String username);

    /**
     * Gets the set of all online players.
     *
     * @return the set of all online players
     */
    Set<CloudPlayer> players();

    /**
     * Connects the player to the given service.
     *
     * @param player the player to connect
     * @param service the service to connect with
     */
    void connectTo(CloudPlayer player, Service service);

    /**
     * Kicks the player from the network.
     *
     * @param player the player to kick
     * @param reason the kick message in MiniMessage format
     */
    void kick(CloudPlayer player, String reason);

    /**
     * Kicks the player from the network with a default message.
     *
     * @param player the player to kick
     */
    default void kick(CloudPlayer player) {
        kick(player, "<red>You have been kicked from the network");
    }

    /**
     * Updates an existing player.
     *
     * @param player the player to update
     */
    void update(CloudPlayer player);

}
