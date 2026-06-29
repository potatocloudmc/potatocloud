package net.potatocloud.network;

import net.potatocloud.network.broadcast.Broadcast;
import net.potatocloud.network.protocol.Packet;

import java.util.Collection;
import java.util.function.Consumer;

public interface NetworkServer extends NetworkComponent {

    void start(String hostname, int port);

    boolean running();

    Collection<NetworkConnection> connectedSessions();

    int port();

    void send(NetworkConnection client, Packet packet);

    default void addDisconnectHandler(Consumer<NetworkConnection> handler) {}

    default Broadcast broadcast() {
        return new Broadcast(this);
    }
}
