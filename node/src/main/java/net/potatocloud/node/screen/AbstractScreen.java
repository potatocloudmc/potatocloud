package net.potatocloud.node.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractScreen implements Screen {

    private final List<String> logs = new ArrayList<>();
    private final Set<ScreenSubscriber> subscribers = ConcurrentHashMap.newKeySet();

    @Override
    public List<String> logs() {
        return List.copyOf(logs);
    }

    @Override
    public void append(String line) {
        if (logs.size() >= ScreenConstants.MAX_LOGS) {
            logs.removeFirst();
        }

        logs.add(line);

        for (ScreenSubscriber subscriber : subscribers) {
            subscriber.handle(line);
        }
    }

    @Override
    public void subscribe(ScreenSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(ScreenSubscriber subscriber) {
        subscribers.remove(subscriber);
    }
}
