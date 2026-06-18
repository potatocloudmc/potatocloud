package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@Jacksonized
public class ApiPlatformVersion {

    private String name;
    private String fullName;
    private String downloadUrl;
    private String fileHash;
    private boolean local;
    private boolean legacy;

}
