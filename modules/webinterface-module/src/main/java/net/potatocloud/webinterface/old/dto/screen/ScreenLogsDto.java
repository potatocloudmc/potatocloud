package net.potatocloud.webinterface.old.dto.screen;

import lombok.Builder;

import java.util.List;

@Builder
public record ScreenLogsDto(String screen, List<String> logs) {
}


