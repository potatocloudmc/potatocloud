package net.potatocloud.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class FileUtils {

    private FileUtils() {
    }

    public static void createHiddenFile(Path path) throws IOException {
        Files.createFile(path);
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            Files.setAttribute(path, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
        }
    }

    public static void deleteDirectory(Path directory) {
        if (Files.notExists(directory)) {
            return;
        }

        list(directory, true).stream()
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete file: " + path, e);
                    }
                });
    }

    public static void copyDirectory(Path source, Path target) {
        if (Files.notExists(source)) {
            throw new RuntimeException("Source directory does not exist: " + source);
        }

        for (Path sourcePath : list(source, true)) {
            try {
                final Path targetPath = target.resolve(source.relativize(sourcePath));

                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy " + sourcePath + " to " + target, e);
            }
        }
    }

    public static void downloadFile(String url, Path targetPath) {
        try {
            final Path parent = targetPath.getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }

            try (InputStream in = URI.create(url).toURL().openStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file from URL: " + url, e);
        }
    }

    public static void unzip(Path archive, Path targetDirectory) {
        final Path normalizedTargetDirectory = targetDirectory.toAbsolutePath().normalize();

        try (ZipInputStream input = new ZipInputStream(Files.newInputStream(archive))) {
            Files.createDirectories(normalizedTargetDirectory);

            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                final Path destination = normalizedTargetDirectory.resolve(entry.getName()).normalize();

                if (!destination.startsWith(normalizedTargetDirectory)) {
                    throw new IllegalStateException("Zip entry points outside the target directory: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(destination);
                } else {
                    final Path parentDirectory = destination.getParent();
                    if (parentDirectory != null) {
                        Files.createDirectories(parentDirectory);
                    }

                    Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
                }

                input.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to unzip " + archive + " to " + targetDirectory, e);
        }
    }

    public static List<Path> list(Path directory) {
        return list(directory, false);
    }

    public static List<Path> list(Path directory, boolean recursive) {
        if (Files.notExists(directory)) {
            throw new RuntimeException("Directory does not exist: " + directory);
        }

        try (Stream<Path> stream = recursive ? Files.walk(directory) : Files.list(directory)) {
            return stream.toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to list directory: " + directory, e);
        }
    }

    public static void replaceInFile(Path path, String target, String replacement) {
        try {
            final String content = Files.readString(path, StandardCharsets.UTF_8);
            if (!content.contains(target)) {
                return;
            }

            Files.writeString(
                    path,
                    content.replace(target, replacement),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to update " + path.getFileName(), e);
        }
    }
}
