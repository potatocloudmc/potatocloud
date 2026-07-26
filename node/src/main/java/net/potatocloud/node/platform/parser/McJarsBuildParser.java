package net.potatocloud.node.platform.parser;

import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.platform.BuildParser;
import net.potatocloud.node.utils.RequestUtil;
import tools.jackson.databind.JsonNode;

import java.util.Comparator;
import java.util.Map;

public final class McJarsBuildParser implements BuildParser {

    private static final String API = "https://mcjars.app/api/v1/builds/";

    private final String name;
    private final String type;

    public McJarsBuildParser(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void parse(PlatformVersion version, String baseUrl) {
        try {
            final String minecraftVersion = version.name().equalsIgnoreCase("latest")
                    ? latestVersion()
                    : version.name();

            final JsonNode response = RequestUtil.request(API + type + "/" + minecraftVersion);
            final JsonNode builds = response.get("builds");

            if (builds == null || !builds.isArray() || builds.isEmpty()) {
                throw new IllegalStateException("No builds found for MCJars platform: " + name + " " + minecraftVersion);
            }

            final JsonNode build = builds.get(0);
            if (build == null || !build.isObject()) {
                throw new IllegalStateException("Invalid build returned for MCJars platform: " + name + " " + minecraftVersion);
            }

            final JsonNode jarUrl = build.get("jarUrl");
            final JsonNode zipUrl = build.get("zipUrl");

            String downloadUrl = jarUrl == null ? "" : jarUrl.asString();
            if (downloadUrl.isBlank()) {
                downloadUrl = zipUrl == null ? "" : zipUrl.asString();
            }

            if (downloadUrl.isBlank()) {
                throw new IllegalStateException("MCJars did not provide a server JAR for: " + name + " " + minecraftVersion);
            }

            if (version instanceof PlatformVersionImpl platformVersion) {
                platformVersion.resolvedName(minecraftVersion);
                platformVersion.downloadUrl(downloadUrl);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse MCJars build for: " + name + " : " + version.name(), e);
        }
    }

    private String latestVersion() {
        final JsonNode response = RequestUtil.request(API + type);
        final JsonNode versions = response.get("versions");

        if (versions == null || !versions.isObject()) {
            throw new IllegalStateException("No versions for platform: " + name);
        }

        return versions.properties().stream()
                .filter(entry -> {
                    final JsonNode supported = entry.getValue().get("supported");
                    return supported != null && supported.asBoolean();
                })
                .filter(entry -> {
                    final JsonNode releaseType = entry.getValue().get("type");
                    return releaseType != null && releaseType.asString().equalsIgnoreCase("RELEASE");
                })
                .filter(entry -> entry.getValue().get("created") != null)
                .max(Comparator.comparing(entry -> entry.getValue().get("created").asString()))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("No supported release versions found for MCJars platform: " + name));
    }

    @Override
    public String getName() {
        return name;
    }
}
