package net.potatocloud.common.config.yaml;

import net.potatocloud.common.config.AbstractConfig;
import net.potatocloud.common.config.ConfigNode;
import net.potatocloud.common.config.JacksonConfigNode;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.nio.file.Path;

public class YamlConfig extends AbstractConfig {

    public YamlConfig(Path path) {
        super(new YamlSource(), path);
    }

    public YamlConfig(Path path, ClassLoader classLoader) {
        super(new YamlSource(), path, classLoader);
    }

    @Override
    public ConfigNode get(String path) {
        return new JacksonConfigNode(resolve(path));
    }

    @Override
    public void set(String path, Object value) {
        if (!(node instanceof ObjectNode root)) {
            return;
        }

        final String[] keys = path.split("\\.");
        ObjectNode current = root;

        for (int i = 0; i < keys.length - 1; i++) {
            JsonNode child = current.get(keys[i]);
            if (child == null || !child.isObject()) {
                child = current.putObject(keys[i]);
            }
            current = (ObjectNode) child;
        }

        final String lastKey = keys[keys.length - 1];
        current.putPOJO(lastKey, value);
    }

    @Override
    public void remove(String path) {
        final int lastDot = path.lastIndexOf('.');
        final JsonNode parent = lastDot == -1 ? node : resolve(path.substring(0, lastDot));

        if (parent instanceof ObjectNode objectNode) {
            objectNode.remove(path.substring(lastDot + 1));
        }
    }

    private JsonNode resolve(String path) {
        if (this.node == null) {
            return null;
        }

        JsonNode node = this.node;
        for (String key : path.split("\\.")) {
            if (!node.isObject()) {
                return null;
            }

            node = node.get(key);

            if (node == null) {
                return null;
            }
        }
        return node;
    }
}
