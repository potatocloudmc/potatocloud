package net.potatocloud.node.platform;

import lombok.SneakyThrows;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.platform.PrepareStep;
import net.potatocloud.api.platform.impl.PlatformImpl;
import net.potatocloud.api.platform.impl.PlatformVersionImpl;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.PacketIds;
import net.potatocloud.core.networking.packets.platform.PlatformAddPacket;
import net.potatocloud.node.console.Logger;
import net.potatocloud.node.platform.steps.*;
import org.apache.commons.io.FileUtils;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlatformManagerImpl implements PlatformManager {

    private final List<Platform> platforms = new ArrayList<>();
    private final File platformsFile = new File("platforms.yml");
    private final Logger logger;

    public PlatformManagerImpl(Logger logger, NetworkServer server) {
        this.logger = logger;

        server.registerPacketListener(PacketIds.REQUEST_PLATFORMS, (connection, packet) -> {
            for (Platform platform : platforms) {
                connection.send(new PlatformAddPacket(platform));
            }
        });
    }

    @SneakyThrows
    public void loadPlatformsFile() {
        if (!platformsFile.exists()) {
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream("platforms.yml")) {
                if (stream != null) {
                    FileUtils.copyInputStreamToFile(stream, platformsFile);
                }
            } catch (IOException e) {
                logger.error("Failed to copy platforms.yml file");
            }
        }

        // load the platforms file in the user directory
        final YamlFile userConfig = new YamlFile(platformsFile);
        userConfig.load();

        // now load the default platforms config
        final YamlConfiguration defaultConfig = new YamlConfiguration();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("platforms.yml")) {
            if (stream == null) {
                return;
            }
            defaultConfig.load(stream);
        } catch (IOException e) {
            logger.error("Failed to copy platforms.yml file");
        }

        // merge user and default config so the user config is always up to date
        final YamlConfiguration mergedConfig = new YamlConfiguration();
        for (String key : userConfig.getKeys(false)) {
            final ConfigurationSection section = userConfig.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            // we want to keep custom platforms of the user so lets set them again in the merged config as well
            if (section.getBoolean("custom", false)) {
                mergedConfig.set(key, section);
            }
        }

        // set all other missing platforms
        for (String key : defaultConfig.getKeys(false)) {
            if (!mergedConfig.contains(key)) {
                mergedConfig.set(key, defaultConfig.get(key));
            }
        }

        // save the new merged config and replace the old user config with it
        mergedConfig.save(platformsFile);

        // now read all platforms
        for (String key : mergedConfig.getKeys(false)) {
            final ConfigurationSection section = mergedConfig.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            final String parser = section.getString("parser", "");
            final String hashType = section.getString("hash-type", "");

            final List<Map<?, ?>> versionMap = section.getMapList("versions");
            if (versionMap == null) {
                logger.warn("No versions found for platform " + key);
                continue;
            }

            final List<PlatformVersion> versions = new ArrayList<>();

            // read versions of the platform
            for (Map<?, ?> map : versionMap) {
                versions.add(new PlatformVersionImpl(
                        key,
                        map.get("version").toString(),
                        map.get("download") != null ? map.get("download").toString() : null,
                        parser,
                        hashType,
                        map.containsKey("legacy") && Boolean.parseBoolean(map.get("legacy").toString())
                ));
            }

            final String downloadUrl = section.getString("download");
            final boolean custom = section.getBoolean("custom", false);
            final boolean proxy = section.getBoolean("proxy", false);
            final String base = section.getString("base", "UNKNOWN");
            final List<String> steps = new ArrayList<>(section.getStringList("prepare-steps"));
            final String preCache = section.getString("pre-cache");

            // create platform with read infos
            final PlatformImpl platform = new PlatformImpl(key, downloadUrl, custom, proxy, base, preCache);
            platform.getPrepareSteps().addAll(steps);
            platform.getVersions().addAll(versions);
            platforms.add(platform);
        }

        logger.info("Loaded &a" + platforms.size() + " &7platforms");
    }

    public PrepareStep getStep(final String stepName) {
        switch (stepName.toLowerCase()) {
            case "default-files":
                return new DefaultFilesStep();
            case "eula":
                return new EulaStep();
            case "port":
                return new PortStep();
            case "setup-forwarding":
                return new SetupForwardingStep();
            case "setup-proxy":
                return new SetupProxyStep();
            default:
                logger.warn("Unknown prepare step: " + stepName);
                return null;
        }
    }

    public List<Platform> getPlatforms() {
        return Collections.unmodifiableList(platforms);
    }
}