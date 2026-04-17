package net.potatocloud.node.module;

import lombok.Getter;
import net.potatocloud.api.module.Module;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ModuleManager {

    private final Map<String, Module> modules = new HashMap<>();

    public void register(Module module) {
        modules.put(module.getName(), module);
    }

    public void enableAll() {
        modules.values().forEach(Module::onEnable);
    }

    public void disableAll() {
        modules.values().forEach(Module::onDisable);
    }

    public Module get(String name) {
        return modules.get(name);
    }
}
