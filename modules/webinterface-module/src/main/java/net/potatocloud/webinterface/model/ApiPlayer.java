package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
public class ApiPlayer {

    @Schema(description = "Username of the player", examples = "Notch")
    private String username;

    @Schema(description = "Unique identifier of the player", examples = "123e4567-e89b-12d3-a456-426614174000")
    private UUID uniqueId;

    @Schema(description = "ID of the connected proxy", examples = "1")
    private int proxyId;

    @Schema(description = "ID of the connected service", examples = "1")
    private int serverId;

    @Schema(description = "Service name of the connected proxy", examples = "proxy-1")
    private String proxyName;

    @Schema(description = "Service name of the connected service", examples = "hub-1")
    private String serverName;

    @Schema(description = "Group name of the service", examples = "hub")
    private String serverGroup;

    @Schema(description = "Group name of the proxy", examples = "proxy")
    private String proxyGroup;

    @Schema(description = "List of custom properties associated with the player")
    private List<ApiProperty> properties;

}
