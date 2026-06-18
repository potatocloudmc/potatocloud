package net.potatocloud.webinterface.service.broadcast.group;

import io.javalin.websocket.WsContext;
import lombok.RequiredArgsConstructor;
import net.potatocloud.webinterface.dto.event.WsEventDto;
import net.potatocloud.webinterface.dto.group.GroupDto;
import net.potatocloud.webinterface.service.GroupService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class GroupDetailsBroadcastService {

    private final GroupService groupService;
    private final int updateIntervalSeconds;

    private final Map<String, Set<WsContext>> subscriptions = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "PotatoCloud-GroupDetails-Broadcast");
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(this::broadcastTick, 0, updateIntervalSeconds, TimeUnit.SECONDS);
    }

    public void register(String groupName, WsContext context) {
        subscriptions.computeIfAbsent(groupName, key -> ConcurrentHashMap.newKeySet()).add(context);
    }

    public void unregister(WsContext context) {
        subscriptions.values().forEach(set -> set.remove(context));
        subscriptions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        subscriptions.clear();
    }

    private void broadcastTick() {
        subscriptions.forEach((groupName, contexts) -> {
            GroupDto payload = groupService.getGroupByName(groupName);

            for (WsContext context : contexts) {
                if (!context.session.isOpen()) {
                    continue;
                }

                try {
                    if (payload == null) {
                        context.send(WsEventDto.builder().type("error").data("Group not found: " + groupName).build());
                        continue;
                    }

                    context.send(WsEventDto.builder().type("group_details").data(payload).build());
                } catch (Exception ignored) {
                }
            }
        });
    }
}

