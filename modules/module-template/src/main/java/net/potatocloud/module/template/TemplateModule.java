package net.potatocloud.module.template;

import com.google.auto.service.AutoService;
import net.potatocloud.api.module.AbstractModule;
import net.potatocloud.api.module.PotatoModule;
import net.potatocloud.api.utils.version.Version;

@AutoService(PotatoModule.class)
public class TemplateModule extends AbstractModule {

    @Override
    public void onEnable() {
        info("[TemplateModule] Loaded module...");
    }

    @Override
    public void onDisable() {
        info("[TemplateModule] Unloaded module...");
    }

    @Override
    public String getName() {
        return "TemplateModule";
    }

    @Override
    public Version version() {
        return Version.of(1, 0, 0);
    }

}
