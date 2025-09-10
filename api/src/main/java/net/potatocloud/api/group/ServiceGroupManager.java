package net.potatocloud.api.group;

import net.potatocloud.api.property.Property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface ServiceGroupManager {

    ServiceGroup getServiceGroup(String name);

    List<ServiceGroup> getAllServiceGroups();

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
                new HashSet<>()
        );
    }

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
            Set<Property> properties
    );


    void deleteServiceGroup(String name);

    default void deleteServiceGroup(ServiceGroup group) {
        deleteServiceGroup(group.getName());
    }

    void updateServiceGroup(ServiceGroup group);

    boolean existsServiceGroup(String name);

}
