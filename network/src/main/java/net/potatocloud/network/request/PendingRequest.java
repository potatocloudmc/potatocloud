package net.potatocloud.network.request;

import net.potatocloud.network.NetworkConnection;

import java.util.concurrent.CompletableFuture;

public record PendingRequest<T extends ResponsePacket>(
        NetworkConnection connection,
        Class<T> responseType,
        CompletableFuture<T> future
) {

}
