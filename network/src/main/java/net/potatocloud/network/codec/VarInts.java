package net.potatocloud.network.codec;

public final class VarInts {

    private VarInts() {
    }

    public static void write(PacketBuffer buf, int value) {
        while ((value & -128) != 0) {
            buf.writeByte((byte) (value & 127 | 128));
            value >>>= 7;
        }

        buf.writeByte((byte) value);
    }

    public static int read(PacketBuffer buf) {
        int out = 0;
        int bytes = 0;

        byte in;
        do {
            in = buf.readByte();
            out |= (in & 127) << bytes++ * 7;
            if (bytes > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((in & 128) == 128);

        return out;
    }
}
