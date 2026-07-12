package net.potatocloud.network;

import net.potatocloud.network.protocol.Packet;
import net.potatocloud.network.request.RequestPacket;
import net.potatocloud.network.request.ResponsePacket;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NetworkClient extends NetworkComponent {

    void connect(String host, int port);

    void send(Packet packet);

    void close();

    void onConnected(Runnable handler);

    <T extends ResponsePacket> CompletableFuture<T> request(RequestPacket packet, Class<T> type);

}
