package net.potatocloud.node.logging;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.screen.ScreenType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public final class NodeLogger implements Logger {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Pattern COLOR_PATTERN = Pattern.compile("(&.)|\u001B\\[[;\\d]*m");

    private static final String LATEST_LOG = "latest.log";

    private final NodeConfig config;
    private final Console console;
    private final ScreenManager screenManager;
    private final Path logsDirectory;

    public NodeLogger(NodeConfig config, Console console, ScreenManager screenManager, Path logsDirectory) {
        this.config = config;
        this.console = console;
        this.screenManager = screenManager;
        this.logsDirectory = logsDirectory;

        try {
            if (Files.notExists(logsDirectory)) {
                Files.createDirectories(logsDirectory);
            }

            Files.deleteIfExists(logsDirectory.resolve(LATEST_LOG));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize NodeLogger", e);
        }
    }

    @Override
    public void log(Level level, String message) {
        if (level == Level.DEBUG && !config.debug()) {
            return;
        }

        final LocalDateTime now = LocalDateTime.now();
        final String time = TIME_FORMAT.format(now);
        final String date = DATE_FORMAT.format(now);

        String raw;
        String colored;

        if (level == Level.COMMAND_INPUT) {
            colored = console.prompt() + message;
            raw = stripColors(colored);
        } else {
            raw = formatRaw(level, time, message);
            colored = formatColored(level, time, message);
        }

        final Path dayLogPath = logsDirectory.resolve(date + ".log");
        final Path latestLogPath = logsDirectory.resolve(LATEST_LOG);

        appendLine(dayLogPath, raw);
        appendLine(latestLogPath, raw);

        if (screenManager.current() != null) {
            final boolean nodeScreen = screenManager.current().type() == ScreenType.NODE;

            if (!level.equals(Level.COMMAND_INPUT) && nodeScreen) {
                console.println(colored);
            }
        } else {
            console.println(colored);
        }

        screenManager.append(Screen.NODE_SCREEN_NAME, colored);
    }

    private String formatRaw(Level level, String time, String message) {
        return "[" + time + " " + level.name() + "] " + stripColors(message);
    }

    private String formatColored(Level level, String time, String message) {
        return "&8[&7" + time + " " + level.colorCode() + level.name() + "&8] &7" + message;
    }

    private String stripColors(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }

    private void appendLine(Path path, String line) {
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

    public void logCommand(String command) {
        log(Level.COMMAND_INPUT, command);
    }
}
