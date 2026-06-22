package net.potatocloud.node.module;

import net.potatocloud.api.module.Module;

import java.net.URLClassLoader;

public record LoadedModule(Module module, URLClassLoader classLoader) {}
