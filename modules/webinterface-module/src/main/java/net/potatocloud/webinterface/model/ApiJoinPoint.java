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
public class ApiJoinPoint {

    @Schema(description = "Hour of the day (0-23)", examples = "14")
    private String hour;
    @Schema(description = "Number of joins in this hour", examples = "100")
    private int joins;

}
