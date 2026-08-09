package net.potatocloud.node.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ServiceConfig(
        @JsonProperty("service-start-port") int serviceStartPort,
        @JsonProperty("proxy-start-port") int proxyStartPort,
        String splitter,
        @JsonProperty("auto-update-platforms") boolean autoUpdatePlatforms,
        @JsonProperty("max-services") int maxServices,
        @JsonProperty("max-starting-services") int maxStartingServices,
        @JsonProperty("kill-timeout") int killTimeout,
        @JsonProperty("memory-check-enabled") boolean memoryCheckEnabled
) {}
 