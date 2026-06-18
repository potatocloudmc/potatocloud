package net.potatocloud.webinterface.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.node.Node;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.webinterface.dto.screen.ScreenInfoDto;
import net.potatocloud.webinterface.dto.screen.ScreenListDto;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ScreenService {

    private final Node node;

    public ScreenListDto getAvailableScreens() {
        List<ScreenInfoDto> screens = node.screenManager().getScreens().values().stream()
                .map(screen -> ScreenInfoDto.builder()
                        .name(screen.name())
                        .logCount(screen.cachedLogs().size())
                        .build())
                .collect(Collectors.toList());

        return ScreenListDto.builder()
                .screens(screens)
                .build();
    }

    public Screen getScreen(String name) {
        return node.screenManager().get(name);
    }
}
