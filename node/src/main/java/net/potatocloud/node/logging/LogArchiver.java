package net.potatocloud.node.logging;

import net.potatocloud.common.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class LogArchiver {

    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.systemDefault());

    private final Path directory;
    private final int retentionDays;

    public LogArchiver(Path directory, int retentionDays) {
        this.directory = directory;
        this.retentionDays = retentionDays;
    }

    public void archive(Path logFile) {
        if (Files.notExists(logFile)) {
            return;
        }

        try {
            Files.createDirectories(directory);

            final String name = NAME_FORMAT.format(Files.getLastModifiedTime(logFile).toInstant());

            FileUtils.gzip(logFile, directory.resolve(name + ".log.gz"));
            Files.delete(logFile);

            deleteExpired();
        } catch (IOException e) {
            throw new RuntimeException("Failed to archive log file: " + logFile, e);
        }
    }

    private void deleteExpired() throws IOException {
        if (retentionDays < 1) {
            return;
        }

        final Instant expiry = Instant.now().minus(Duration.ofDays(retentionDays));

        for (Path archivePath : FileUtils.list(directory)) {
            if (Files.getLastModifiedTime(archivePath).toInstant().isBefore(expiry)) {
                Files.delete(archivePath);
            }
        }
    }
}
