package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
public class ApiPlatform {

    private String name;
    private String base;
    private String downloadUrl;
    private boolean custom;
    private boolean proxy;
    private boolean bukkitBased;
    private boolean paperBased;
    private boolean velocityBased;
    private boolean limboBased;
    private List<ApiPlatformVersion> versions;
    private List<String> prepareSteps;

}
