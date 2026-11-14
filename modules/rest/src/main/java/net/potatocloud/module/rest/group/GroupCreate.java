package net.potatocloud.module.rest.group;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record GroupCreate(
        String name,
        String node,
        String platform,
        String platformVersion,
        List<String> templates,
        Integer minServices,
        Integer maxServices,
        Integer maxPlayers,
        Integer maxMemory,
        Boolean fallback,
        Boolean staticServices,
        Integer startPriority,
        Integer startPercentage,
        String javaCommand,
        Set<String> customJvmFlags,
        Map<String, Object> properties
) {}
