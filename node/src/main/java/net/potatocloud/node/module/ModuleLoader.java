package net.potatocloud.node.module;

import net.potatocloud.api.module.AbstractModule;
import net.potatocloud.api.module.Module;
import net.potatocloud.api.version.Version;
import net.potatocloud.common.FileUtils;
import net.potatocloud.common.JacksonUtils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModuleLoader {

    private final ModuleManager moduleManager;

    public ModuleLoader(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void load(Path modulesPath) {
        try {
            if (Files.notExists(modulesPath)) {
                Files.createDirectories(modulesPath);
                return;
            }

            FileUtils.list(modulesPath).stream()
                    .filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".jar"))
                    .forEach(this::loadJar);
        } catch (Exception e) {
            throw new RuntimeException("Error loading modules: " + e);
        }
    }

    private void loadJar(Path jar) {
        URLClassLoader loader = null;

        try {
            loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, getClass().getClassLoader());

            try (InputStream stream = loader.getResourceAsStream("module.yml")) {
                if (stream == null) {
                    loader.close();
                    return;
                }

                final ModuleConfig config = JacksonUtils.YAML_MAPPER.readValue(stream, ModuleConfig.class);

                final Class<?> clazz = Class.forName(config.mainClass(), true, loader);
                final Module module = (Module) clazz.getDeclaredConstructor().newInstance();

                if (module instanceof AbstractModule abstractModule) {
                    abstractModule.name(config.name());
                    abstractModule.version(Version.fromString(config.version()));
                }

                module.onLoad();
                moduleManager.register(new LoadedModule(module, loader));
            }
        } catch (Exception e) {
            if (loader != null) {
                try {
                    loader.close();
                } catch (Exception ignored) {
                }
            }
            throw new RuntimeException("Error loading module " + jar.getFileName() + ": " + e.getMessage(), e);
        }
    }
}
