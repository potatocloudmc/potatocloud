package net.potatocloud.network.codec;

public abstract class AbstractPacketBuffer implements PacketBuffer {

    @Override
    public void writeVarInt(int value) {
        while ((value & -128) != 0) {
            writeByte((byte) (value & 127 | 128));
            value >>>= 7;
        }

        writeByte((byte) value);
    }

    @Override
    public int readVarInt() {
        int out = 0;
        int bytes = 0;

        byte in;
        do {
            in = readByte();
            out |= (in & 127) << bytes++ * 7;
            if (bytes > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((in & 128) == 128);

        return out;
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
