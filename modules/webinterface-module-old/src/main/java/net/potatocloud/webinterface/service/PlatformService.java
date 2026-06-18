package net.potatocloud.webinterface.service;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.webinterface.dto.platform.PlatformDto;

import java.util.List;

@RequiredArgsConstructor
public class PlatformService {

    private final CloudAPI cloudAPI;

    public List<PlatformDto> getPlatforms() {
        return cloudAPI.platformManager().platforms().stream()
                .map(PlatformDto::from)
                .toList();
    }
}

