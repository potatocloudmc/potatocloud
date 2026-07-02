package net.potatocloud.network.protocol;

import net.potatocloud.network.NetworkConnection;
import net.potatocloud.network.request.RequestManager;
import net.potatocloud.network.request.RequestPacket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PacketManager {

    private final Map<Integer, Packet.Codec<? extends Packet>> codecs = new ConcurrentHashMap<>();
    private final Map<Class<? extends Packet>, Integer> packetIds = new ConcurrentHashMap<>();

    private final Map<Class<? extends Packet>, CopyOnWriteArrayList<PacketHandler<? extends Packet>>> handlers = new ConcurrentHashMap<>();

    private final RequestManager requestManager;

    public PacketManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

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


    public <T extends Packet> void on(Class<T> type, PacketHandler<T> handler) {
        handlers.computeIfAbsent(type, _ -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> void dispatch(NetworkConnection connection, T packet) {
        if (requestManager.handleResponse(packet)) {
            return;
        }

        final List<PacketHandler<? extends Packet>> list = handlers.get(packet.getClass());
        if (list == null) {
            return;
        }

        int requestId = 0;
        if (packet instanceof RequestPacket requestPacket) {
            requestId = requestManager.requestId(requestPacket);
        }

        final PacketContext<T> ctx = new PacketContext<>(connection, requestManager, packet, requestId);

        for (PacketHandler<? extends Packet> handler : list) {
            ((PacketHandler<T>) handler).handle(ctx);
        }
    }
}
