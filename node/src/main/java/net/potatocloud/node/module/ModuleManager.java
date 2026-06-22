package net.potatocloud.node.module;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {

    private final Map<String, LoadedModule> modules = new HashMap<>();

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
                throw new RuntimeException("Failed to class loader of module: " + module.module().name(), e);
            }
        }

        modules.clear();
    }

    public Map<String, LoadedModule> modules() {
        return modules;
    }

    public LoadedModule get(String name) {
        return modules.get(name);
    }
}
