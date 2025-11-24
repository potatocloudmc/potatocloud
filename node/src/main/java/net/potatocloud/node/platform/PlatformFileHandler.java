package net.potatocloud.node.platform;

import lombok.SneakyThrows;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.impl.PlatformImpl;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.node.console.Logger;
import org.apache.commons.io.FileUtils;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlatformFileHandler {

    private final Logger logger;
    private final File file;
    private final YamlFile config;

    public PlatformFileHandler(Logger logger) {
        this.logger = logger;
        this.file = new File("platforms.yml");

        // Create platforms.yml if missing
        if (!file.exists()) {
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream("platforms.yml")) {
                if (stream != null) {
                    FileUtils.copyInputStreamToFile(stream, file);
                }
            } catch (IOException e) {
                logger.error("Failed to copy platforms.yml file");
            }
        }

        this.config = mergePlatformsFile();
    }

    public List<Platform> loadPlatformsFile() {
        final List<Platform> platforms = new ArrayList<>();

        // Read all platforms from the merged config
        for (String key : config.getKeys(false)) {
            final ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            final PlatformImpl platform = new PlatformImpl(
                    key,
                    section.getString("download"),
                    section.getBoolean("custom", false),
                    section.getBoolean("proxy", false),
                    section.getString("base", "UNKNOWN"),
                    section.getString("pre-cache"),
                    section.getString("parser", ""),
                    section.getString("hash-type", ""),
                    section.getStringList("prepare-steps")
            );

            // Read versions of the platform
            final List<Map<?, ?>> versionMap = section.getMapList("versions");
            if (versionMap == null) {
                logger.warn("No versions found for platform " + key);
                continue;
            }

            final List<PlatformVersion> versions = new ArrayList<>();

            for (Map<?, ?> map : versionMap) {
                final String version = String.valueOf(map.get("version"));
                final String download = map.containsKey("download") ? String.valueOf(map.get("download")) : null;
                final boolean legacy = map.containsKey("legacy") && Boolean.parseBoolean(map.get("legacy").toString());
                final boolean local = map.containsKey("local") && Boolean.parseBoolean(map.get("local").toString());

                versions.add(new PlatformVersionImpl(key, version, local, download, legacy));
            }

            platform.getVersions().addAll(versions);
            platforms.add(platform);
        }
        return platforms;
    }

    @SneakyThrows
    private YamlFile mergePlatformsFile() {
        // Load the platforms file in the user directory
        final YamlFile userConfig = new YamlFile(file);
        userConfig.load();

        // Now load the default platforms config
        final YamlFile defaultConfig = new YamlFile();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("platforms.yml")) {
            if (stream != null) {
                defaultConfig.load(stream);
            }
        } catch (IOException e) {
            logger.error("Failed to copy platforms.yml file");
        }

        // Merge user and default config so the user config is always up to date
        final YamlFile mergedConfig = new YamlFile();
        for (String key : userConfig.getKeys(false)) {
            final ConfigurationSection section = userConfig.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            // We want to keep custom platforms of the user so set them again in the merged config as well
            if (section.getBoolean("custom", false)) {
                mergedConfig.set(key, section);
            }
        }

        // Add all other missing platforms
        for (String key : defaultConfig.getKeys(false)) {
            if (!mergedConfig.contains(key)) {
                mergedConfig.set(key, defaultConfig.get(key));
            }
        }

        // Save the new merged config and replace the old user config with it
        mergedConfig.save(file);
        return mergedConfig;
    }

    @SneakyThrows
    public void updatePlatform(Platform platform) {
        final List<Map<String, Object>> versions = new ArrayList<>();

        for (PlatformVersion version : platform.getVersions()) {
            final Map<String, Object> versionMap = new LinkedHashMap<>();

            versionMap.put("version", version.getName());
            versionMap.put("download", version.getDownloadUrl());
            versionMap.put("legacy", version.isLegacy());

            versions.add(versionMap);
        }

        config.set(platform.getName() + ".versions", versions);
        config.save(file);
    }

    @SneakyThrows
    public void addPlatform(Platform platform) {
        if (platform == null) {
            return;
        }

        final Map<String, Object> platformMap = new LinkedHashMap<>();
        putIfNotNull(platformMap, "download", platform.getDownloadUrl());
        putIfNotNull(platformMap, "base", platform.getBase());
        putIfNotNull(platformMap, "custom", platform.isCustom());
        putIfNotNull(platformMap, "pre-cache", platform.getPreCacheBuilder());
        putIfNotNull(platformMap, "parser", platform.getParser());
        putIfNotNull(platformMap, "proxy", platform.isProxy());
        putIfNotNull(platformMap, "hash-type", platform.getHashType());
        putIfNotNull(platformMap, "prepare-steps", new ArrayList<>(platform.getPrepareSteps()));

        final List<Map<String, Object>> versions = new ArrayList<>();

        for (PlatformVersion version : platform.getVersions()) {
            final Map<String, Object> versionMap = new LinkedHashMap<>();

            putIfNotNull(versionMap, "version", version.getName());
            putIfNotNull(versionMap, "download", version.getDownloadUrl());
            putIfNotNull(versionMap, "legacy", version.isLegacy());
            putIfNotNull(versionMap, "local", version.isLocal());

            versions.add(versionMap);
        }

        putIfNotNull(platformMap, "versions", versions);

        config.set(platform.getName(), platformMap);
        config.save(file);
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
