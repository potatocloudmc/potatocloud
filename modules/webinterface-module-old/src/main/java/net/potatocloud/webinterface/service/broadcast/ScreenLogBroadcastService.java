package net.potatocloud.webinterface.service.broadcast;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.websocket.WsContext;
import lombok.RequiredArgsConstructor;
import net.potatocloud.node.Node;
import net.potatocloud.node.screen.Screen;
import net.potatocloud.webinterface.WebInterfaceModule;
import net.potatocloud.webinterface.dto.event.WsEventDto;
import net.potatocloud.webinterface.dto.screen.ScreenLogDto;
import org.eclipse.jetty.websocket.api.Callback;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


@RequiredArgsConstructor
public class ScreenLogBroadcastService {

    private final Node node;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Set<WsContext>> sessionsByScreen = new ConcurrentHashMap<>();

    private final Map<String, Consumer<String>> listenerByScreen = new ConcurrentHashMap<>();

    public void register(String screenName, WsContext context) {
        sessionsByScreen.computeIfAbsent(screenName, key -> ConcurrentHashMap.newKeySet()).add(context);

        listenerByScreen.computeIfAbsent(screenName, key -> {
            Screen screen = node.screenManager().get(key);
            if (screen == null) {
                return null;
            }
            Consumer<String> listener = line -> broadcast(key, line);
            screen.subscribe(listener);
            return listener;
        });
    }

    public void unregister(String screenName, WsContext context) {
        Set<WsContext> sessions = sessionsByScreen.get(screenName);
        if (sessions == null) {
            return;
        }

        sessions.remove(context);

        if (sessions.isEmpty()) {
            sessionsByScreen.remove(screenName);
            removeScreenListener(screenName);
        }
    }

    public void shutdown() {
        sessionsByScreen.keySet().forEach(this::removeScreenListener);
        sessionsByScreen.clear();
    }

    private void broadcast(String screenName, String line) {
        Set<WsContext> sessions = sessionsByScreen.get(screenName);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        ScreenLogDto screenLogDto = new ScreenLogDto(screenName, line);
        WsEventDto<Object> event = WsEventDto.builder()
                .type("screen:logs:batch")
                .data(screenLogDto)
                .build();

        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            WebInterfaceModule.getInstance().getCloudAPI().logger()
                    .error("Failed to serialize WebSocket event for screen: " + screenName);
            return;
        }

        for (WsContext ctx : sessions) {
            try {
                if (ctx.session instanceof org.eclipse.jetty.websocket.api.Session jettySession) {
                    if (jettySession.isOpen()) {
                        jettySession.sendText(jsonMessage, Callback.NOOP);
                    }
                }
            } catch (Exception ignored) {
                WebInterfaceModule.getInstance().getCloudAPI().logger()
                        .error("Failed to send screen log to session " + ctx.sessionId());
            }
        }
    }

    private void removeScreenListener(String screenName) {
        Consumer<String> listener = listenerByScreen.remove(screenName);
        if (listener == null) {
            return;
        }
        Screen screen = node.screenManager().get(screenName);
        if (screen != null) {
            screen.unsubscribe(listener);
        }
    }
}