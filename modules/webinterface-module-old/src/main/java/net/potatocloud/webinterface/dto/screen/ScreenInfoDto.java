package net.potatocloud.webinterface.dto.screen;

import lombok.Builder;


@Builder
public record ScreenInfoDto(String name, int logCount) {
}