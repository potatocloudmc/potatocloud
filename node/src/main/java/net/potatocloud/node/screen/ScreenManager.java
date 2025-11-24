package net.potatocloud.node.screen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.console.Logger;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class ScreenManager {

    private final Console console;
    private final Logger logger;

    private Screen currentScreen = null;
    private final List<Screen> screens = new ArrayList<>();

    public void switchScreen(String screenName, boolean updatePrompt) {
        final Screen screen = getScreen(screenName);
        if (screen == null) {
            return;
        }

        currentScreen = screen;

        console.clearScreen();

        if (screen.getName().equals(Screen.NODE_SCREEN)) {
            // Get cached logs directly from the logger for the node screen and print them
            logger.getCachedLogs().stream()
                    .filter(log -> !log.toLowerCase().contains("service screen")) // Remove service screen commands from the logs
                    .forEach(console::println);
            console.setPrompt(console.getDefaultPrompt());
            return;
        }

        if (updatePrompt) {
            console.setPrompt("[" + screen.getName() + "] ");
        }

        screen.getCachedLogs().forEach(console::println);
    }

    public void switchScreen(String screenName) {
        switchScreen(screenName, true);
    }

    public Screen getScreen(String screenName) {
        return screens.stream()
                .filter(screen -> screen.getName().equalsIgnoreCase(screenName))
                .findFirst()
                .orElse(null);
    }

    public void addScreen(Screen screen) {
        screens.add(screen);
    }

    public void removeScreen(Screen screen) {
        screens.remove(screen);
    }
}
