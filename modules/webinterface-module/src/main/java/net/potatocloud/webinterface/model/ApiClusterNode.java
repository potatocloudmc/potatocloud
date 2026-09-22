package net.potatocloud.webinterface.model;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@Jacksonized
public class ApiClusterNode {

    @Schema(description = "Name of the node", examples = "node-1")
    private String name;
    @Schema(description = "Host address of the node", examples = "127.0.0.1")
    private String host;
    @Schema(description = "Port of the node", examples = "8080")
    private int port;
    @Schema(description = "Timestamp of the node start", examples = "2023-01-01T00:00:00Z")
    private Instant startedAt;

}
