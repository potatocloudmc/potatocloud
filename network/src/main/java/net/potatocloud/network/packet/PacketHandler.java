package net.potatocloud.network.packet;

public interface PacketHandler<T extends Packet> {

    void handle(PacketContext<T> ctx);

}
