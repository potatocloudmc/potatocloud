package net.potatocloud.webinterface.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@Jacksonized
public class ApiPlatformVersion {

    @Schema(description = "Name of the platform version", examples = "1.21.11")
    private String name;

    @Schema(description = "Full name of the version", examples = "paper-1.21.11")
    private String fullName;

    @Schema(description = "The download url of the version (if exists)", examples = "https://...")
    private String downloadUrl;

    @Schema(description = "The file hash of the file (if exists)", examples = "abc123def456...")
    private String fileHash;

    @Schema(description = "If the version is stored as jar on the server")
    private boolean local;

    @Schema(description = "If the version is legacy (older versions of the game e.g. 1.8.8)")
    private boolean legacy;


}
