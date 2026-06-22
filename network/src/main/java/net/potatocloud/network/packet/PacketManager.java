package net.potatocloud.network.packet;

import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.packet.request.PendingRequest;
import net.potatocloud.network.packet.request.RequestPacket;
import net.potatocloud.network.packet.request.ResponsePacket;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class PacketManager {

    private final Map<Integer, Packet.Codec<? extends Packet>> codecs = new ConcurrentHashMap<>();
    private final Map<Class<? extends Packet>, Integer> packetIds = new ConcurrentHashMap<>();

    private final Map<Class<? extends Packet>, CopyOnWriteArrayList<PacketHandler<? extends Packet>>> handlers = new ConcurrentHashMap<>();

    private final Map<Integer, PendingRequest<?>> pending = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger(1);
    private final Map<Packet, Integer> requestIds = Collections.synchronizedMap(new IdentityHashMap<>());

    public <T extends Packet> void register(int id, Class<T> clazz, Packet.Codec<T> codec) {
        if (codecs.containsKey(id)) {
            throw new IllegalStateException("Duplicate packet id: " + id + " for " + clazz.getName());
        }

        if (packetIds.containsKey(clazz)) {
            throw new IllegalStateException("Packet already registered: " + clazz.getName());
        }

        codecs.put(id, codec);
        packetIds.put(clazz, id);
    }

    public int packetId(Packet packet) {
        final Integer id = packetIds.get(packet.getClass());
        if (id == null) {
            throw new IllegalStateException("Packet not registered: " + packet.getClass().getName());
        }

        return id;
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> Packet.Codec<T> codec(int id) {
        return (Packet.Codec<T>) codecs.get(id);
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

    public <T extends Packet> void on(Class<T> type, PacketHandler<T> handler) {
        handlers.computeIfAbsent(type, _ -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public <T extends ResponsePacket> CompletableFuture<T> request(NetworkConnection connection, RequestPacket packet, Class<T> type) {
        final int id = requestCounter.getAndIncrement();
        requestIds.put(packet, id);

        final CompletableFuture<T> future = new CompletableFuture<>();
        pending.put(id, new PendingRequest<>(type, future));

        connection.send(packet);

        future.whenComplete((_, _) -> pending.remove(id));

        return future;
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> void dispatch(NetworkConnection connection, T packet) {
        if (packet instanceof ResponsePacket response) {
            final int id = requestId(packet);
            removeRequest(packet);

            final PendingRequest<?> pendingRequest = pending.get(id);

            if (pendingRequest == null || pendingRequest.future() == null || pendingRequest.future().isDone()) {
                return;
            }

            if (pendingRequest.responseType().isInstance(response)) {
                ((CompletableFuture<Object>) pendingRequest.future()).complete(response);
            } else {
                pendingRequest.future().completeExceptionally(new IllegalStateException(
                        "Expected " + pendingRequest.responseType().getSimpleName() + " but got " + response.getClass().getSimpleName()));
            }
            return;
        }

        final List<PacketHandler<? extends Packet>> list = handlers.get(packet.getClass());
        if (list == null) {
            return;
        }

        int requestId = 0;
        if (packet instanceof RequestPacket requestPacket) {
            requestId = requestId(requestPacket);
        }

        final PacketContext<T> ctx = new PacketContext<>(connection, this, packet, requestId);

        for (PacketHandler<? extends Packet> handler : list) {
            ((PacketHandler<T>) handler).handle(ctx);
        }
    }
}
