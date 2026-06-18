package net.potatocloud.webinterface.dto.stats;

import lombok.Builder;


@Builder
public record JoinPointDto(String hour, int joins) {
}


