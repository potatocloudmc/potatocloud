package net.potatocloud.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ResourceFileUtils {

    private ResourceFileUtils() {
    }

    public static void copyResourceFile(String resourceName, Path targetPath) {
        try (InputStream stream = ResourceFileUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new IllegalStateException(resourceName + " not found in resources!");
            }
            Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy " + resourceName + " from resources!", e);
        }
    }
}
