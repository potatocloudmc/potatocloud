package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@Jacksonized
public class ApiGroup {

    private String name;
    private String javaCommand;
    private ApiPlatform platform;
    private ApiPlatformVersion platformVersion;
    private boolean isStatic;
    private boolean fallback;
    private boolean localNodeReady;
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
