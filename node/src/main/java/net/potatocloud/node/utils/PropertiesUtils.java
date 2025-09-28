package net.potatocloud.node.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@UtilityClass
public class PropertiesUtils {

    @SneakyThrows
    public Properties loadProperties(Path file) {
        Properties properties = new Properties();
        if (Files.exists(file)) {
            try (FileInputStream in = new FileInputStream(file.toFile())) {
                properties.load(in);
            }
        }
        return properties;
    }

    @SneakyThrows
    public void saveProperties(Properties properties, Path file) {
        try (OutputStream out = Files.newOutputStream(file)) {
            properties.store(out, null);
        }
    }
}
