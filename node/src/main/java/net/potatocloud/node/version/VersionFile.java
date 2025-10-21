package net.potatocloud.node.version;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.potatocloud.api.CloudAPI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

@UtilityClass
public class VersionFile {

    public static Path VERSION_FILE = PathUtils.current().resolve(".version");

    @SneakyThrows
    public void create() {
        if (!Files.exists(VERSION_FILE)) {
            Files.createFile(VERSION_FILE);

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Files.setAttribute(VERSION_FILE, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
            }
        }

        FileUtils.writeStringToFile(VERSION_FILE.toFile(), CloudAPI.VERSION, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public String getVersion() {
        if (Files.exists(VERSION_FILE)) {
            return FileUtils.readFileToString(VERSION_FILE.toFile(), StandardCharsets.UTF_8).strip();
        }
        return null;
    }
}