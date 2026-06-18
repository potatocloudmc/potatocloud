package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
public class ApiProperty {

    private Object value;
    private Object defaultValue;
    private String name;

}
