package net.potatocloud.webinterface.old.dto.screen;

import lombok.Builder;


@Builder
public record ScreenInfoDto(String name, int logCount) {
}