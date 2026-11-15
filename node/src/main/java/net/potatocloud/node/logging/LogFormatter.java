package net.potatocloud.node.logging;

import net.potatocloud.api.logging.Logger;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public final class LogFormatter {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Pattern COLOR_PATTERN = Pattern.compile("(&.)|\u001B\\[[;\\d]*m");

    private LogFormatter() {
    }

    public static String format(Logger.Level level, String message) {
        final String time = TIME_FORMAT.format(LocalTime.now());

        return "&8[&7" + time + " " + level.colorCode() + level.name() + "&8] &7" + message;
    }

    public static String stripColors(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }
}
