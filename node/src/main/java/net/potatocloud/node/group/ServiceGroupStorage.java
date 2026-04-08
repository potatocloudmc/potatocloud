package net.potatocloud.node.group;

import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.impl.ServiceGroupImpl;
import net.potatocloud.api.property.Property;
import net.potatocloud.node.utils.YamlUtils;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class ServiceGroupStorage {

    private ServiceGroupStorage() {
    }

    public static void saveToFile(ServiceGroup group, Path directory) {
        final YamlFile config = new YamlFile(directory.resolve(group.getName() + ".yml").toFile());

        config.set("name", group.getName());
        config.set("platform", group.getPlatformName());
        config.set("platform-version", group.getPlatformVersionName());
        config.set("java-command", group.getJavaCommand());

        if (!group.getCustomJvmFlags().isEmpty()) {
            config.set("jvm-flags", group.getCustomJvmFlags());
        }

        config.set("max-players", group.getMaxPlayers());
        config.set("max-memory", group.getMaxMemory());
        config.set("min-online-count", group.getMinOnlineCount());
        config.set("max-online-count", group.getMaxOnlineCount());

        config.set("static", group.isStatic());
        config.set("fallback", group.isFallback());
        config.set("start-priority", group.getStartPriority());
        config.set("start-percentage", group.getStartPercentage());

        config.set("templates", group.getServiceTemplates());

        if (!group.getProperties().isEmpty()) {
            for (Property<?> property : group.getProperties()) {
                config.set("properties." + property.getName() + ".value", property.getValue());
                config.set("properties." + property.getName() + ".default", property.getDefaultValue());
            }
        }

        try {
            config.save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save service group to file: " + config.getFilePath(), e);
        }
    }

    public static ServiceGroup loadFromFile(File file) {
        final YamlFile config = new YamlFile(file);
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load service group from file: " + file, e);
        }

        return new ServiceGroupImpl(
                config.getString("name"),
                config.getString("platform"),
                config.getString("platform-version"),
                config.getString("java-command"),
                config.getStringList("jvm-flags"),
                config.getInt("max-players"),
                config.getInt("max-memory"),
                config.getInt("min-online-count"),
                config.getInt("max-online-count"),
                config.getBoolean("static"),
                config.getBoolean("fallback"),
                config.getInt("start-priority"),
                config.getInt("start-percentage"),
                config.getStringList("templates"),
                YamlUtils.getProperties(config)
        );
    }
}