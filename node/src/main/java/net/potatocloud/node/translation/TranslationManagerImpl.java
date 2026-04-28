package net.potatocloud.node.translation;

import com.google.gson.JsonParser;
import lombok.SneakyThrows;
import net.potatocloud.api.translation.Translation;
import net.potatocloud.api.translation.TranslationManager;
import net.potatocloud.common.DockerFile;
import net.potatocloud.connector.translation.TranslationImpl;
import net.potatocloud.core.networking.NetworkServer;
import net.potatocloud.core.networking.packet.packets.translation.RequestTranslationPacket;
import net.potatocloud.core.networking.packet.packets.translation.TranslationUpdatePacket;
import net.potatocloud.node.translation.listeners.RequestTranslationListener;
import net.potatocloud.node.translation.listeners.TranslationUpdateListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class TranslationManagerImpl implements TranslationManager {

    private final List<Translation> translations = new CopyOnWriteArrayList<>();

    private final NetworkServer server;

    public TranslationManagerImpl(NetworkServer server) {
        this.server = server;
        load();
        server.on(RequestTranslationPacket.class, new RequestTranslationListener(this));
        server.on(TranslationUpdatePacket.class, new TranslationUpdateListener(this, server));
    }

    @Override
    public Translation getTranslation(String key) {
        return translations.stream()
                .filter(translation -> translation.getKey().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Translation> getAllTranslations() {
        return Collections.unmodifiableList(translations);
    }

    @Override
    public void updateTranslation(Translation translation) {
        server.generateBroadcast().broadcast(new TranslationUpdatePacket(
                translation.getKey(),
                translation.getValue()
        ));
    }

    @SneakyThrows
    private void load() {
        List<Path> files = Files.list(Path.of(DockerFile.stringFromServerDir("lang"))).toList();
        for (Path file : files) {
            JsonParser.parseString(Files.readString(file)).getAsJsonArray().forEach(jsonElement -> {
                jsonElement.getAsJsonObject().entrySet().forEach(entry -> {
                    String key = entry.getKey();
                    translations.add(new TranslationImpl(key.substring(0, key.indexOf(".")), Locale.of(file.getFileName().toString().substring(0, 2), file.getFileName().toString().substring(3, 5).toLowerCase()), key, entry.getValue().getAsString()));
                });
            });
        }
    }
}
