package net.potatocloud.api.player;

import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.service.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public interface CloudPlayerManager {

    /**
     * Gets a player by its username.
     *
     * @param username the name of the player
     * @return the player
     */
    CloudPlayer getCloudPlayer(String username);

    /**
     * Gets a player by its unique id.
     *
     * @param uniqueId the unique id of the player
     * @return the player
     */
    CloudPlayer getCloudPlayer(UUID uniqueId);

    /**
     * Gets the set of all online players.
     *
     * @return the set of all online players
     */
    Set<CloudPlayer> getOnlinePlayers();

    /**
     * Gets the set of all online players that are connected to the given service group.
     *
     * @param group the service group
     * @return the set of all online players that are connected to the given service group
     */
    default Set<CloudPlayer> getOnlinePlayersByGroup(ServiceGroup group) {
        return getOnlinePlayers().stream()
                .filter(player -> player.getConnectedService() != null && player.getConnectedService().getServiceGroup().getName().equals(group.getName()))
                .collect(Collectors.toSet());
    }

    /**
     * Connects the player to the given service.
     *
     * @param playerName  the name of the player to connect
     * @param serviceName the name of the service
     */
    void connectPlayerWithService(String playerName, String serviceName);

    /**
     * Connects the player to the given service.
     *
     * @param player      the player to connect
     * @param serviceName the name of the service
     */
    default void connectPlayerWithService(CloudPlayer player, String serviceName) {
        connectPlayerWithService(player.getUsername(), serviceName);
    }

    /**
     * Connects the player to the given service.
     *
     * @param playerName the name of the player to connect
     * @param service    the service to connect with
     */
    default void connectPlayerWithService(String playerName, Service service) {
        connectPlayerWithService(playerName, service.getName());
    }

    /**
     * Connects the player to the given service.
     *
     * @param player  the player to connect
     * @param service the service to connect with
     */
    default void connectPlayerWithService(CloudPlayer player, Service service) {
        connectPlayerWithService(player, service.getName());
    }

    /**
     * Updates an existing player.
     *
     * @param player the player to update
     */
    void updatePlayer(CloudPlayer player);

}
