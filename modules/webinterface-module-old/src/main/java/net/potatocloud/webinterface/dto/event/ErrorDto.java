package net.potatocloud.webinterface.dto.event;

import lombok.Builder;

@Builder
public record ErrorDto(String error) {
}