package net.potatocloud.node.screen;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Screen {

    public static final String NODE_SCREEN = "node_screen";

    private final String name;
    private final List<String> cachedLogs;

    public Screen(String name) {
        this.name = name;
        this.cachedLogs = new CopyOnWriteArrayList<>();
    }

    public String name() {
        return name;
    }

    public List<String> cachedLogs() {
        return Collections.unmodifiableList(cachedLogs);
    }

    public void addLog(String log) {
        synchronized (cachedLogs) {
            if (cachedLogs.size() >= 1000) {
                cachedLogs.remove(0);
            }
            cachedLogs.add(log);
        }
    }
}
