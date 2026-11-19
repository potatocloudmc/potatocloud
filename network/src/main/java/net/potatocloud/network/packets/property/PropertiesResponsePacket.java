package net.potatocloud.network.packets.property;

import net.potatocloud.api.property.PropertyKey;
import net.potatocloud.network.codec.CollectionSerializers;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.request.ResponsePacket;

import java.util.Map;

public record PropertiesResponsePacket(Map<PropertyKey<?>, Object> properties) implements ResponsePacket {

    public static final Codec<PropertiesResponsePacket> CODEC = new Codec<>() {

        @Override
        public void encode(PropertiesResponsePacket packet, PacketBuffer buf) {
            buf.write(packet.properties(), CollectionSerializers.propertyMap());
        }

        @Override
        public PropertiesResponsePacket decode(PacketBuffer buf) {
            return new PropertiesResponsePacket(buf.read(CollectionSerializers.propertyMap()));
        }
    };
}
