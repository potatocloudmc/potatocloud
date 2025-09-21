package net.potatocloud.plugin;

import lombok.experimental.UtilityClass;
import net.potatocloud.api.CloudAPI;
import net.potatocloud.api.service.Service;
import net.potatocloud.api.service.ServiceStatus;

import java.util.Comparator;

@UtilityClass
public class PluginUtils {

    public Service getBestFallback() {
        return CloudAPI.getInstance().getServiceManager().getAllServices().stream()
                .filter(service -> service.getServiceGroup().isFallback())
                .filter(service -> service.getStatus() == ServiceStatus.RUNNING)
                .min(Comparator.comparingInt(Service::getOnlinePlayerCount))
                .orElse(null);
    }
}
