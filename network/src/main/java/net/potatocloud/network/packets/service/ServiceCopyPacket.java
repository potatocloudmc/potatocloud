package net.potatocloud.network.packets.service;

import net.potatocloud.api.template.Template;
import net.potatocloud.api.template.TemplateCopyOptions;
import net.potatocloud.network.codec.PacketBuffer;
import net.potatocloud.network.protocol.Packet;

public record ServiceCopyPacket(String serviceName, Template template, TemplateCopyOptions options) implements Packet {

    public static final Codec<ServiceCopyPacket> CODEC = new Codec<>() {

        @Override
        public void encode(ServiceCopyPacket packet, PacketBuffer buf) {
            buf.writeString(packet.serviceName());
            buf.write(packet.template(), Template.class);
            buf.writeString(packet.options().path());
            buf.writeBoolean(packet.options().replaceExisting());
        }

        @Override
        public ServiceCopyPacket decode(PacketBuffer buf) {
            return new ServiceCopyPacket(
                    buf.readString(),
                    buf.read(Template.class),
                    new TemplateCopyOptions(buf.readString(), buf.readBoolean())
            );
        }
    };
}