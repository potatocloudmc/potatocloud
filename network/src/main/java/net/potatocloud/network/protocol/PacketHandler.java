package net.potatocloud.network.protocol;

public interface PacketHandler<T extends Packet> {

    void handle(PacketContext<T> ctx);

}
