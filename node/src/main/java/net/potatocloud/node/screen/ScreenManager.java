package net.potatocloud.node.screen;

import net.potatocloud.network.NetworkServer;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.console.Console;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ScreenManager {

    private static final int MAX_LOGS = 1000;

    private final Console console;
    private final Map<String, Screen> screens = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<String>> logs = new ConcurrentHashMap<>();

    private volatile Screen current;
    private ScreenNetworkHandler networkHandler;

    public ScreenManager(Console console) {
        this.console = console;
    }

    public void init(NetworkServer server, ClusterManagerImpl clusterManager) {
        networkHandler = new ScreenNetworkHandler(this, server, clusterManager);
    }

    public void register(Screen screen) {
        logs.putIfAbsent(screen.name(), new CopyOnWriteArrayList<>());
        screens.put(screen.name(), screen);
    }

    public void unregister(String name) {
        final Screen screen = screens.remove(name);
        if (screen == null) {
            return;
        }

        if (screen.equals(current)) {
            open(Screen.NODE_SCREEN_NAME);
        }

        logs.remove(name);
    }

    public void open(Screen screen) {
        if (screen == null || screen == current) {
            return;
        }

        changeScreen(screen);
    }

    public void open(String name) {
        open(screens.get(name));
    }

    public void close() {
        changeScreen(null);
    }

    private synchronized void changeScreen(Screen screen) {
        if (screen == current) {
            return;
        }

        if (networkHandler != null && current != null) {
            networkHandler.screenClosed(current);
        }

        current = screen;
        if (screen == null) {
            return;
        }

        console.clearScreen();

        if (networkHandler != null) {
            networkHandler.screenOpened(screen);
        }

        if (!screen.isRemote()) {
            for (String line : logs(screen.name())) {
                if (screen.type() != ScreenType.NODE || !line.toLowerCase().contains("service screen")) {
                    console.println(line);
                }
            }
        }

        console.prompt(screen.prompt());
    }

    public void append(String screenName, String line) {
        final List<String> screenLogs = logs.get(screenName);
        if (screenLogs == null) {
            return;
        }

        screenLogs.add(line);
        if (screenLogs.size() > MAX_LOGS) {
            screenLogs.removeFirst();
        }

        final Screen activeScreen = current;
        if (activeScreen != null
                && activeScreen.type() != ScreenType.NODE
                && activeScreen.name().equals(screenName)) {
            console.println(line);
        }

        if (networkHandler != null) {
            networkHandler.sendLog(screenName, line);
        }
    }

    public List<String> logs(String screenName) {
        final List<String> screenLogs = logs.get(screenName);
        if (screenLogs == null) {
            return List.of();
        }

        return Collections.unmodifiableList(screenLogs);
    }

    public Map<String, Screen> screens() {
        return Collections.unmodifiableMap(screens);
    }

    public Screen get(String name) {
        return screens.get(name);
    }

    public Screen current() {
        return current;
    }

    public void current(Screen screen) {
        this.current = screen;
    }
}
