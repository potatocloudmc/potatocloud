package net.potatocloud.node.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NodeConfig(
        ConsoleConfig console,
        ServiceConfig service,
        FoldersConfig folders,
        NodeNetworkConfig node,
        ClusterConfig cluster,
        SecurityConfig security,
        @JsonProperty("disable-update-checker") boolean disableUpdateChecker,
        @JsonProperty("delete-logs-after-days") int deleteLogsAfterDays,
        boolean debug
) {}