package net.potatocloud.webinterface.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import net.potatocloud.webinterface.model.ApiPlatformVersion;
import net.potatocloud.webinterface.model.ApiProperty;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Jacksonized
@Accessors(fluent = true, chain = true)
public class GroupSummaryResponse {

    private String name;
    private String javaCommand;
    private String platformName;
    private ApiPlatformVersion platformVersion;
    private boolean staticServices;
    private boolean fallback;
    private int onlineServicesCount;
    private int onlinePlayerCount;
    private int minOnlineCount;
    private int maxOnlineCount;
    private int maxPlayerCount;
    private int maxMemory;
    private int startPriority;
    private int newServicePercentage;
    private Set<String> customJvmFlags;
    private Set<String> serviceTemplates;
    private List<ApiProperty> properties;
    private boolean useModernVelocityForwarding;

}
