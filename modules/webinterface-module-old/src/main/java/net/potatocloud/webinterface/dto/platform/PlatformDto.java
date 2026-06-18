package net.potatocloud.webinterface.dto.platform;

import lombok.Builder;
import net.potatocloud.api.platform.Platform;

import java.util.List;


@Builder
public record PlatformDto(
        String name,
        String base,
        String downloadUrl,
        boolean custom,
        boolean proxy,
        boolean bukkitBased,
        boolean paperBased,
        boolean velocityBased,
        boolean limboBased,
        List<PlatformVersionDto> versions,
        List<String> prepareSteps
) {

    public static PlatformDto from(Platform platform) {
        return PlatformDto.builder()
                .name(platform.name())
                .base(platform.base().id())
                .downloadUrl(platform.downloadUrl())
                .custom(platform.custom())
                .proxy(platform.proxy())
                .bukkitBased(platform.bukkitBased())
                .paperBased(platform.paperBased())
                .velocityBased(platform.velocityBased())
                .limboBased(platform.limboBased())
                .versions(platform.versions().stream().map(PlatformVersionDto::from).toList())
                .prepareSteps(platform.prepareSteps())
                .build();
    }
}

