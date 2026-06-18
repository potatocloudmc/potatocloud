package net.potatocloud.webinterface.dto.screen;

import lombok.Builder;

@Builder
public record ScreenLogDto(String screenName, String line) {
}
