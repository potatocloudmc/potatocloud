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
public class ApiProperty {

    @Schema(description = "The value of the property", examples = "true")
    private Object value;

    @Schema(description = "The default value of the property", examples = "false")
    private Object defaultValue;

    @Schema(description = "The name of the property", examples = "isPotato")
    private String name;

}
