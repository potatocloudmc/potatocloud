package net.potatocloud.api.service;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.property.PropertyHolder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents one service.
 */
public interface Service extends PropertyHolder {

    /**
     * Gets the name of the service.
     *
     * @return the name of the service
     */
    String name();

    /**
     * Gets the ID of the service.
     *
     * @return the ID of the service
     */
    int id();

    /**
     * Gets the cluster node assigned to the group of this service.
     *
     * @return the cluster node, or an empty optional if not assigned
     */
    Optional<ClusterNode> node();

    /**
     * Gets the host of the node this service runs on.
     *
     * @return the host address of the node
     */
    String host();

    /**
     * Gets the port of the service.
     *
     * @return the port of the service
     */
    int port();

    /**
     * Gets the state of the service.
     *
     * @return the state of the service
     */
    ServiceState state();

    /**
     * Sets the state of the service.
     *
     * @param state the new state of the service
     */
    void state(ServiceState state);

    /**
     * Gets whether the service is online.
     *
     * @return {@code true} if the service is online, otherwise {@code false}
     */
    default boolean running() {
        return state() == ServiceState.RUNNING;
    }

    /**
     * Gets the timestamp of the service start.
     *
     * @return the timestamp of the service start
     */
    Instant startedAt();

    /**
     * Gets the uptime of the service.
     *
     * @return the uptime of the service
     */
    Duration uptime();

    /**
     * Gets the maximum player count of the service.
     *
     * @return the maximum player count of the service
     */
    int maxPlayers();

    /**
     * Sets the maximum player count of the service.
     *
     * @param maxPlayers the new maximum player count of the service
     */
    void maxPlayers(int maxPlayers);


    /**
     * Gets the used memory of the service.
     *
     * @return the used memory of the service
     */
    int usedMemory();

    /**
     * Gets the group of the service.
     *
     * @return the group of the service
     */
    Group group();

    /**
     * Gets the online players of the service.
     *
     * @return the online players of the service
     */
    default Set<CloudPlayer> players() {
        return CloudAPI.instance().playerManager().players().stream()
                .filter(player -> player.service().stream().anyMatch(service -> name().equals(service.name())))
                .collect(Collectors.toSet());
    }

    /**
     * Gets the online player count of the service.
     *
     * @return the online player count of the service
     */
    default int playerCount() {
        return players().size();
    }

    /**
     * Gets whether the service is full.
     *
     * @return {@code true} if the service is full, otherwise {@code false}
     */
    default boolean full() {
        return playerCount() >= maxPlayers();
    }
}
