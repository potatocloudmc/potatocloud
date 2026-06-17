package net.potatocloud.webinterface.old.dto.stats;

import lombok.Builder;


@Builder
public record JoinPointDto(String hour, int joins) {
}


