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

    String getName();

    String getPlatformName();

    default Platform getPlatform() {
        return CloudAPI.getInstance().getPlatformManager().getPlatform(getPlatformName());
    }

    String getPlatformVersionName();

    default PlatformVersion getPlatformVersion() {
        return getPlatform().getVersion(getPlatformVersionName());
    }

    List<String> getServiceTemplates();

    int getMinOnlineCount();

    void setMinOnlineCount(int minOnlineCount);

    int getMaxOnlineCount();

    void setMaxOnlineCount(int maxOnlineCount);

    default Set<CloudPlayer> getOnlinePlayers() {
        return CloudAPI.getInstance().getPlayerManager().getOnlinePlayersByGroup(this);
    }

    default int getOnlinePlayerCount() {
        return getOnlinePlayers().size();
    }

    int getMaxPlayers();

    void setMaxPlayers(int maxPlayers);

    int getMaxMemory();

    void setMaxMemory(int maxMemory);

    boolean isFallback();

    void setFallback(boolean fallback);

    boolean isStatic();

    int getStartPriority();

    void setStartPriority(int startPriority);

    int getStartPercentage();

    void setStartPercentage(int startPercentage);

    String getJavaCommand();

    List<String> getCustomJvmFlags();

    void addCustomJvmFlag(String flag);

    void addServiceTemplate(String template);

    void removeServiceTemplate(String template);

    default List<Service> getAllServices() {
        return CloudAPI.getInstance().getServiceManager().getAllServices(getName());
    }

    default List<Service> getOnlineServices() {
        return CloudAPI.getInstance().getServiceManager().getOnlineServices(getName());
    }

    default int getOnlineServiceCount() {
        return getOnlineServices().size();
    }

    default void update() {
        CloudAPI.getInstance().getServiceGroupManager().updateServiceGroup(this);
    }
}
