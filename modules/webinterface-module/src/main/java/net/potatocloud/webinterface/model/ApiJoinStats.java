package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
public class ApiJoinStats {

    @Schema(description = "Total number of joins in the cloud", examples = "1000")
    private int total;
    @Schema(description = "Number of joins in the last hour")
    private List<ApiJoinPoint> data;

}
