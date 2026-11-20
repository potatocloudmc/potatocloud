package net.potatocloud.node.module;

import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.module.Module;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class ModuleManager {

    private final Logger logger;
    private final ModuleLoader loader;
    private final Map<String, LoadedModule> modules = new HashMap<>();

    public ModuleManager(Logger logger) {
        this.logger = logger;
        this.loader = new ModuleLoader(this, logger);
    }

    public void load(Path modulesPath) {
        loader.load(modulesPath);

        if (modules.isEmpty()) {
            return;
        }

        final int count = modules.size();
        final String moduleText = count == 1 ? "module" : "modules";

        logger.info("Loaded &a" + count + "&7 " + moduleText + "&8:");
        modules.values().stream()
                .map(LoadedModule::module)
                .sorted(Comparator.comparing(Module::name))
                .forEach(module -> logger.info("&8» &a" + module.name() + " &7v" + module.version()));
    }

    public void register(LoadedModule module) {
        final String name = module.module().name();
        final LoadedModule previous = modules.putIfAbsent(name, module);

        if (previous != null) {
            throw new IllegalStateException("Module already registered: " + name);
        }
    }

    public void enableAll() {
        for (LoadedModule module : modules.values()) {
            try {
                module.module().onEnable();
            } catch (Exception e) {
                logger.error("Failed to enable module " + module.module().name() + ": " + e.getMessage());
            }
        }
    }

    public void disableAll() {
        for (LoadedModule module : modules.values()) {
            try {
                module.module().onDisable();
            } catch (Exception e) {
                logger.error("Failed to disable module " + module.module().name() + ": " + e.getMessage());
            }
        }

        for (LoadedModule module : modules.values()) {
            try {
                module.classLoader().close();
            } catch (IOException e) {
                logger.error("Failed to close class loader of module " + module.module().name() + ": " + e.getMessage());
            }
        }

        modules.clear();
    }

    public LoadedModule get(String name) {
        return modules.get(name);
    }
}
