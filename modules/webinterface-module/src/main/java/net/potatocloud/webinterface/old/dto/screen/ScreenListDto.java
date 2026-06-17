package net.potatocloud.webinterface.old.dto.screen;

import lombok.Builder;

import java.util.List;

@Builder
public record ScreenListDto(List<ScreenInfoDto> screens) {
}
