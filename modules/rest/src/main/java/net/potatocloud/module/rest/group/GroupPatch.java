package net.potatocloud.module.rest.group;

import java.util.List;
import java.util.Set;

public record GroupPatch(
        String node,
        String platform,
        String platformVersion,
        List<String> templates,
        Integer minServices,
        Integer maxServices,
        Integer maxPlayers,
        Integer maxMemory,
        Boolean fallback,
        Integer startPriority,
        Integer startPercentage,
        String javaCommand,
        Set<String> customJvmFlags
) {}
