package net.potatocloud.node.console;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.potatocloud.node.Node;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.screen.Screen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class Logger {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String LATEST_LOG_FILENAME = "latest.log";
    private static final Pattern COLOR_PATTERN = Pattern.compile("(&.)|\u001B\\[[;\\d]*m");

    private final NodeConfig config;
    private final Console console;
    private final Path logsDirectory;
    private final List<String> cachedLogs = new CopyOnWriteArrayList<>();

    public Logger(NodeConfig config, Console console, Path logsDirectory) {
        this.config = config;
        this.console = console;
        this.logsDirectory = logsDirectory;

        if (Files.notExists(logsDirectory)) {
            try {
                Files.createDirectories(logsDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create logs directory: " + logsDirectory, e);
            }
        }

        final Path latestLogPath = logsDirectory.resolve(LATEST_LOG_FILENAME);
        if (Files.exists(latestLogPath)) {
            try {
                Files.deleteIfExists(latestLogPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete " + LATEST_LOG_FILENAME, e);
            }
        }
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void warn(String message) {
        log(Level.WARN, message);
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    public void debug(String message) {
        if (config.isDebug()) {
            log(Level.DEBUG, message);
        }
    }

    public void logCommand(String command) {
        log(Level.COMMAND, command);
    }

    public List<String> getCachedLogs() {
        return Collections.unmodifiableList(cachedLogs);
    }

    private void log(Level level, String message) {
        final Date now = new Date();
        final String formattedTime = TIME_FORMAT.format(now);
        final String formattedDate = DATE_FORMAT.format(now);

        String uncoloredMessage;
        String coloredMessage;

        if (level.equals(Level.COMMAND)) {
            coloredMessage = console.getPrompt() + message;
            uncoloredMessage = removeColorCodes(coloredMessage);
        } else {
            uncoloredMessage = "[" + formattedTime + " " + level.name() + "] " + removeColorCodes(message);
            coloredMessage = "&8[&7" + formattedTime + " " + level.getColorCode() + level.name() + "&8] &7" + message;
        }

        final Path dayLogPath = logsDirectory.resolve(formattedDate + ".log");
        final Path latestLogPath = logsDirectory.resolve(LATEST_LOG_FILENAME);

        appendLine(dayLogPath, uncoloredMessage);
        appendLine(latestLogPath, uncoloredMessage);

        // Make sure the cached logs list wont get too big
        synchronized (cachedLogs) {
            if (cachedLogs.size() >= 1000) {
                cachedLogs.remove(0);
            }
        }

        cachedLogs.add(coloredMessage);

        final boolean isNodeScreen = Node.getInstance().getScreenManager().getCurrentScreen().name().equals(Screen.NODE_SCREEN);
        if (!level.equals(Level.COMMAND) && isNodeScreen) {
            console.println(coloredMessage);
        }
    }

    private String removeColorCodes(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }

    private void appendLine(Path path, String line) {
        if (Files.notExists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create log file: " + path, e);
            }
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

    @Getter
    @RequiredArgsConstructor
    public enum Level {
        INFO("&a"),
        WARN("&e"),
        ERROR("&c"),
        DEBUG("&e"),
        COMMAND("&7");

        private final String colorCode;

    }
}
