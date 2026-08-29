package net.potatocloud.network.codec.serializers;

import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.codec.TypeSerializer;

import java.time.Instant;
import java.util.UUID;

public final class ObjectSerializer implements TypeSerializer<Object> {

    private static final int NULL = 0;
    private static final int STRING = 1;
    private static final int INTEGER = 2;
    private static final int BOOLEAN = 3;
    private static final int LONG = 4;
    private static final int FLOAT = 5;
    private static final int DOUBLE = 6;
    private static final int BYTE = 7;
    private static final int SHORT = 8;
    private static final int CHARACTER = 9;
    private static final int UUID_VALUE = 10;
    private static final int INSTANT = 11;

    @Override
    public void write(PacketBuffer buffer, Object value) {
        if (value == null) {
            buffer.writeVarInt(NULL);
            return;
        }

        switch (value) {
            case String s -> {
                buffer.writeVarInt(STRING);
                buffer.writeString(s);
            }
            case Integer i -> {
                buffer.writeVarInt(INTEGER);
                buffer.writeInt(i);
            }
            case Boolean b -> {
                buffer.writeVarInt(BOOLEAN);
                buffer.writeBoolean(b);
            }
            case Long l -> {
                buffer.writeVarInt(LONG);
                buffer.writeLong(l);
            }
            case Float f -> {
                buffer.writeVarInt(FLOAT);
                buffer.writeFloat(f);
            }
            case Double d -> {
                buffer.writeVarInt(DOUBLE);
                buffer.writeDouble(d);
            }
            case Byte b -> {
                buffer.writeVarInt(BYTE);
                buffer.writeByte(b);
            }
            case Short s -> {
                buffer.writeVarInt(SHORT);
                buffer.writeInt(s);
            }
            case Character c -> {
                buffer.writeVarInt(CHARACTER);
                buffer.writeInt(c);
            }
            case UUID uuid -> {
                buffer.writeVarInt(UUID_VALUE);
                buffer.write(uuid, UUID.class);
            }
            case Instant instant -> {
                buffer.writeVarInt(INSTANT);
                buffer.write(instant, Instant.class);
            }
            default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }

    @Override
    public Object read(PacketBuffer buffer) {
        final int type = buffer.readVarInt();
        return switch (type) {
            case NULL -> null;
            case STRING -> buffer.readString();
            case INTEGER -> buffer.readInt();
            case BOOLEAN -> buffer.readBoolean();
            case LONG -> buffer.readLong();
            case FLOAT -> buffer.readFloat();
            case DOUBLE -> buffer.readDouble();
            case BYTE -> buffer.readByte();
            case SHORT -> (short) buffer.readInt();
            case CHARACTER -> (char) buffer.readInt();
            case UUID_VALUE -> buffer.read(UUID.class);
            case INSTANT -> buffer.read(Instant.class);
            default -> throw new IllegalArgumentException("Unknown object type: " + type);
        };
    }
}
