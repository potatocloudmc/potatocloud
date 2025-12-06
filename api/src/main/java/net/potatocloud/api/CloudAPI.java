package net.potatocloud.api;

import lombok.Getter;
import net.potatocloud.api.event.EventManager;
import net.potatocloud.api.group.ServiceGroupManager;
import net.potatocloud.api.platform.PlatformManager;
import net.potatocloud.api.player.CloudPlayerManager;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceManager;

@Getter
public abstract class CloudAPI {

    /**
     * The current CloudAPI instance.
     */
    @Getter
    private static CloudAPI instance;

    /**
     * The current version.
     */
    public static final String VERSION = "1.3.0";

    public CloudAPI() {
        instance = this;
    }

    /**
     * Gets the service group manager.
     *
     * @return the service group manager
     */
    public abstract ServiceGroupManager getServiceGroupManager();

    /**
     * Gets the service manager.
     *
     * @return the service manager
     */
    public abstract ServiceManager getServiceManager();

    /**
     * Gets the platform manager.
     *
     * @return the platform manager
     */
    public abstract PlatformManager getPlatformManager();

    /**
     * Gets the event manager.
     *
     * @return the event manager
     */
    public abstract EventManager getEventManager();

    /**
     * Gets the player manager.
     *
     * @return the player manager
     */
    public abstract CloudPlayerManager getPlayerManager();

    /**
     * Gets the global properties holder.
     *
     * @return the global properties holder
     */
    public abstract PropertyHolder getGlobalProperties();

    /**
     * @deprecated Use {@link ServiceManager#getCurrentService()} instead
     */
    @Deprecated
    public Service getThisService() {
        return getServiceManager().getCurrentService();
    }
}
