package net.potatocloud.node.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

public record SecurityConfig(
        @JsonProperty("ssl-enabled") boolean sslEnabled,
        @JsonProperty("security-directory") Path securityDirectory,
        @JsonProperty("require-client-auth") boolean requireClientAuth
) {

    public net.potatocloud.network.security.SecurityConfig toNetworkConfig() {
        return new net.potatocloud.network.security.SecurityConfig(sslEnabled, securityDirectory, requireClientAuth);
    }
}