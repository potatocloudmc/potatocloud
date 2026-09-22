package net.potatocloud.webinterface.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.event.events.service.ServiceStartingEvent;
import net.potatocloud.api.event.events.service.ServiceStoppedEvent;
import net.potatocloud.api.event.events.service.ServiceStoppingEvent;
import net.potatocloud.node.Node;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.node.screen.ScreenSubscriber;
import net.potatocloud.webinterface.dto.response.ScreenLogResponse;
import net.potatocloud.webinterface.dto.response.WsEnvelope;
import net.potatocloud.webinterface.service.ScreenLogService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ScreenLogServiceImpl implements ScreenLogService {

    private final Map<String, Set<WebSocketConnection>> connectionsByScreen = new ConcurrentHashMap<>();
    private final Map<String, ScreenSubscriber> subscribersByScreen = new ConcurrentHashMap<>();

    @Inject
    ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        EventBus eventBus = CloudAPI.instance().eventBus();
        eventBus.subscribe(ServiceStoppedEvent.class, event -> removeScreenListener(event.serviceName()));
        eventBus.subscribe(ServiceStoppingEvent.class, event -> removeScreenListener(event.serviceName()));
        eventBus.subscribe(ServiceStartingEvent.class, event -> attachListener(event.serviceName()));
    }

    @Override
    public void register(String screenName, WebSocketConnection connection) {
        connectionsByScreen.computeIfAbsent(screenName, key -> ConcurrentHashMap.newKeySet()).add(connection);
        attachListener(screenName);
    }

    private void attachListener(String screenName) {
        subscribersByScreen.computeIfAbsent(screenName, key -> {
            Screen screen = Node.instance().screenManager().get(key);
            if (screen == null) {
                return null;
            }

            ScreenSubscriber subscriber = line -> broadcast(key, line);
            screen.subscribe(subscriber);
            return subscriber;
        });
    }

    @Override
    public void unregister(String screenName, WebSocketConnection connection) {
        Set<WebSocketConnection> connections = connectionsByScreen.get(screenName);
        if (connections == null) {
            return;
        }
        connections.remove(connection);
        if (connections.isEmpty()) {
            connectionsByScreen.remove(screenName);
            removeScreenListener(screenName);
        }
    }

    @PreDestroy
    public void shutdown() {
        connectionsByScreen.keySet().forEach(this::removeScreenListener);
        connectionsByScreen.clear();
    }

    @Override
    public void broadcast(String screenName, String line) {
        Set<WebSocketConnection> connections = connectionsByScreen.get(screenName);
        if (connections == null || connections.isEmpty()) {
            return;
        }

        ScreenLogResponse screenLogDto = new ScreenLogResponse(screenName, line);
        WsEnvelope<ScreenLogResponse> event = new WsEnvelope<>("service_screen_log", screenLogDto);

        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            Log.error("Failed to serialize WebSocket event for screen: " + screenName, e);
            return;
        }

        for (WebSocketConnection connection : connections) {
            try {
                connection.sendTextAndAwait(jsonMessage);
            } catch (Exception e) {
                Log.error("Failed to send screen log to connection " + connection.id(), e);
            }
        }
    }

    @Override
    public void removeScreenListener(String screenName) {
        ScreenSubscriber subscriber = subscribersByScreen.remove(screenName);
        if (subscriber == null) {
            return;
        }
        Screen screen = Node.instance().screenManager().get(screenName);
        if (screen != null) {
            screen.unsubscribe(subscriber);
        }
    }
}