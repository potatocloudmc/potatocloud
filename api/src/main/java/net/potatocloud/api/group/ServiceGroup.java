package net.potatocloud.api.group;

import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.platform.Platform;
import net.potatocloud.api.platform.PlatformVersion;
import net.potatocloud.api.player.CloudPlayer;
import net.potatocloud.api.property.PropertyHolder;
import net.potatocloud.api.service.Service;

import java.util.List;
import java.util.Set;

public interface ServiceGroup extends PropertyHolder {

    /**
     * Gets the name of the group.
     *
     * @return the name of the group
     */
    String getName();

    /**
     * Gets the name of the platform of the group.
     *
     * @return the name of the platform of the group
     */
    String getPlatformName();

    /**
     * Gets the platform of the group as an object.
     *
     * @return the platform of the group
     */
    default Platform getPlatform() {
        return CloudAPI.getInstance().getPlatformManager().getPlatform(getPlatformName());
    }

    /**
     * Gets the name of the platform version of the group.
     *
     * @return the name of the platform version of the group
     */
    String getPlatformVersionName();

    /**
     * Gets the platform version of the group as an object.
     *
     * @return the platform version of the group
     */
    default PlatformVersion getPlatformVersion() {
        return getPlatform().getVersion(getPlatformVersionName());
    }

    /**
     * Gets the list of service templates of the group.
     *
     * @return the list of service templates of the group
     */
    List<String> getServiceTemplates();

    /**
     * Gets the minimum online count of the group.
     *
     * @return the minimum online count of the group
     */
    int getMinOnlineCount();

    /**
     * Sets the minimum online count of the group.
     *
     * @param minOnlineCount the minimum online count of the group
     */
    void setMinOnlineCount(int minOnlineCount);

    /**
     * Gets the maximum online count of the group.
     *
     * @return the maximum online count of the group
     */
    int getMaxOnlineCount();

    /**
     * Sets the maximum online count of the group.
     *
     * @param maxOnlineCount the maximum online count of the group
     */
    void setMaxOnlineCount(int maxOnlineCount);

    /**
     * Gets the online players of the group.
     *
     * @return the online players of the group
     */
    default Set<CloudPlayer> getOnlinePlayers() {
        return CloudAPI.getInstance().getPlayerManager().getOnlinePlayersByGroup(this);
    }

    /**
     * Gets the online player count of the group.
     *
     * @return the online player count of the group
     */
    default int getOnlinePlayerCount() {
        return getOnlinePlayers().size();
    }

    /**
     * Gets the maximum players of the group.
     *
     * @return the maximum players of the group
     */
    int getMaxPlayers();

    /**
     * Sets the maximum players of the group.
     *
     * @param maxPlayers the maximum players of the group
     */
    void setMaxPlayers(int maxPlayers);

    /**
     * Gets the maximum memory of the group.
     *
     * @return the maximum memory of the group in MB
     */
    int getMaxMemory();

    /**
     * Sets the maximum memory of the group.
     *
     * @param maxMemory the maximum memory of the group in MB
     */
    void setMaxMemory(int maxMemory);

    /**
     * Gets whether the group is a fallback.
     *
     * @return {@code true} if the group is a fallback, otherwise {@code false}
     */
    boolean isFallback();

    /**
     * Sets whether the group is a fallback.
     *
     * @param fallback {@code true} to make the group a fallback, otherwise {@code false}
     */
    void setFallback(boolean fallback);

    /**
     * Gets whether the group is static.
     *
     * @return {@code true} if the group is static, otherwise {@code false}
     */
    boolean isStatic();

    /**
     * Gets the start priority of the group.
     *
     * @return the start priority of the group
     */
    int getStartPriority();

    /**
     * Sets the start priority of the group.
     *
     * @param startPriority the start priority of the group
     */
    void setStartPriority(int startPriority);

    /**
     * Gets the start percentage of the group.
     *
     * @return the start percentage of the group
     */
    int getStartPercentage();

    /**
     * Sets the start percentage of the group.
     *
     * @param startPercentage the start percentage of the group
     */
    void setStartPercentage(int startPercentage);

    /**
     * Gets the Java command used to start services of the group.
     *
     * @return the Java command of the group
     */
    String getJavaCommand();

    /**
     * Gets the custom jvm flags of the group.
     *
     * @return the custom jvm flags of the group
     */
    List<String> getCustomJvmFlags();

    /**
     * Adds a custom jvm flag to the group.
     *
     * @param flag the custom jvm flag to add
     */
    void addCustomJvmFlag(String flag);

    /**
     * Adds a service template to the group.
     *
     * @param template the service template to add
     */
    void addServiceTemplate(String template);

    /**
     * Removes a service template from the group.
     *
     * @param template the service template to remove
     */
    void removeServiceTemplate(String template);

    /**
     * Gets all services of the group.
     *
     * @return the list of all services of the group
     */
    default List<Service> getAllServices() {
        return CloudAPI.getInstance().getServiceManager().getAllServices(getName());
    }

    /**
     * Gets all online services of the group.
     *
     * @return the list of all online services of the group
     */
    default List<Service> getOnlineServices() {
        return CloudAPI.getInstance().getServiceManager().getOnlineServices(getName());
    }

    /**
     * Gets the online service count of the group.
     *
     * @return the online service count of the group
     */
    default int getOnlineServiceCount() {
        return getOnlineServices().size();
    }

    /**
     * Updates the group.
     */
    default void update() {
        CloudAPI.getInstance().getServiceGroupManager().updateServiceGroup(this);
    }
}
