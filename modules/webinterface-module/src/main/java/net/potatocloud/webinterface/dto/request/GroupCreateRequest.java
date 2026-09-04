package net.potatocloud.webinterface.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import net.potatocloud.webinterface.model.ApiProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Setter
@Getter
@Accessors(chain = true, fluent = true)
@Jacksonized
@Schema(name = "GroupCreateRequest", description = "Request to create a new group")
public class GroupCreateRequest {

    @NotBlank(message = "Group name is required")
    @Schema(
            description = "Unique name of the group",
            examples = "hub"
    )
    private String name;

    @NotBlank(message = "Platform is required")
    @Schema(
            description = "Target platform",
            examples = "paper"
    )
    private String platform;

    @NotBlank(message = "Platform version is required")
    @Schema(
            description = "Platform version identifier",
            examples = "1.21.8"
    )
    private String platformVersion;

    @NotNull(message = "Minimum service count is required")
    @Min(value = 1, message = "Minimum service count must be at least 1")
    @Schema(
            description = "Minimum number of online services",
            examples = "1"
    )
    private Integer minServices;

    @NotNull(message = "Maximum service count is required")
    @Min(value = 1, message = "Maximum service count must be at least 1")
    @Schema(
            description = "Maximum number of online services",
            examples = "10"
    )
    private Integer maxServices;

    @NotNull(message = "Maximum player count is required")
    @Min(value = 1, message = "Maximum player count must be at least 1")
    @Schema(
            description = "Maximum players per service",
            examples = "100"
    )
    private Integer maxPlayerCount;

    @NotNull(message = "Maximum memory is required")
    @Min(value = 128, message = "Maximum memory must be at least 128 MB")
    @Schema(
            description = "Maximum memory allocated per service in MB",
            examples = "1024"
    )
    private Integer maxMemory;

    @NotNull(message = "Fallback flag is required")
    @Schema(
            description = "Whether this group acts as a fallback group",
            examples = "false"
    )
    private Boolean fallback;

    @NotNull(message = "Static services flag is required")
    @Schema(
            description = "Whether services in this group are static",
            examples = "false"
    )
    private Boolean staticServices;

    @NotNull(message = "Velocity forwarding flag is required")
    @Schema(
            description = "Whether modern Velocity forwarding should be enabled",
            examples = "true"
    )
    private Boolean useModernVelocityForwarding;

    @NotNull(message = "Start priority is required")
    @Min(value = 0, message = "Start priority must be between 0 and 100")
    @Max(value = 100, message = "Start priority must be between 0 and 100")
    @Schema(
            description = "Group startup priority",
            examples = "50"
    )
    private int startPriority;

    @NotNull(message = "Start percentage is required")
    @Min(value = -1, message = "Start percentage must be between -1 and 100")
    @Max(value = 100, message = "Start percentage must be between -1 and 100")
    @Schema(
            description = "Percentage threshold used for dynamic service startup",
            examples = "75"
    )
    private int startPercentage;

    @Schema(
            description = "Custom Java command used to start services",
            examples = "java"
    )
    private String javaCommand;

    @Schema(
            description = "Additional JVM flags"
    )
    private List<String> customJvmFlags;

    @Schema(
            description = "Custom group properties"
    )
    private List<@Valid ApiProperty> properties;

    @AssertTrue(message = "Maximum service count must be greater than or equal to minimum service count")
    public boolean isServiceRangeValid() {
        if (minServices == null || maxServices == null) {
            return true;
        }

        return maxServices >= minServices;
    }

}
