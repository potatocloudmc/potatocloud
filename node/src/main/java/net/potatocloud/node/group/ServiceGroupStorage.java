package net.potatocloud.node.group;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.potatocloud.api.group.ServiceGroup;
import net.potatocloud.api.group.impl.ServiceGroupImpl;
import net.potatocloud.api.property.Property;
import org.simpleyaml.configuration.file.YamlFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ServiceGroupStorage {

    @SneakyThrows
    public void saveToFile(ServiceGroup group, Path directory) {
        final YamlFile config = new YamlFile(directory.resolve(group.getName() + ".yml").toFile());
        config.set("name", group.getName());
        config.set("platform", group.getPlatform().getName() + "-" + group.getPlatformVersionName());
        config.set("templates", group.getServiceTemplates());
        config.set("minOnlineCount", group.getMinOnlineCount());
        config.set("maxOnlineCount", group.getMaxOnlineCount());
        config.set("maxPlayers", group.getMaxPlayers());
        config.set("maxMemory", group.getMaxMemory());
        config.set("fallback", group.isFallback());
        config.set("static", group.isStatic());
        config.set("startPriority", group.getStartPriority());
        config.set("startPercentage", group.getStartPercentage());
        config.set("javaCommand", group.getJavaCommand());

        if (!group.getCustomJvmFlags().isEmpty()) {
            config.set("customJvmFlags", group.getCustomJvmFlags());
        }

        if (!group.getProperties().isEmpty()) {
            for (Property<?> property : group.getProperties()) {
                config.set("properties." + property.getName() + ".value", property.getValue());
                config.set("properties." + property.getName() + ".default", property.getDefaultValue());
            }
        }

        config.save();
    }

    @SneakyThrows
    public ServiceGroup loadFromFile(Path groupFile) {
        final YamlFile config = new YamlFile(groupFile.toFile());
        config.load();

        final Map<String, Property<?>> properties = new HashMap<>();
        if (config.isSet("properties")) {
            for (String key : config.getConfigurationSection("properties").getKeys(false)) {
                final Object value = config.get("properties." + key + ".value");
                Object defaultValue = config.get("properties." + key + ".default");
                if (defaultValue == null) {
                    defaultValue = value;
                }

                properties.put(key, Property.of(key, defaultValue, value));
            }
        }

        final String platformFullName = config.getString("platform");
        final String[] parts = platformFullName.split("-", 2);
        final String platformName = parts[0];
        final String platformVersion = parts[1];

        return new ServiceGroupImpl(
                config.getString("name"),
                platformName,
                platformVersion,
                config.getStringList("templates"),
                config.getInt("minOnlineCount"),
                config.getInt("maxOnlineCount"),
                config.getInt("maxPlayers"),
                config.getInt("maxMemory"),
                config.getBoolean("fallback"),
                config.getBoolean("static"),
                config.getInt("startPriority"),
                config.getInt("startPercentage"),
                config.getString("javaCommand"),
                config.getStringList("customJvmFlags"),
                properties
        );
    }
}
