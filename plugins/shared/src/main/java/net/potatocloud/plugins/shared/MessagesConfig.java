package net.potatocloud.plugins.shared;

import net.kyori.adventure.text.Component;
import net.potatocloud.common.config.Config;
import net.potatocloud.common.config.yaml.YamlConfig;

import java.nio.file.Path;

public class MessagesConfig {

    private final Config config;

    public MessagesConfig(String directory) {
        this.config = new YamlConfig(Path.of(directory).resolve("messages.yml"));
    }

    public void load() {
        config.load();
    }

    public void reload() {
        config.reload();
    }

    public Component get(String key) {
        return get(key, true);
    }

    public Component get(String path, boolean withPrefix) {
        final String prefix = withPrefix ? config.get("prefix").asString() : "";
        final String message = config.get(path).asString();
        return MessageUtils.format(prefix + message);
    }

    public Component get(String key, String... placeholders) {
        Component message = get(key);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            final String placeholder = placeholders[i];
            final String value = placeholders[i + 1];
            message = message.replaceText(text -> text.matchLiteral(placeholder).replacement(value));
        }
        return message;
    }
}
