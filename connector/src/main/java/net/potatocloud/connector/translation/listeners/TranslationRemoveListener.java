package net.potatocloud.connector.translation.listeners;

import lombok.RequiredArgsConstructor;
import net.potatocloud.api.translation.Translation;
import net.potatocloud.connector.translation.TranslationManagerImpl;
import net.potatocloud.core.networking.NetworkConnection;
import net.potatocloud.core.networking.packet.PacketListener;
import net.potatocloud.core.networking.packet.packets.translation.TranslationRemovePacket;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TranslationRemoveListener implements PacketListener<TranslationRemovePacket> {

    private final TranslationManagerImpl translationManager;

    @Override
    public void onPacket(NetworkConnection connection, TranslationRemovePacket packet) {
        final List<Translation> translations = translationManager.getAllTranslations();
        Optional<Translation> optional = translations.stream().filter(translation -> translation.getKey().equals(packet.getKey())).findFirst();
        if (optional.isEmpty()) return;
        translations.remove(optional.get());
    }
}
