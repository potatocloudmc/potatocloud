package net.potatocloud.webinterface.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSummaryResponse {

    @Schema(description = "Name of the platform", examples = "paper")
    private String name;

    @Schema(description = "Base type of the platform (e.g. Leaf is based on PAPER)", examples = "PAPER")
    private String base;

    @Schema(description = "Download url of the platform", examples = "https://fill-data.papermc.io/v1/objects/{sha256}/paper-{version}-{build}.jar")
    private String downloadUrl;

    @Schema(description = "If the platform is created by a user")
    private boolean custom;

    @Schema(description = "If the platform is a proxy")
    private boolean proxy;

    @Schema(description = "If the platform is based on a bukkit based server software (e.g. Spigot, Paper, Waterfall, Velocity, etc.)")
    private boolean bukkitBased;

    @Schema(description = "If the platform is based on a sponge based server software (e.g. Leaf)")
    private boolean paperBased;

    @Schema(description = "If the platform is based on a velocity based server software")
    private boolean velocityBased;

    @Schema(description = "If the platform is based on a limbo based server software")
    private boolean limboBased;

    @Schema(description = "List of steps to prepare the platform (e.g. download, extract, etc.)", examples = {"default-files", "eula", "port", "setup-proxy"})
    private List<String> prepareSteps;

}
