package net.potatocloud.network.request;

import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.protocol.Packet;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class RequestManager {

    private static final long REQUEST_TIMEOUT_SECONDS = 10;

    private final Map<Integer, PendingRequest<?>> pending = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger(1);
    private final Map<Packet, Integer> requestIds = Collections.synchronizedMap(new IdentityHashMap<>());

    public <T extends ResponsePacket> CompletableFuture<T> request(NetworkConnection connection, RequestPacket packet, Class<T> type) {
        final int id = requestCounter.getAndIncrement();
        requestIds.put(packet, id);

        final CompletableFuture<T> future = new CompletableFuture<T>();
        future.orTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        pending.put(id, new PendingRequest<>(type, future));

        connection.send(packet);

        future.whenComplete((_, _) -> {
            pending.remove(id);
            requestIds.remove(packet);
        });

        return future;
    }

    @SuppressWarnings("unchecked")
    public boolean handleResponse(Packet packet) {
        if (!(packet instanceof ResponsePacket response)) {
            return false;
        }

        final int id = requestId(packet);
        removeRequest(packet);

        final PendingRequest<?> pendingRequest = pending.get(id);

        if (pendingRequest == null || pendingRequest.future() == null || pendingRequest.future().isDone()) {
            return false;
        }

        if (pendingRequest.responseType().isInstance(response)) {
            ((CompletableFuture<Object>) pendingRequest.future()).complete(response);
        } else {
            pendingRequest.future().completeExceptionally(new IllegalStateException(
                    "Expected " + pendingRequest.responseType().getSimpleName() + " but got " + response.getClass().getSimpleName()));
        }
        return true;
    }

    public int requestId(Packet packet) {
        return requestIds.getOrDefault(packet, 0);
    }

    public void requestId(Packet packet, int requestId) {
        if (requestId != 0) {
            requestIds.put(packet, requestId);
        }
    }

    public void removeRequest(Packet packet) {
        requestIds.remove(packet);
    }
}
