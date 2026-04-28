package net.potatocloud.connector.translation;

import net.potatocloud.api.translation.Translation;
import net.potatocloud.api.translation.TranslationManager;
import net.potatocloud.connector.translation.listeners.TranslationAddListener;
import net.potatocloud.connector.translation.listeners.TranslationRemoveListener;
import net.potatocloud.connector.translation.listeners.TranslationUpdateListener;
import net.potatocloud.core.networking.NetworkClient;
import net.potatocloud.core.networking.packet.packets.service.*;
import net.potatocloud.core.networking.packet.packets.translation.RequestTranslationPacket;
import net.potatocloud.core.networking.packet.packets.translation.TranslationAddPacket;
import net.potatocloud.core.networking.packet.packets.translation.TranslationRemovePacket;
import net.potatocloud.core.networking.packet.packets.translation.TranslationUpdatePacket;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TranslationManagerImpl implements TranslationManager {

    private final List<Translation> translations = new CopyOnWriteArrayList<>();

    private final NetworkClient client;

    public TranslationManagerImpl(NetworkClient client) {
        this.client = client;

        client.on(TranslationAddPacket.class, new TranslationAddListener(this));
        client.on(TranslationRemovePacket.class, new TranslationRemoveListener(this));

        client.on(TranslationUpdatePacket.class, new TranslationUpdateListener(this));
    }

    public void addTranslation(Translation translation) {
        translations.add(translation);
    }

    @Override
    public Translation getTranslation(String key) {
        return translations.stream()
                .filter(translation -> translation.getKey().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }

    public void requestTranslations(String group) {
        client.send(new RequestTranslationPacket(group));
    }

    @Override
    public List<Translation> getAllTranslations() {
        return Collections.unmodifiableList(translations);
    }

    @Override
    public void updateTranslation(Translation translation) {
        client.send(new TranslationUpdatePacket(
                translation.getKey(),
                translation.getValue()
        ));
    }
}