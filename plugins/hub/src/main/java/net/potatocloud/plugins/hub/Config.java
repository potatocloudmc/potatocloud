package net.potatocloud.plugins.hub;

import lombok.SneakyThrows;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class Config {

    private final File configFile;
    private YamlFile config;

    public Config() {
        this.configFile = new File("plugins/potatocloud-hub", "config.yml");
    }

    @SneakyThrows
    public void load() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();

            try (InputStream defaultConfigStream = HubPlugin.class.getClassLoader().getResourceAsStream("config.yml")) {
                if (defaultConfigStream != null) {
                    Files.copy(defaultConfigStream, configFile.toPath());
                }
            }
        }

        config = YamlFile.loadConfiguration(configFile);
        config.load();
    }

    public String[] aliases() {
        List<String> fromConfig = this.config.getStringList("aliases");
        return fromConfig.toArray(new String[0]);
    }

}
