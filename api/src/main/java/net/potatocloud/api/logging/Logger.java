package net.potatocloud.api.logging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface Logger {

    /**
     * Logs an informational message.
     *
     * @param message the message to log
     */
    default void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs a warning message.
     *
     * @param message the message to log
     */
    default void warn(String message) {
        log(Level.WARN, message);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     */
    default void error(String message) {
        log(Level.ERROR, message);
    }

    default void exception(Throwable throwable) {
        log(Level.ERROR, "An exception occurred: " + throwable.getMessage());
        for (StackTraceElement element : throwable.getStackTrace()) {
            log(Level.ERROR, "    at " + element.toString());
        }
    }

    /**
     * Logs a debug message.
     * Debug messages are only shown when debug mode is enabled.
     *
     * @param message the message to log
     */
    default void debug(String message) {
        log(Level.DEBUG, message);
    }

    /**
     * Logs a message with a specific log level.
     *
     * @param level   the severity level of the log message
     * @param message the message to log
     */
    void log(Level level, String message);

    /**
     * Represents the severity level of a log message.
     */
    @Getter
    @RequiredArgsConstructor
    enum Level {

        /**
         * Informational messages for normal operations.
         */
        INFO("&a"),

        /**
         * Warning messages indicating potential issues.
         */
        WARN("&e"),

        /**
         * Error messages indicating failures or critical issues.
         */
        ERROR("&c"),

        /**
         * Debug messages used for development purposes.
         */
        DEBUG("&e"),

        /**
         * Command input entered by a user in the console
         */
        COMMAND_INPUT("&7");

        /**
         * Color code associated with this log level.
         */
        private final String colorCode;
    }
}