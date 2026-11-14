package net.potatocloud.node.screen;

import net.potatocloud.api.service.LogListener;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.node.cluster.ClusterManagerImpl;
import net.potatocloud.node.console.Console;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public final class ScreenManager {

    private static final int MAX_LOGS = 1000;

    private final Console console;
    private final Map<String, Screen> screens = new ConcurrentHashMap<>();
    private final Map<String, List<String>> logs = new ConcurrentHashMap<>();
    private final Map<String, Set<LogListener>> listeners = new ConcurrentHashMap<>();

    private volatile Screen current;
    private ScreenNetworkHandler network;

    public ScreenManager(Console console) {
        this.console = console;
    }

    public void init(NetworkServer server, ClusterManagerImpl clusterManager) {
        network = new ScreenNetworkHandler(this, server, clusterManager);
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
        listeners.remove(name);
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
        final Screen previous = current;
        if (screen == previous) {
            return;
        }

        if (network != null && previous != null && !hasListeners(previous.name())) {
            network.screenClosed(previous);
        }

        current = screen;
        if (screen == null) {
            return;
        }

        console.clearScreen();

        if (network != null) {
            network.screenOpened(screen);
        }

        logs(screen.name()).thenAccept(lines -> print(screen, lines));
        console.prompt(screen.prompt());
    }

    private void print(Screen screen, List<String> lines) {
        if (!screen.equals(current)) {
            return;
        }

        for (String line : lines) {
            if (screen.type() != ScreenType.NODE || !line.toLowerCase().contains("service screen")) {
                console.println(line);
            }
        }
    }

    public void append(String name, String line) {
        final List<String> screenLogs = logs.get(name);
        if (screenLogs == null) {
            return;
        }

        screenLogs.add(line);
        if (screenLogs.size() > MAX_LOGS) {
            screenLogs.removeFirst();
        }

        final Screen screen = current;
        if (screen != null && screen.type() != ScreenType.NODE && screen.name().equals(name)) {
            console.println(line);
        }

        if (network != null) {
            network.sendLog(name, line);
        }

        for (LogListener listener : listeners.getOrDefault(name, Set.of())) {
            listener.onLog(line);
        }
    }

    public CompletableFuture<List<String>> logs(String name) {
        final Screen screen = screens.get(name);
        if (screen == null) {
            return CompletableFuture.completedFuture(List.of());
        }

        if (screen.isRemote()) {
            return network.requestLogs(screen);
        }

        return CompletableFuture.completedFuture(List.copyOf(logs.getOrDefault(name, List.of())));
    }

    public void addListener(String name, LogListener listener) {
        final Screen screen = screens.get(name);
        if (screen == null) {
            return;
        }

        listeners.computeIfAbsent(name, key -> new CopyOnWriteArraySet<>()).add(listener);
        network.screenOpened(screen);
    }

    public void removeListener(String name, LogListener listener) {
        final Screen screen = screens.get(name);
        if (screen == null || !hasListeners(name)) {
            return;
        }

        listeners.get(name).remove(listener);

        if (!hasListeners(name) && !screen.equals(current)) {
            network.screenClosed(screen);
        }
    }

    private boolean hasListeners(String name) {
        final Set<LogListener> screenListeners = listeners.get(name);
        return screenListeners != null && !screenListeners.isEmpty();
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
