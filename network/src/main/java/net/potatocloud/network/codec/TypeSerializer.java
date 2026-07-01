package net.potatocloud.network.codec;

public interface TypeSerializer<T> {

    void write(PacketBuffer buffer, T value);

    T read(PacketBuffer buffer);

}
