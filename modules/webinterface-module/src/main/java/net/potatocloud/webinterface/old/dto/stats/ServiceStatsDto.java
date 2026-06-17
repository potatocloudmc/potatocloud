package net.potatocloud.webinterface.old.dto.stats;

import lombok.Builder;

@Builder
public record ServiceStatsDto(int running, int starting, int stopping, int currentMemoryUsage) {
}
