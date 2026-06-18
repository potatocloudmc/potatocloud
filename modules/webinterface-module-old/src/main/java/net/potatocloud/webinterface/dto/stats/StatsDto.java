package net.potatocloud.webinterface.dto.stats;

import lombok.Builder;


@Builder
public record StatsDto(long uptime, int groups, int services, int onlinePlayers) {
}


