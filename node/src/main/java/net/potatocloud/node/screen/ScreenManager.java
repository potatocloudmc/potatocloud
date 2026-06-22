package net.potatocloud.node.screen;

import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.packet.packets.service.ServiceScreenLogPacket;
import net.potatocloud.network.packet.packets.service.ServiceScreenSubscribePacket;
import net.potatocloud.network.packet.packets.service.ServiceScreenUnsubscribePacket;
import net.potatocloud.node.console.Console;
import net.potatocloud.node.screen.impl.NodeScreen;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScreenManager {

    private final Console console;

    private Screen current;
    private final Map<String, Screen> screens = new HashMap<>();
    private final Map<NetworkConnection, Map<String, ScreenSubscriber>> networkSubscriptions = new ConcurrentHashMap<>();

    public ScreenManager(Console console) {
        this.console = console;
    }

    public void init(NetworkServer server) {
        server.on(ServiceScreenSubscribePacket.class, ctx -> {
            final String serviceName = ctx.packet().serviceName();
            final Screen screen = screens.get(serviceName);

            if (screen == null) {
                return;
            }

            final NetworkConnection connection = ctx.connection();
            final ScreenSubscriber subscriber = line -> connection.send(new ServiceScreenLogPacket(serviceName, line));
            final Map<String, ScreenSubscriber> subscribers = networkSubscriptions.computeIfAbsent(connection, _ -> new ConcurrentHashMap<>());

            subscribers.put(serviceName, subscriber);

            screen.subscribe(subscriber);

            for (String log : screen.logs()) {
                connection.send(new ServiceScreenLogPacket(serviceName, log));
            }
        });

        server.on(ServiceScreenUnsubscribePacket.class, ctx -> {
            final String serviceName = ctx.packet().serviceName();
            final NetworkConnection connection = ctx.connection();

            Map<String, ScreenSubscriber> subscribers = networkSubscriptions.get(connection);
            if (subscribers == null) {
                return;
            }

            final ScreenSubscriber subscriber = subscribers.remove(serviceName);
            if (subscriber == null) {
                return;
            }

            if (subscribers.isEmpty()) {
                networkSubscriptions.remove(connection);
            }

            final Screen screen = screens.get(serviceName);

            if (screen != null) {
                screen.unsubscribe(subscriber);
            }
        });

        server.on(ServiceScreenLogPacket.class, ctx -> {
            final Screen screen = screens.get(ctx.packet().serviceName());

            if (screen == null) {
                return;
            }

            screen.append(ctx.packet().line());
        });
    }


    public void register(Screen screen) {
        screens.put(screen.name(), screen);

        if (!screen.name().equals(NodeScreen.NODE_SCREEN_NAME)) {
            screen.subscribe(line -> {
                if (screen == current) {
                    console.println(line);
                }
            });
        }
    }

    public void unregister(String name) {
        final Screen screen = screens.remove(name);
        if (screen == null) {
            return;
        }

        if (current != null && current.name().equals(name)) {
            current = null;
        }
    }

    public void open(Screen screen) {
        if (screen == null) {
            return;
        }

        if (current != null) {
            current.close();
        }

        console.clearScreen();

        current = screen;
        screen.open();
    }

    public void open(String name) {
        open(screens.get(name));
    }

    public void close() {
        if (current != null) {
            current.close();
            current = null;
        }
    }

    @SuppressWarnings("unused")
    public Map<String, Screen> screens() {
        return screens;
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
