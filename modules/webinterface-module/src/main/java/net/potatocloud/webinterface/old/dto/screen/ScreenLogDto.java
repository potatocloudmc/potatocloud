package net.potatocloud.webinterface.old.dto.screen;

import lombok.Builder;

@Builder
public record ScreenLogDto(String screenName, String line) {
}
