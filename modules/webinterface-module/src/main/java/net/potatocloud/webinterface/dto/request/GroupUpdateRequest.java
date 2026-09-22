package net.potatocloud.webinterface.dto.request;


//        String groupName,
//        Set<String> customJvmFlags,


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import net.potatocloud.webinterface.model.ApiProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Set;

@Setter
@Getter
@Accessors(chain = true, fluent = true)
@Jacksonized
@Schema(name = "GroupUpdateRequest", description = "Request to update a new group")
public class GroupUpdateRequest {

    @Schema(description = "Additional JVM flags")
    private List<String> customJvmFlags;

    @Schema(description = "Maximum number of players allowed in each service of the group")
    @Min(value = 1, message = "Maximum player count must be at least 1")
    private int maxPlayers;

    @Schema(description = "Maximum memory allocated to each service of the group (in MB)")
    @Min(value = 128, message = "Maximum memory must be at least 128 MB")
    private int maxMemory;

    @Schema(description = "Minimum number of online services for the group")
    @Min(value = 1, message = "Minimum service count must be at least 1")
    private int minServices;

    @Schema(description = "Maximum number of online services for the group")
    @Min(value = 1, message = "Maximum service count must be at least 1")
    private int maxServices;

    @Schema(description = "Whether the group is a fallback group")
    private boolean fallback;

    @Schema(description = "Whether the group is static (services will not be automatically started or stopped)")
    @Min(value = 0, message = "Start priority must be at least 0")
    @Max(value = 100, message = "Start priority must be between 0 and 100")
    private int startPriority;

    @Schema(description = "Percentage of new services to start when the group is started")
    @Min(value = -1, message = "Start percentage must be between -1 and 100")
    private int startPercentage;

    @Schema(description = "Set of service templates associated with the group")
    private Set<String> templates;

    @Schema(description = "Map of custom properties for the group")
    private List<ApiProperty> properties;

}
