package net.potatocloud.plugins.notify;

import lombok.SneakyThrows;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class Config {

    private final File configFile;
    private YamlFile config;

    public Config() {
        this.configFile = new File("plugins/potatocloud-notify", "config.yml");
    }

    @SneakyThrows
    public void load() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();

            try (InputStream defaultConfigStream = NotifyPlugin.class.getClassLoader().getResourceAsStream("config.yml")) {
                if (defaultConfigStream != null) {
                    Files.copy(defaultConfigStream, configFile.toPath());
                }
            }
        }

        config = YamlFile.loadConfiguration(configFile);
        config.load();
    }

    public String getPermission() {
        return config.getString("permission");
    }

    public boolean enableStarting() {
        return config.getBoolean("messages.enable-service-starting");
    }

    public boolean enableStopping() {
        return config.getBoolean("messages.enable-service-stopping");
    }
}
