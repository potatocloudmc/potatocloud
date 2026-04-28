package net.potatocloud.node.console;

import lombok.Getter;
import net.potatocloud.node.command.CommandManager;
import net.potatocloud.node.config.NodeConfig;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.nio.charset.StandardCharsets;

@Getter
public class Console {

    private final NodeConfig config;

    private final Terminal terminal;
    private final LineReader lineReader;
    private final ConsoleReader consoleReader;

    private String prompt;

    public Console(NodeConfig config, CommandManager commandManager) {
        this.config = config;

        try {
            this.terminal = TerminalBuilder.builder()
                    .name("potatocloud-console")
                    .system(true)
                    .encoding(StandardCharsets.UTF_8)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize terminal", e);
        }

        this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new ConsoleCompleter(commandManager))
                .build();

        this.prompt = defaultPrompt();

        this.consoleReader = new ConsoleReader(this, commandManager);
    }

    public void start() {
        clearScreen();

        consoleReader.start();
    }

    public void println(String message) {
        lineReader.printAbove(ConsoleColor.format(message));
    }

    public String defaultPrompt() {
        final String rawPrompt = config.getPrompt();

        return ConsoleColor.format(rawPrompt.replace("%user%", System.getProperty("user.name")));
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
        if (lineReader instanceof LineReaderImpl impl) {
            impl.setPrompt(prompt);
        }
    }

    public void clearScreen() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        updateScreen();
    }

    public void updateScreen() {
        terminal.flush();
        if (lineReader.isReading()) {
            lineReader.callWidget(LineReader.REDRAW_LINE);
            lineReader.callWidget(LineReader.REDISPLAY);
        }
    }

    public void close() {
        consoleReader.interrupt();
        try {
            terminal.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close terminal", e);
        }
    }
}