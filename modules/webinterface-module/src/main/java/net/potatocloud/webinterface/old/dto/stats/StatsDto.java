package net.potatocloud.webinterface.old.dto.stats;

import lombok.Builder;


@Builder
public record StatsDto(long uptime, int groups, int services, int onlinePlayers) {
}


