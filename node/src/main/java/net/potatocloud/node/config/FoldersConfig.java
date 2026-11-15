package net.potatocloud.node.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FoldersConfig(
        String groups,
        @JsonProperty("static") String staticServices,
        @JsonProperty("temp-services") String tempServices,
        String templates,
        String platforms,
        String modules,
        String logs,
        @JsonProperty("log-archive") String logArchive,
        String data,
        String backups
) {}
 