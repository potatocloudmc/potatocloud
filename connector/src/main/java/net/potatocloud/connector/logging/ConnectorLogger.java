package net.potatocloud.connector.logging;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.network.NetworkClient;
import net.potatocloud.network.packet.packets.logging.LogMessagePacket;

public class ConnectorLogger implements Logger {

    private final NetworkClient client;

    public ConnectorLogger(NetworkClient client) {
        this.client = client;
    }

    @Override
    public void log(Level level, String message) {
        client.send(new LogMessagePacket(level.name(), message));
    }
}
