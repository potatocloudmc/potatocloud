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
        this.loader = new ModuleLoader(this);
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
        modules.put(module.module().name(), module);
    }

    public void enableAll() {
        modules.values().forEach(module -> module.module().onEnable());
    }

    public void disableAll() {
        modules.values().forEach(module -> module.module().onDisable());

        for (LoadedModule module : modules.values()) {
            try {
                module.classLoader().close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close class loader of module: " + module.module().name(), e);
            }
        }

        modules.clear();
    }

    public LoadedModule get(String name) {
        return modules.get(name);
    }
}
