package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
public class ApiPlayer {

    private String username;
    private UUID uniqueId;
    private int proxyId;
    private int serverId;
    private String proxyName;
    private String serverName;
    private String serverGroup;
    private String proxyGroup;
    private List<ApiProperty> properties;

}
