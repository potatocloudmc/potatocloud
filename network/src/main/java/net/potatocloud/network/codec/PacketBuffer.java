package net.potatocloud.network.codec;

public interface PacketBuffer {

    void writeByte(byte b);

    byte readByte();

    void writeBytes(byte[] bytes);

    void readBytes(byte[] bytes);

    void writeBoolean(boolean b);

    boolean readBoolean();

    void writeInt(int i);

    int readInt();

    void writeLong(long l);

    long readLong();

    void writeFloat(float f);

    float readFloat();

    void writeDouble(double d);

    double readDouble();

    void writeVarInt(int value);

    int readVarInt();

    void writeString(String s);

    String readString();

    <T> void write(T value, Class<T> type);

    <T> T read(Class<T> type);

    <T> void write(T value, TypeSerializer<T> serializer);

    <T> T read(TypeSerializer<T> serializer);

}
