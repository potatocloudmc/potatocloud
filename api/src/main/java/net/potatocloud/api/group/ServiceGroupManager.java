package net.potatocloud.api.group;

import net.potatocloud.api.property.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ServiceGroupManager {

    /**
     * Gets a service group by its name.
     *
     * @param name the name of the service group
     * @return the service group
     */
    ServiceGroup getServiceGroup(String name);

    /**
     * Gets the list of all service groups.
     *
     * @return the list of all service groups
     */
    List<ServiceGroup> getAllServiceGroups();

    /**
     * Creates a new service group with the given configuration.
     */
    default void createServiceGroup(
            String name,
            String platformName,
            String platformVersionName,
            int minOnlineCount,
            int maxOnlineCount,
            int maxPlayers,
            int maxMemory,
            boolean fallback,
            boolean isStatic,
            int startPriority,
            int startPercentage
    ) {
        createServiceGroup(
                name,
                platformName,
                platformVersionName,
                minOnlineCount,
                maxOnlineCount,
                maxPlayers,
                maxMemory,
                fallback,
                isStatic,
                startPriority,
                startPercentage,
                "java",
                new ArrayList<>(),
                new HashMap<>()
        );
    }

    /**
     * Creates a new service group with the given configuration.
     */
    void createServiceGroup(
            String name,
            String platformName,
            String platformVersionName,
            int minOnlineCount,
            int maxOnlineCount,
            int maxPlayers,
            int maxMemory,
            boolean fallback,
            boolean isStatic,
            int startPriority,
            int startPercentage,
            String javaCommand,
            List<String> customJvmFlags,
            Map<String, Property<?>> propertyMap
    );

    /**
     * Deletes the given service group.
     *
     * @param name the name of the service group
     */
    void deleteServiceGroup(String name);

    /**
     * Deletes the given service group.
     *
     * @param group the service group to delete
     */
    default void deleteServiceGroup(ServiceGroup group) {
        deleteServiceGroup(group.getName());
    }

    /**
     * Updates an existing service group.
     *
     * @param group the service group to update
     */
    void updateServiceGroup(ServiceGroup group);

    /**
     * Checks if a service group exists by name.
     *
     * @param name the name of the group
     * @return {@code true} if the group exists, otherwise {@code false}
     */
    boolean existsServiceGroup(String name);

}
