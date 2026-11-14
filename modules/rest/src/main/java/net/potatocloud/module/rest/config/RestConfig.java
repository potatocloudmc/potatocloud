package net.potatocloud.module.rest.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.potatocloud.common.JacksonUtils;
import net.potatocloud.common.ResourceFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public record RestConfig(
        String host,
        int port,
        @JsonProperty("cors-origins") List<String> corsOrigins,
        boolean openapi,
        List<ApiKey> keys
) {

    public ApiKey findKey(String token) {
        return keys.stream().filter(key -> key.key().equals(token)).findFirst().orElse(null);
    }

    public static RestConfig load(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            ResourceFileUtils.copyResourceFile(RestConfig.class.getClassLoader(), "config.yml", path);

            final String key = UUID.randomUUID().toString().replace("-", "");
            Files.writeString(path, Files.readString(path).replace("%key%", key));
        }
        return JacksonUtils.YAML_MAPPER.readValue(path.toFile(), RestConfig.class);
    }
}
