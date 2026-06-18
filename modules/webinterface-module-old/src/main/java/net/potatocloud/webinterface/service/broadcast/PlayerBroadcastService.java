package net.potatocloud.webinterface.service.broadcast;

import io.javalin.websocket.WsContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.events.player.CloudPlayerDisconnectEvent;
import net.potatocloud.api.event.events.player.CloudPlayerJoinEvent;
import net.potatocloud.webinterface.dto.event.WsEventDto;
import net.potatocloud.webinterface.dto.player.PlayerDto;
import net.potatocloud.webinterface.service.PlayerService;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class PlayerBroadcastService {

    private final CloudAPI cloudAPI;
    private final PlayerService playerService;
    private final int updateIntervalSeconds;

    private final Set<WsContext> subscribers = ConcurrentHashMap.newKeySet();
    private ScheduledExecutorService scheduler;

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "PotatoCloud-Player-Broadcast");
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(this::broadcastTick, 0, updateIntervalSeconds, TimeUnit.SECONDS);
    }

    public void registerCloudListeners() {
        var em = cloudAPI.eventBus();

        em.subscribe(CloudPlayerJoinEvent.class, event ->
                broadcastTick()
        );

        em.subscribe(CloudPlayerDisconnectEvent.class, event -> {
            broadcastTick();
        });
    }

    public void register(WsContext context) {
        subscribers.add(context);
    }

    public void unregister(WsContext context) {
        subscribers.remove(context);
    }

    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        subscribers.clear();
    }

    private void broadcastTick() {
        if (subscribers.isEmpty()) {
            return;
        }

        try {
            List<PlayerDto> players = playerService.getOnlinePlayers();

            WsEventDto<Object> event = WsEventDto.builder()
                    .type("player_update")
                    .data(players)
                    .build();

            subscribers.removeIf(ctx -> !ctx.session.isOpen());
            for (WsContext ctx : subscribers) {
                try {
                    ctx.send(event);
                } catch (Exception e) {
                    log.warn("Could not send player update to session {}", ctx.sessionId());
                }
            }
        } catch (Exception e) {
            log.error("Error during player broadcast tick", e);
        }
    }
}

