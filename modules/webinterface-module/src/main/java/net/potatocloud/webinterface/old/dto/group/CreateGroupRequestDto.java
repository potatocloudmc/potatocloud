package net.potatocloud.webinterface.old.dto.group;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateGroupRequestDto(
        String name,
        String platform,
        String platformVersion,
        int minOnlineCount,
        int maxOnlineCount,
        int maxPlayerCount,
        int maxMemory,
        boolean fallback,
        @JsonProperty("static")
        @JsonAlias("isStatic")
        boolean isStatic,
        boolean useModernVelocityForwarding,
        int startPriority,
        int newServicePercent,
        String startCommand,
        List<String> customJvmFlags,
        List<PropertyDto> properties
) {
}
