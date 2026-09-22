package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Jacksonized
@Accessors(fluent = true, chain = true)
public class ApiStatsSummary {

    @Schema(description = "Uptime of the cloud in milliseconds", examples = "3600000")
    private long uptime;
    @Schema(description = "Number of groups in the cloud", examples = "5")
    private int groups;
    @Schema(description = "Number of templates in the cloud", examples = "10")
    private int services;
    @Schema(description = "Number of players currently online in the cloud", examples = "100")
    private int playerCount;

}
