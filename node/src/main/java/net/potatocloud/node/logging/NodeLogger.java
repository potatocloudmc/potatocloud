package net.potatocloud.node.logging;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.node.Node;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
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

public class NodeLogger implements Logger {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Pattern COLOR_PATTERN = Pattern.compile("(&.)|\u001B\\[[;\\d]*m");

    private static final String LATEST_LOG = "latest.log";
    private static final int MAX_CACHED_LOGS = 1000;

    private final NodeConfig config;
    private final Console console;
    private final Path logsDirectory;

    private final List<String> cache = new CopyOnWriteArrayList<>();

    public NodeLogger(NodeConfig config, Console console, Path logsDirectory) {
        this.config = config;
        this.console = console;
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
        if (level == Level.DEBUG && !config.isDebug()) {
            return;
        }

        final Date now = new Date();
        final String time = TIME_FORMAT.format(now);
        final String date = DATE_FORMAT.format(now);

        String raw;
        String colored;

        if (level == Level.COMMAND_INPUT) {
            colored = console.getPrompt() + message;
            raw = stripColors(colored);
        } else {
            raw = formatRaw(level, time, message);
            colored = formatColored(level, time, message);
        }

        final Path dayLogPath = logsDirectory.resolve(date + ".log");
        final Path latestLogPath = logsDirectory.resolve(LATEST_LOG);

        appendLine(dayLogPath, raw);
        appendLine(latestLogPath, raw);

        // Make sure the cached logs list will not get too big
        synchronized (cache) {
            if (cache.size() >= MAX_CACHED_LOGS) {
                cache.remove(0);
            }
        }

        cache.add(colored);

        final boolean nodeScreen = Node.getInstance()
                .getScreenManager()
                .getCurrentScreen()
                .name()
                .equals(Screen.NODE_SCREEN);

        if (!level.equals(Level.COMMAND_INPUT) && nodeScreen) {
            console.println(colored);
        }
    }

    private String formatRaw(Level level, String time, String message) {
        return "[" + time + " " + level.name() + "] " + stripColors(message);
    }

    private String formatColored(Level level, String time, String message) {
        return "&8[&7" + time + " " + level.getColorCode() + level.name() + "&8] &7" + message;
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

    public List<String> getCachedLogs() {
        return Collections.unmodifiableList(cache);
    }
}
