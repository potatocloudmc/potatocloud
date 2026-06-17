package net.potatocloud.webinterface.old.dto.event;

import lombok.Builder;

@Builder
public record ErrorDto(String error) {
}