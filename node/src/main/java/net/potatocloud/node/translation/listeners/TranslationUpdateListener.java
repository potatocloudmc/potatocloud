package net.potatocloud.node.translation.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.property.Property;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.service.ServiceStatus;
import net.potatocloud.api.translation.Translation;
import net.potatocloud.api.translation.TranslationManager;
import net.potatocloud.common.PropertyUtil;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.service.ServiceUpdatePacket;
import net.potatocloud.core.networking.packet.packets.translation.TranslationUpdatePacket;

@RequiredArgsConstructor
public class TranslationUpdateListener implements PacketListener<TranslationUpdatePacket> {

    private final TranslationManager translationManager;
    private final NetworkServer server;

    @Override
    public void onPacket(NetworkConnection connection, TranslationUpdatePacket packet) {
        final Translation translation = translationManager.getTranslation(packet.getKey());
        if (translation == null) {
            return;
        }
        translation.setValue(packet.getValue());
        server.generateBroadcast().exclude(connection).broadcast(packet);
    }
}
