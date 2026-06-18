package net.potatocloud.webinterface.service.broadcast;

import io.javalin.websocket.WsContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.potatocloud.webinterface.dto.event.WsEventDto;
import net.potatocloud.webinterface.dto.service.ServerOverviewDto;
import net.potatocloud.webinterface.service.ServerService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class ServiceDetailsBroadcastService {

    private final ServerService serverService;
    private final int updateIntervalSeconds;

    private final Map<String, Set<WsContext>> subscriptions = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "PotatoCloud-ServiceDetails-Broadcast");
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(this::broadcastTick, 0, updateIntervalSeconds, TimeUnit.SECONDS);
    }

    public void register(String serviceName, WsContext context) {
        subscriptions.computeIfAbsent(serviceName, key -> ConcurrentHashMap.newKeySet()).add(context);
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
        subscriptions.forEach((serviceName, contexts) -> {
            ServerOverviewDto payload = serverService.getService(serviceName);

            for (WsContext context : contexts) {
                if (!context.session.isOpen()) {
                    continue;
                }

                try {
                    if (payload == null) {
                        context.send(WsEventDto.builder().type("error").data("Service not found: " + serviceName).build());
                        continue;
                    }

                    context.send(WsEventDto.builder().type("service_update").data(payload).build());
                } catch (Exception ignored) {
                }
            }
        });
    }

}
