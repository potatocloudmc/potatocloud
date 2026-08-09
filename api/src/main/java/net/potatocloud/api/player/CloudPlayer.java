package net.potatocloud.api.player;

import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.service.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a player connected to the cloud.
 */
public interface CloudPlayer extends PropertyHolder {

    /**
     * Gets the unique ID of the player.
     *
     * @return the unique ID of the player
     */
    UUID uniqueId();

    /**
     * Gets the username of the player.
     *
     * @return the username of the player
     */
    String username();

    /**
     * Gets the connected proxy of the player.
     *
     * @return the connected proxy of the player
     */
    Service proxy();

    /**
     * Gets the connected service of the player.
     *
     * @return the connected service, or an empty optional if not connected
     */
    Optional<Service> service();

}
