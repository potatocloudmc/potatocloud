package net.potatocloud.webinterface.dto.stats;

import lombok.Builder;

import java.util.List;

@Builder
public record JoinStatsDto(int total, List<JoinPointDto> data) {
}

