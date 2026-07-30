package net.potatocloud.network;

import net.potatocloud.network.broadcast.Broadcast;
import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.request.RequestPacket;
import net.potatocloud.network.request.ResponsePacket;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NetworkServer extends NetworkComponent {

    void start(String hostname, int port);

    boolean running();

    Collection<NetworkConnection> connections();

    int port();

    void sendTo(NetworkConnection client, Packet packet);

    <T extends ResponsePacket> CompletableFuture<T> request(NetworkConnection connection, RequestPacket packet, Class<T> type);

    void onClientDisconnected(Consumer<NetworkConnection> handler);

    default Broadcast broadcast() {
        return new Broadcast(this);
    }
}
