package net.potatocloud.api.module;

import net.potatocloud.api.version.Version;

/**
 * Represents a loadable potatocloud module.
 */
public interface Module {

    /**
     * Called when the module is loaded, before it is enabled.
     */
    void onLoad();

    /**
     * Called when the module is enabled.
     */
    void onEnable();

    /**
     * Called when the module is disabled.
     */
    void onDisable();

    /**
     * Gets the name of the module.
     *
     * @return the name of the module
     */
    String name();

    /**
     * Gets the version of the module.
     *
     * @return the version of the module
     */
    Version version();

}
