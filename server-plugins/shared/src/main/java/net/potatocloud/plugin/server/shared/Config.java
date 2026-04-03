package net.potatocloud.plugin.server.shared;

import net.potatocloud.common.ResourceFileUtils;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private final String fileName;
    private final Path filePath;
    private YamlFile yaml;

    public Config(String folder, String fileName) {
        this.fileName = fileName;
        this.filePath = Path.of(folder).resolve(fileName);
    }

    public void load() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            if (Files.notExists(filePath)) {
                ResourceFileUtils.copyResourceFile(fileName, filePath);
            }

            yaml = new YamlFile(filePath.toFile());
            yaml.loadWithComments();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config: " + fileName, e);
        }
    }

    public void save() {
        if (yaml == null) {
            return;
        }
        try {
            yaml.save();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config: " + fileName, e);
        }
    }

    public void reload() {
        load();
    }

    public YamlFile yaml() {
        return yaml;
    }
}
