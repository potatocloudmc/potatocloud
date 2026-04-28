package net.potatocloud.plugin.platform.spigot;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.translation.Translation;
import net.potatocloud.connector.translation.TranslationManagerImpl;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.Optional;

public class CoreAPI {
    private static CoreAPI instance;

    private final CaptainHook hookManager;

    private CoreAPI() {
        this.hookManager = new CaptainHook();
    }

    public void connectGroup(Player p) {
        CloudAPI api = SpigotPlugin.getInstance().getApi();
        Optional<Service> o = api.getServiceManager().getAllServices(api.getServiceManager().getCurrentService().getServiceGroup()).stream().findAny();
        if (o.isEmpty()) return;
        api.getPlayerManager().getCloudPlayer(p.getUniqueId()).connectWithService(o.get());
    }

    public String translate(Player p, String key, String... placeholders) {
        Optional<Translation> t = SpigotPlugin.getInstance().getApi().getTranslationManager().getAllTranslations().stream()
                .filter(translation -> translation.getLocale().toString().equals(p.getLocale()))
                .filter(translation -> translation.getKey().equals(key)).findFirst();
        if (t.isEmpty()) return key;
        return MessageFormat.format(t.get().getValue(), (Object[]) placeholders);
    }

    public void requestTranslations(String group) {
        final TranslationManagerImpl translationManager = (TranslationManagerImpl) SpigotPlugin.getInstance().getApi().getTranslationManager();
        translationManager.requestTranslations(group);
    }

    public String getCurrentGroupName() {
        return SpigotPlugin.getInstance().getApi().getServiceManager().getCurrentService().getServiceGroup().getName();
    }

    public String getCurrentServiceName() {
        return SpigotPlugin.getInstance().getApi().getServiceManager().getCurrentService().getName();
    }

    public CaptainHook getHookManager() {
        return hookManager;
    }

    public static CoreAPI getInstance() {
        if (instance == null) instance = new CoreAPI();
        return instance;
    }
}
