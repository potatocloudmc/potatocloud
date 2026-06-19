package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@Jacksonized
public class ApiGroup {

    @Schema(description = "Name of the group", examples = "hub")
    private String name;

    @Schema(description = "Java command of the group", examples = "java")
    private String javaCommand;

    @Schema(description = "The platform of the group")
    private ApiPlatform platform;

    @Schema(description = "The platform version of the group")
    private ApiPlatformVersion platformVersion;

    @Schema(description = "Whether the group is static (services are not deleted when stopped)")
    private boolean staticServices;

    @Schema(description = "Whether the group is a fallback (should be enabled for lobby-servers)")
    private boolean fallback;

    @Schema(description = "How many services of the group are currently online")
    private int onlineServicesCount;

    @Schema(description = "How many players are currently online in the group")
    private int onlinePlayerCount;

    @Schema(description = "Minimum number of services that should be online in the group")
    private int minServices;

    @Schema(description = "Maximum number of services that can be online in the group")
    private int maxServices;

    @Schema(description = "Maximum number of players allowed in each service of the group")
    private int maxPlayers;

    @Schema(description = "Amount of memory allocated to each service of the group in megabytes")
    private int maxMemory;

    @Schema(description = "Priority of the group when starting new services (higher priority groups are started first)")
    private int startPriority;

    @Schema(description = "Percentage of new services that should be started in this group (used for load balancing between multiple groups with the same priority)")
    private int startPercentage;

    @Schema(description = "Custom JVM flags that are added when starting a service of the group", examples = {"-Xmx1024M", "-Xms512M"})
    private Set<String> customJvmFlags;

    @Schema(description = "Custom service templates that are used when starting a service of the group", examples = {"template1", "template2"})
    private Set<String> templates;

    @Schema(description = "Custom properties that are added to each service of the group")
    private List<ApiProperty> properties;

    @Schema(description = "Whether to use modern velocity forwarding (only for velocity based platforms)")
    private boolean useModernVelocityForwarding;


}
