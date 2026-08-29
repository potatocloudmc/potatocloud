package net.potatocloud.api.group;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.cluster.ClusterNode;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.service.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a service group.
 */
public interface Group extends PropertyHolder {

    /**
     * Gets the name of the group.
     *
     * @return the name of the group
     */
    String name();

    /**
     * Gets the node of the group.
     *
     * @return the node of the group, or an empty optional if not assigned
     */
    Optional<ClusterNode> node();

    /**
     * Gets the platform of the group.
     *
     * @return the platform of the group
     * @throws IllegalStateException if the configured platform cannot be resolved
     */
    Platform platform();

    /**
     * Gets the platform version of the group.
     *
     * @return the platform version of the group
     * @throws IllegalStateException if the configured platform or version cannot be resolved
     */
    PlatformVersion platformVersion();

    /**
     * Gets the service templates of the group.
     *
     * @return the set of service templates of the group
     */
    Set<String> templates();

    /**
     * Gets the minimum service count of the group.
     *
     * @return the minimum service count of the group
     */
    int minServices();

    /**
     * Sets the minimum service count of the group.
     *
     * @param minServices the minimum service count of the group
     */
    void minServices(int minServices);

    /**
     * Gets the maximum service count of the group.
     *
     * @return the maximum service count of the group
     */
    int maxServices();

    /**
     * Sets the maximum service count of the group.
     *
     * @param maxServices the maximum service count of the group
     */
    void maxServices(int maxServices);

    /**
     * Gets the online players of the group.
     *
     * @return the online players of the group
     */
    default Set<CloudPlayer> players() {
        return CloudAPI.instance().playerManager().players().stream()
                .filter(player -> player.service().stream().anyMatch(service -> service.group().equals(this)))
                .collect(Collectors.toSet());
    }

    /**
     * Gets the maximum player count of each service in the group.
     *
     * @return the maximum player count of each service
     */
    int maxPlayers();

    /**
     * Sets the maximum player count of each service in the group.
     *
     * @param maxPlayers the maximum player count of each service
     */
    void maxPlayers(int maxPlayers);

    /**
     * Gets the maximum memory of the group.
     *
     * @return the maximum memory of the group in MB
     */
    int maxMemory();

    /**
     * Sets the maximum memory of the group.
     *
     * @param maxMemory the maximum memory of the group in MB
     */
    void maxMemory(int maxMemory);

    /**
     * Gets whether the group is a fallback.
     *
     * @return {@code true} if the group is a fallback, otherwise {@code false}
     */
    boolean fallback();

    /**
     * Sets whether the group is a fallback.
     *
     * @param fallback {@code true} to make the group a fallback, otherwise {@code false}
     */
    void fallback(boolean fallback);

    /**
     * Gets whether the group is static.
     *
     * @return {@code true} if the group is static, otherwise {@code false}
     */
    boolean staticServices();

    /**
     * Gets the start priority of the group.
     *
     * @return the start priority of the group
     */
    int startPriority();

    /**
     * Sets the start priority of the group.
     *
     * @param startPriority the start priority of the group
     */
    void startPriority(int startPriority);

    /**
     * Gets the start percentage of the group.
     *
     * @return the start percentage of the group
     */
    int startPercentage();

    /**
     * Sets the start percentage of the group.
     *
     * @param startPercentage the start percentage of the group
     */
    void startPercentage(int startPercentage);

    /**
     * Gets the Java command used to start services of the group.
     *
     * @return the Java command of the group
     */
    String javaCommand();

    /**
     * Gets the custom JVM flags of the group.
     *
     * @return the custom JVM flags of the group
     */
    Set<String> customJvmFlags();

    /**
     * Adds a custom JVM flag to the group.
     *
     * @param flag the custom JVM flag to add
     */
    void addCustomJvmFlag(String flag);

    /**
     * Adds a template to the group.
     *
     * @param template the service template to add
     */
    void addTemplate(String template);

    /**
     * Removes a template from the group.
     *
     * @param template the service template to remove
     */
    void removeTemplate(String template);

    /**
     * Gets all services of the group.
     *
     * @return the list of all services of the group
     */
    default List<Service> services() {
        return CloudAPI.instance().serviceManager().services().stream()
                .filter(service -> service.group().equals(this))
                .toList();
    }
}
