package net.potatocloud.network.broadcast;

import net.potatocloud.network.ConnectionType;
import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.NetworkServer;
import net.potatocloud.network.protocol.Packet;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class Broadcast {

    private final NetworkServer server;
    private final Set<NetworkConnection> excludedConnections = new HashSet<>();
    private Predicate<NetworkConnection> filter;

    public Broadcast(NetworkServer server) {
        this.server = server;
    }

    public Broadcast exclude(NetworkConnection connection) {
        excludedConnections.add(connection);
        return this;
    }

    public Broadcast filter(Predicate<NetworkConnection> predicate) {
        this.filter = this.filter == null ? predicate : this.filter.and(predicate);
        return this;
    }

    public Broadcast connectors() {
        return filter(connection -> connection.type() == ConnectionType.CONNECTOR);
    }

    public void send(Packet packet) {
        server.connections().forEach(connection -> {
            if (excludedConnections.contains(connection)) {
                return;
            }

            if (filter != null && !filter.test(connection)) {
                return;
            }

            server.sendTo(connection, packet);
        });
    }
}
