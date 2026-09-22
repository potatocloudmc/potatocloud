package net.potatocloud.webinterface.service;

import io.quarkus.websockets.next.WebSocketConnection;

public interface ScreenLogService {

    void register(String screenName, WebSocketConnection connection);

    void unregister(String screenName, WebSocketConnection connection);

    void broadcast(String screenName, String line);

    void removeScreenListener(String screenName);

}
