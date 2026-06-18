package net.potatocloud.webinterface.dto.service;

import lombok.Builder;
import net.potatocloud.api.service.Service;

@Builder
public record ServiceDto(
        int serviceId,
        String name,
        String formattedUptime,
        long uptime,
        int port,
        int maxPlayers,
        int onlinePlayerCount,
        int usedMemory
) {

    public static ServiceDto from(Service service) {
        return ServiceDto.builder()
                .serviceId(service.id())
                .name(service.name())
                .formattedUptime(service.uptime().toString())
                .uptime(service.uptime().toMillis())
                .port(service.port())
                .maxPlayers(service.maxPlayers())
                .onlinePlayerCount(service.playerCount())
                .usedMemory(service.usedMemory())
                .build();
    }
}

