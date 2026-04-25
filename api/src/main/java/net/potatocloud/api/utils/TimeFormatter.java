package net.potatocloud.api.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TimeFormatter {

    private TimeFormatter() {
    }

    public static String formatAsDuration(long millis) {
        final Duration duration = Duration.ofMillis(millis);

        final long totalDays = duration.toDays();

        final long months = totalDays / 30;
        final long weeks = (totalDays % 30) / 7;
        final long days = totalDays % 7;

        final long hours = duration.toHours() % 24;
        final long minutes = duration.toMinutes() % 60;
        final long seconds = duration.getSeconds() % 60;

        final StringBuilder builder = new StringBuilder();
        if (months > 0) {
            builder.append(months).append("mo ");
        }
        if (weeks > 0) {
            builder.append(weeks).append("w ");
        }
        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        builder.append(seconds).append("s");

        return builder.toString().trim();
    }

    public static String formatAsDateAndTime(long millis) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(millis));
    }
}
