package net.potatocloud.node.logging;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.node.config.NodeConfig;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.screen.ScreenType;

import java.nio.file.Path;

public final class NodeLogger implements Logger {

    private final NodeConfig config;
    private final Console console;
    private final ScreenManager screenManager;
    private final LogFile logFile;

    public NodeLogger(NodeConfig config, Console console, ScreenManager screenManager, Path logsDirectory, Path archiveDirectory) {
        this.config = config;
        this.console = console;
        this.screenManager = screenManager;
        this.logFile = new LogFile(logsDirectory, new LogArchiver(archiveDirectory, config.deleteLogsAfterDays()));
    }

    @Override
    public void log(Level level, String message) {
        if (level == Level.DEBUG && !config.debug()) {
            return;
        }

        final boolean commandInput = level == Level.COMMAND_INPUT;
        final String line = commandInput ? console.prompt() + message : LogFormatter.format(level, message);

        logFile.write(LogFormatter.stripColors(line));

        if (!commandInput && nodeScreenVisible()) {
            console.println(line);
        }

        screenManager.append(Screen.NODE_SCREEN_NAME, line);
    }

    public void logCommand(String command) {
        log(Level.COMMAND_INPUT, command);
    }

    private boolean nodeScreenVisible() {
        final Screen current = screenManager.current();

        return current == null || current.type() == ScreenType.NODE;
    }
}
