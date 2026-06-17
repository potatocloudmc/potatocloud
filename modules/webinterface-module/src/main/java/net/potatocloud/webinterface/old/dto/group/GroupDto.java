package net.potatocloud.webinterface.old.dto.group;

import lombok.Builder;
import net.potatocloud.api.group.Group;
import net.potatocloud.api.service.Service;
import net.potatocloud.webinterface.old.dto.platform.PlatformDto;
import net.potatocloud.webinterface.old.dto.platform.PlatformVersionDto;

import java.util.List;
import java.util.Set;

@Builder
public record GroupDto(
        String name,
        String javaCommand,
        PlatformDto platform,
        PlatformVersionDto platformVersion,
        boolean isStatic,
        boolean isFallback,
        boolean localNodeReady,
        int onlineServicesCount,
        int onlinePlayerCount,
        int minOnlineCount,
        int maxOnlineCount,
        int maxPlayerCount,
        int maxMemory,
        int startPriority,
        int newServicePercent,
        Set<String> customJvmFlags,
        Set<String> serviceTemplates,
        List<PropertyDto> properties,
        boolean useModernVelocityForwarding
) {

    public static GroupDto from(Group group, boolean localNodeReady) {
        return GroupDto.builder()
                .name(group.name())
                .javaCommand(group.javaCommand())
                .platform(PlatformDto.from(group.platform()))
                .platformVersion(PlatformVersionDto.from(group.platformVersion()))
                .isStatic(group.staticServices())
                .isFallback(group.fallback())
                .localNodeReady(localNodeReady)
                .onlineServicesCount(group.services().size())
                .onlinePlayerCount(
                        group.platform().proxy() ? group.services().stream().mapToInt(Service::playerCount).sum() : group.players().size()
                )
                .minOnlineCount(group.minServices())
                .maxOnlineCount(group.maxServices())
                .maxPlayerCount(group.maxPlayers())
                .maxMemory(group.maxMemory())
                .startPriority(group.startPriority())
                .newServicePercent(group.startPercentage())
                .customJvmFlags(group.customJvmFlags())
                .serviceTemplates(group.templates())
                .properties(group.properties().stream()
                        .map(PropertyDto::from)
                        .toList())
                .useModernVelocityForwarding(group.properties().stream()
                        .anyMatch(property -> "velocityModernForwarding".equals(property.name()) && Boolean.TRUE.equals(property.value())))
                .build();
    }
}

