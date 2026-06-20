package net.potatocloud.node.console;

import net.potatocloud.node.Node;
import net.potatocloud.node.command.CommandManager;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenManager;
import net.potatocloud.node.screen.impl.NodeScreen;
import net.potatocloud.node.setup.Setup;
import org.jline.jansi.Ansi;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;

public class ConsoleReader extends Thread {

    private final Console console;
    private final CommandManager commandManager;

    public ConsoleReader(Console console, CommandManager commandManager) {
        this.console = console;
        this.commandManager = commandManager;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {

                final Node node = Node.instance();

                if (!node.ready()) {
                    continue;
                }

                final String input = console.getLineReader().readLine(console.getPrompt());

                final ScreenManager screenManager = node.screenManager();
                final Screen currentScreen = screenManager.current();
                final boolean isNodeScreen = currentScreen.name().equals(NodeScreen.NODE_SCREEN_NAME);

                if (isNodeScreen && input.isBlank()) {
                    console.println(Ansi.ansi().cursorUpLine().eraseLine().cursorUp(1).toString());
                    continue;
                }

                if (isNodeScreen) {
                    node.logger().logCommand(input);

                    commandManager.executeCommand(input);
                    continue;
                }

                if (currentScreen.name().contains("setup")) {
                    final Setup currentSetup = node.setupManager().getCurrentSetup();
                    if (currentSetup != null) {
                        currentSetup.handleInput(input);
                    }
                    continue;
                }

                if (input.strip().equalsIgnoreCase("leave") || input.strip().equalsIgnoreCase("exit")) {
                    screenManager.open(screenManager.get(NodeScreen.NODE_SCREEN_NAME));
                    continue;
                }

                node.serviceManager().find(currentScreen.name()).ifPresent(service -> {
                    node.serviceManager().execute(service, input);
                });

            }
        } catch (UserInterruptException e) {
            Node.instance().shutdown();
        } catch (EndOfFileException e) {
            console.clearScreen();
            console.updateScreen();
        }
    }
}