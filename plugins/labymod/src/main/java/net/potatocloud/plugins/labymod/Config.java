package net.potatocloud.plugins.labymod;

import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class Config {

    private final File configFile;
    private YamlConfiguration config;

    public Config() {
        this.configFile = new File("plugins/potatocloud-labymod", "config.yml");
    }

    @SneakyThrows
    public void load() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();

            try (InputStream defaultConfigStream = LabyModPlugin.class.getClassLoader().getResourceAsStream("config.yml")) {
                if (defaultConfigStream != null) {
                    Files.copy(defaultConfigStream, configFile.toPath());
                }
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String notifyMessage() {
        return this.config.getString("notify-message");
    }
}
