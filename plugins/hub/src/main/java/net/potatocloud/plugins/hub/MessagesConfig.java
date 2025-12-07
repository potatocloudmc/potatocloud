package net.potatocloud.plugins.hub;

import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class MessagesConfig {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final File configFile;
    private YamlFile config;

    public MessagesConfig() {
        this.configFile = new File("plugins/potatocloud-hub", "messages.yml");
    }

    @SneakyThrows
    public void load() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();

            try (InputStream defaultConfigStream = HubPlugin.class.getClassLoader().getResourceAsStream("messages.yml")) {
                if (defaultConfigStream != null) {
                    Files.copy(defaultConfigStream, configFile.toPath());
                }
            }
        }

        config = YamlFile.loadConfiguration(configFile);
        config.load();
    }

    public Component get(String key) {
        return miniMessage.deserialize(this.config.getString("prefix") + config.getString(key));
    }
}
