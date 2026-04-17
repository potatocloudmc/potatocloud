package net.potatocloud.connector.logging;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.packet.packets.logging.LogMessagePacket;

@RequiredArgsConstructor
public class ConnectorLogger implements Logger {

    private final NetworkClient client;

    @Override
    public void log(Level level, String message) {
        client.send(new LogMessagePacket(level.name(), message));
    }
}
