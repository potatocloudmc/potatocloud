package net.potatocloud.api;

import net.potatocloud.api.cluster.ClusterManager;
import net.potatocloud.api.event.EventBus;
import net.potatocloud.api.group.GroupManager;
import net.potatocloud.api.logging.Logger;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.service.ServiceManager;
import net.potatocloud.api.version.Version;

/**
 * Main entry point for the potatocloud API.
 */
public abstract class CloudAPI {

    /**
     * The current CloudAPI instance.
     */
    private static CloudAPI instance;

    /**
     * The current version.
     */
    public static final Version VERSION = Version.of(2, 0, 0);

    /**
     * Creates the API instance.
     */
    public CloudAPI() {
        instance = this;
    }

    /**
     * Gets the current CloudAPI instance.
     *
     * @return the CloudAPI instance
     */
    public static CloudAPI instance() {
        return instance;
    }

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    public abstract Logger logger();

    /**
     * Gets the group manager.
     *
     * @return the group manager
     */
    public abstract GroupManager groupManager();

    /**
     * Gets the service manager.
     *
     * @return the service manager
     */
    public abstract ServiceManager serviceManager();

    /**
     * Gets the platform manager.
     *
     * @return the platform manager
     */
    public abstract PlatformManager platformManager();

    /**
     * Gets the event bus.
     *
     * @return the event bus
     */
    public abstract EventBus eventBus();

    /**
     * Gets the player manager.
     *
     * @return the player manager
     */
    public abstract CloudPlayerManager playerManager();

    /**
     * Gets the global properties holder.
     *
     * @return the global properties holder
     */
    public abstract PropertyHolder globalProperties();

    /**
     * Gets the cluster manager.
     *
     * @return the cluster manager
     */
    public abstract ClusterManager clusterManager();
}
