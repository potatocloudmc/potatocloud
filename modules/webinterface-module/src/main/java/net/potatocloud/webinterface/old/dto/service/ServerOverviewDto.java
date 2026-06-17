package net.potatocloud.webinterface.old.dto.service;

import lombok.Builder;
import net.potatocloud.api.service.Service;

@Builder
public record ServerOverviewDto(
        String service,
        String serviceStatus,
        int playerCount,
        int maxPlayerCount,
        long uptime
) {

    public static ServerOverviewDto from(Service service) {
        return ServerOverviewDto.builder()
                .service(service.name())
                .serviceStatus(service.state().name())
                .playerCount(service.playerCount())
                .maxPlayerCount(service.maxPlayers())
                .uptime(service.startedAt().toEpochMilli())
                .build();
    }

}
