package net.potatocloud.network.netty;

import io.netty.buffer.ByteBuf;
import net.potatocloud.network.codec.AbstractPacketBuffer;

public final class NettyPacketBuffer extends AbstractPacketBuffer {

    private final ByteBuf buf;

    public NettyPacketBuffer(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public void writeByte(byte b) {
        buf.writeByte(b);
    }

    @Override
    public byte readByte() {
        return buf.readByte();
    }

    @Override
    public void writeBytes(byte[] bytes) {
        buf.writeBytes(bytes);
    }

    @Override
    public void readBytes(byte[] bytes) {
        buf.readBytes(bytes);
    }

    @Override
    public void writeBoolean(boolean b) {
        buf.writeBoolean(b);
    }

    @Override
    public boolean readBoolean() {
        return buf.readBoolean();
    }

    @Override
    public void writeInt(int i) {
        buf.writeInt(i);
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }

    @Override
    public void writeLong(long l) {
        buf.writeLong(l);
    }

    @Override
    public long readLong() {
        return buf.readLong();
    }

    @Override
    public void writeFloat(float f) {
        buf.writeFloat(f);
    }

    @Override
    public float readFloat() {
        return buf.readFloat();
    }

    @Override
    public void writeDouble(double d) {
        buf.writeDouble(d);
    }

    @Override
    public double readDouble() {
        return buf.readDouble();
    }
}
