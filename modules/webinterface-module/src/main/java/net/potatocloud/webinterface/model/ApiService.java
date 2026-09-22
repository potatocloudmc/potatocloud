package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import net.potatocloud.api.service.ServiceState;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Duration;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
public class ApiService {

    @Schema(description = "Unique identifier of the service", examples = "1")
    private int id;

    @Schema(description = "Group of the service", examples = "Lobby")
    private String group;

    @Schema(description = "Name of the service", examples = "Lobby-1")
    private String name;

    @Schema(description = "Uptime of the service in seconds", examples = "68.810360711")
    private Duration uptime;

    @Schema(description = "Current state of the service", examples = "RUNNING")
    private ServiceState state;

    @Schema(description = "Port on which the service is running", examples = "25565")
    private int port;

    @Schema(description = "Maximum number of players allowed on the service", examples = "100")
    private int maxPlayers;

    @Schema(description = "Current number of players connected to the service", examples = "42")
    private int playerCount;

    @Schema(description = "Amount of memory allocated to the service in megabytes", examples = "1024")
    private int usedMemory;

}
