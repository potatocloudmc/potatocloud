package net.potatocloud.node.screen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.console.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@RequiredArgsConstructor
public class ScreenManager {

    private final Console console;
    private final Logger logger;

    private final Map<String, Screen> screens = new ConcurrentHashMap<>();
    private Screen currentScreen;

    public void addScreen(Screen screen) {
        screens.put(screen.name(), screen);
    }

    public void removeScreen(Screen screen) {
        if (screen != null) {
            screens.remove(screen.name());
        }
    }

    public Screen screen(String name) {
        return screens.get(name);
    }

    public void switchTo(String screenName) {
        switchTo(screenName, true);
    }

    public void switchTo(String name, boolean updatePrompt) {
        final Screen screen = screens.get(name);

        if (screen == null) {
            return;
        }

        this.currentScreen = screen;

        console.clearScreen();

        if (screen.name().equals(Screen.NODE_SCREEN)) {
            // Get cached logs directly from the logger for the node screen and print them
            logger.getCachedLogs().stream()
                    .filter(log -> !log.toLowerCase().contains("service screen")) // Remove service screen commands from the logs
                    .forEach(console::println);

            console.setPrompt(console.defaultPrompt());
            return;
        }

        if (updatePrompt) {
            console.setPrompt("[" + screen.name() + "] ");
        }

        screen.cachedLogs().forEach(console::println);
    }
}
