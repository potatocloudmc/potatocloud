package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
public class ApiServiceStats {

    @Schema(description = "Number of services currently running", examples = "3")
    private int running;
    @Schema(description = "Number of services currently starting", examples = "2")
    private int starting;
    @Schema(description = "Number of services currently stopping", examples = "1")
    private int stopping;
    @Schema(description = "Current memory usage of all services", examples = "4048")
    private int currentMemoryUsage;

}
