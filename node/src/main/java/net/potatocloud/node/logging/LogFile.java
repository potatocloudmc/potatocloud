package net.potatocloud.node.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

public final class LogFile {

    private static final String FILE_NAME = "latest.log";

    private final Path path;
    private final LogArchiver archiver;

    private LocalDate date;

    public LogFile(Path directory, LogArchiver archiver) {
        this.path = directory.resolve(FILE_NAME);
        this.archiver = archiver;
        this.date = LocalDate.now();

        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create logs directory: " + directory, e);
        }

        archiver.archive(path);
    }

    public synchronized void write(String line) {
        final LocalDate today = LocalDate.now();

        if (!today.equals(date)) {
            archiver.archive(path);
            date = today;
        }

        try {
            Files.writeString(
                    path,
                    line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to log file: " + path, e);
        }
    }
}
