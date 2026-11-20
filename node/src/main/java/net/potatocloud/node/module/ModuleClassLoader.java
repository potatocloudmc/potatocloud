package net.potatocloud.node.module;

import java.net.URL;
import java.net.URLClassLoader;

public final class ModuleClassLoader extends URLClassLoader {

    public ModuleClassLoader(URL jar, ClassLoader parent) {
        super(new URL[]{jar}, parent);
    }

    @Override
    public URL getResource(String name) {
        final URL url = findResource(name);
        return url != null ? url : super.getResource(name);
    }
}
