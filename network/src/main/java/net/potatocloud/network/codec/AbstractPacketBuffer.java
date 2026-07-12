package net.potatocloud.network.codec;

public abstract class AbstractPacketBuffer implements PacketBuffer {

    @Override
    public void writeVarInt(int value) {
        VarInts.write(this, value);
    }

    @Override
    public int readVarInt() {
        return VarInts.read(this);
    }

    @Override
    public void writeString(String s) {
        write(s, String.class);
    }

    @Override
    public String readString() {
        return read(String.class);
    }

    @Override
    public <T> void write(T value, Class<T> type) {
        SerializerRegistry.get(type).write(this, value);
    }

    @Override
    public <T> T read(Class<T> type) {
        return SerializerRegistry.get(type).read(this);
    }

    @Override
    public <T> void write(T value, TypeSerializer<T> serializer) {
        serializer.write(this, value);
    }

    @Override
    public <T> T read(TypeSerializer<T> serializer) {
        return serializer.read(this);
    }
}
